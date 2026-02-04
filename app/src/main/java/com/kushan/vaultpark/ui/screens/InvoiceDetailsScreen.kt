package com.kushan.vaultpark.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kushan.vaultpark.model.InvoiceNew
import com.kushan.vaultpark.model.ParkingSession
import com.kushan.vaultpark.ui.components.StatColumn
import com.kushan.vaultpark.ui.theme.Background
import com.kushan.vaultpark.ui.theme.DarkSurface
import com.kushan.vaultpark.ui.theme.DarkSurfaceVariant
import com.kushan.vaultpark.ui.theme.Poppins
import com.kushan.vaultpark.ui.theme.PrimaryPurple
import com.kushan.vaultpark.ui.theme.SecondaryGold
import com.kushan.vaultpark.ui.theme.StatusSuccess
import com.kushan.vaultpark.ui.theme.TextLight
import com.kushan.vaultpark.ui.theme.TextSecondaryDark
import com.kushan.vaultpark.ui.theme.TextTertiaryDark
import com.kushan.vaultpark.viewmodel.BillingViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.graphics.Bitmap
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailsScreen(
    invoice: InvoiceNew?,
    invoiceId: String = "",
    billingViewModel: BillingViewModel? = null,
    sessions: List<ParkingSession> = emptyList(),
    onBackPressed: () -> Unit
) {
    val actualInvoice = invoice ?: InvoiceNew(id = invoiceId)
    val monthName = getMonthName(actualInvoice.month)
    val animatedAmount by animateFloatAsState(
        targetValue = actualInvoice.totalAmount.toFloat(),
        animationSpec = tween(1000)
    )
    
    var paymentStep by remember { mutableStateOf<PaymentStep>(PaymentStep.DETAILS) }
    var qrCodeBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isProcessingPayment by remember { mutableStateOf(false) }
    var paymentSuccess by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Invoice Details",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = TextLight
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextLight
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface
                )
            )
        },
        containerColor = Background
    ) { paddingValues ->
        when (paymentStep) {
            PaymentStep.DETAILS -> {
                InvoiceDetailsContent(
                    invoice = actualInvoice,
                    monthName = monthName,
                    animatedAmount = animatedAmount,
                    sessions = sessions,
                    paddingValues = paddingValues,
                    onPayClick = {
                        if (actualInvoice.status != "PAID") {
                            paymentStep = PaymentStep.PAYMENT_PROCESS
                        }
                    }
                )
            }
            PaymentStep.PAYMENT_PROCESS -> {
                PaymentProcessContent(
                    invoice = actualInvoice,
                    paddingValues = paddingValues,
                    isProcessing = isProcessingPayment,
                    onProcessPayment = {
                        isProcessingPayment = true
                        // Simulate payment processing
                        scope.launch {
                            delay(2000)
                            paymentStep = PaymentStep.QR_CODE_DISPLAY
                            isProcessingPayment = false
                        }
                    },
                    onCancel = {
                        paymentStep = PaymentStep.DETAILS
                    }
                )
            }
            PaymentStep.QR_CODE_DISPLAY -> {
                QRCodePaymentContent(
                    invoice = actualInvoice,
                    paddingValues = paddingValues,
                    onQRGenerated = { bitmap ->
                        qrCodeBitmap = bitmap
                    },
                    onComplete = {
                        paymentSuccess = true
                        paymentStep = PaymentStep.COMPLETION
                    }
                )
            }
            PaymentStep.COMPLETION -> {
                PaymentCompletionContent(
                    invoice = actualInvoice,
                    paddingValues = paddingValues,
                    onDone = {
                        paymentStep = PaymentStep.DETAILS
                        onBackPressed()
                    }
                )
            }
        }
    }
}

enum class PaymentStep {
    DETAILS,
    PAYMENT_PROCESS,
    QR_CODE_DISPLAY,
    COMPLETION
}

