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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.kushan.vaultpark.ui.theme.StatusSuccess
import com.kushan.vaultpark.ui.theme.TextLight
import com.kushan.vaultpark.ui.theme.TextSecondaryDark
import com.kushan.vaultpark.ui.theme.TextTertiaryDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailsScreen(
    invoice: InvoiceNew,
    sessions: List<ParkingSession> = emptyList(),
    onBackPressed: () -> Unit
) {
    val monthName = getMonthName(invoice.month)
    val animatedAmount by animateFloatAsState(
        targetValue = invoice.totalAmount.toFloat(),
        animationSpec = tween(1000)
    )
    
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
            
            // Download Button
            item {
                DownloadInvoiceButton()
            }
        }
    }
}

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