@Composable
private fun InvoiceDetailsContent(
    invoice: InvoiceNew,
    monthName: String,
    animatedAmount: Float,
    sessions: List<ParkingSession>,
    paddingValues: androidx.compose.foundation.layout.PaddingValues,
    onPayClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(
            horizontal = 16.dp,
            vertical = 16.dp
        )
    ) {
        // Invoice Header Card
        item {
            InvoiceHeaderCard(
                invoice = invoice,
                monthName = monthName
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Invoice Summary
        item {
            InvoiceSummaryCard(
                invoice = invoice,
                monthName = monthName,
                animatedAmount = animatedAmount
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
        
        // Session Breakdown
        if (sessions.isNotEmpty()) {
            item {
                Text(
                    text = "Session Breakdown",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = TextLight
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            items(sessions.size) { index ->
                SessionDetailCard(
                    session = sessions[index],
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
        }
        
        // Amount Summary
        item {
            AmountSummaryCard(invoice = invoice)
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Payment Button or Status
        item {
            if (invoice.status == "PAID") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = StatusSuccess.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Paid",
                            tint = StatusSuccess,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Invoice Paid",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = StatusSuccess
                        )
                    }
                }
            } else {
                Button(
                    onClick = onPayClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryPurple
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = "Pay",
                            tint = ComposeColor.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Pay Now - $${String.format("%.2f", invoice.totalAmount)}",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = ComposeColor.White
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Download Button
        item {
            DownloadInvoiceButton()
        }
    }
}

@Composable
private fun PaymentProcessContent(
    invoice: InvoiceNew,
    paddingValues: androidx.compose.foundation.layout.PaddingValues,
    isProcessing: Boolean,
    onProcessPayment: () -> Unit,
    onCancel: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = DarkSurface,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Confirm Payment",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = TextLight
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Amount to Pay",
                fontFamily = Poppins,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = TextSecondaryDark
            )
            
            Text(
                text = "$${String.format("%.2f", invoice.totalAmount)}",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 48.sp,
                color = SecondaryGold
            )
            
            Divider(color = TextTertiaryDark.copy(alpha = 0.2f))
            
            Text(
                text = "Invoice: ${getMonthName(invoice.month)} ${invoice.year}",
                fontFamily = Poppins,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = TextSecondaryDark
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onProcessPayment,
                enabled = !isProcessing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryPurple
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = ComposeColor.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Processing...")
                } else {
                    Text("Continue to Payment")
                }
            }
            
            Button(
                onClick = onCancel,
                enabled = !isProcessing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TextTertiaryDark.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Cancel", color = TextLight)
            }
        }
    }
}

@Composable
private fun QRCodePaymentContent(
    invoice: InvoiceNew,
    paddingValues: androidx.compose.foundation.layout.PaddingValues,
    onQRGenerated: (Bitmap?) -> Unit,
    onComplete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = DarkSurface,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Scan to Complete Payment",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = TextLight
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Placeholder QR Code Box
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .background(
                        color = ComposeColor.White,
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = "QR Code",
                        modifier = Modifier.size(80.dp),
                        tint = PrimaryPurple
                    )
                    Text(
                        text = "[QR Code Generated]\nAmount: $${String.format("%.2f", invoice.totalAmount)}",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = ComposeColor.Black,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Use your payment app to scan this QR code",
                fontFamily = Poppins,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = TextSecondaryDark,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StatusSuccess
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Complete",
                        tint = ComposeColor.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text("Payment Complete")
                }
            }
        }
    }
}

@Composable
private fun PaymentCompletionContent(
    invoice: InvoiceNew,
    paddingValues: androidx.compose.foundation.layout.PaddingValues,
    onDone: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = DarkSurface,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = StatusSuccess.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(50)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Success",
                    tint = StatusSuccess,
                    modifier = Modifier.size(48.dp)
                )
            }
            
            Text(
                text = "Payment Successful!",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = StatusSuccess
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Your payment of $${String.format("%.2f", invoice.totalAmount)} has been processed",
                fontFamily = Poppins,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = TextSecondaryDark,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "${getMonthName(invoice.month)} ${invoice.year}",
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = TextLight
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onDone,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryPurple
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Done", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// Existing helper composables remain below...


@Composable
private fun InvoiceHeaderCard(
    invoice: InvoiceNew,
    monthName: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = DarkSurface,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
            )
            .padding(20.dp)
    ) {
        Text(
            text = "Invoice #${invoice.id.take(8).uppercase()}",
            fontFamily = Poppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = TextSecondaryDark
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "$monthName ${invoice.year}",
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = TextLight
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        Divider(color = TextTertiaryDark.copy(alpha = 0.2f))
        
        Spacer(modifier = Modifier.height(16.dp))
        
        InvoiceDetailRow("Status", invoice.status)
        Spacer(modifier = Modifier.height(8.dp))
        InvoiceDetailRow(
            "Generated",
            formatDate(invoice.generatedAt?.time ?: System.currentTimeMillis())
        )
        if (invoice.paidAt != null) {
            Spacer(modifier = Modifier.height(8.dp))
            InvoiceDetailRow("Paid On", formatDate(invoice.paidAt.time))
        }
    }
}

@Composable
private fun InvoiceSummaryCard(
    invoice: InvoiceNew,
    monthName: String,
    animatedAmount: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(DarkSurfaceVariant, DarkSurface)
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Invoice Summary",
            fontFamily = Poppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = TextLight
        )
        Spacer(modifier = Modifier.height(20.dp))
        
        // Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatColumn(
                icon = Icons.Default.LocalParking,
                value = invoice.totalSessions.toString(),
                label = "Sessions",
                modifier = Modifier.weight(1f)
            )
            StatColumn(
                icon = Icons.Default.CreditCard,
                value = String.format("%.1f", invoice.totalHours),
                label = "Hours",
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Divider(color = TextTertiaryDark.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Total Amount",
            fontFamily = Poppins,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = TextSecondaryDark
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = String.format("$%.2f", animatedAmount),
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            fontSize = 48.sp,
            color = PrimaryPurple
        )
    }
}

@Composable
private fun SessionDetailCard(
    session: ParkingSession,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = DarkSurface,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = formatDateShort(session.entryTime),
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = TextLight
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${formatTime(session.entryTime)} - ${formatTime(session.exitTime ?: 0L)}",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = TextSecondaryDark
                )
            }
            
            Text(
                text = session.duration,
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = PrimaryPurple
            )
        }
    }
}

@Composable
private fun AmountSummaryCard(invoice: InvoiceNew) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = DarkSurface,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            )
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Subtotal",
                fontFamily = Poppins,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = TextSecondaryDark
            )
            Text(
                text = String.format("$%.2f", invoice.totalAmount),
                fontFamily = Poppins,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = TextLight
            )
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Tax",
                fontFamily = Poppins,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = TextSecondaryDark
            )
            Text(
                text = "$0.00",
                fontFamily = Poppins,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = TextLight
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        Divider(color = TextTertiaryDark.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Total",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextLight
            )
            Text(
                text = String.format("$%.2f", invoice.totalAmount),
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = PrimaryPurple
            )
        }
    }
}

@Composable
private fun DownloadInvoiceButton() {
    Button(
        onClick = { },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryPurple
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
    ) {
        Icon(
            imageVector = Icons.Default.FileDownload,
            contentDescription = "Download",
            tint = TextLight,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.padding(8.dp))
        Text(
            text = "Download Invoice PDF",
            fontFamily = Poppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = TextLight
        )
    }
}

@Composable
private fun InvoiceDetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontFamily = Poppins,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            color = TextSecondaryDark
        )
        Text(
            text = value,
            fontFamily = Poppins,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = TextLight
        )
    }
}

private fun getMonthName(month: Int): String {
    return when (month) {
        1 -> "January"
        2 -> "February"
        3 -> "March"
        4 -> "April"
        5 -> "May"
        6 -> "June"
        7 -> "July"
        8 -> "August"
        9 -> "September"
        10 -> "October"
        11 -> "November"
        12 -> "December"
        else -> "Unknown"
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatDateShort(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
