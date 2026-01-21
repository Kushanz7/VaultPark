package com.kushan.vaultpark.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kushan.vaultpark.ui.components.InvoiceCard
import com.kushan.vaultpark.ui.components.PaymentMethodCard
import com.kushan.vaultpark.ui.components.PricingRow
import com.kushan.vaultpark.ui.components.StatColumn
import com.kushan.vaultpark.ui.components.StatusBadge
import com.kushan.vaultpark.ui.theme.Background
import com.kushan.vaultpark.ui.theme.DarkSurface
import com.kushan.vaultpark.ui.theme.DarkSurfaceVariant
import com.kushan.vaultpark.ui.theme.Poppins
import com.kushan.vaultpark.ui.theme.PrimaryPurple
import com.kushan.vaultpark.ui.theme.SecondaryGold
import com.kushan.vaultpark.ui.theme.StatusError
import com.kushan.vaultpark.ui.theme.StatusSuccess
import com.kushan.vaultpark.ui.theme.TextLight
import com.kushan.vaultpark.ui.theme.TextSecondaryDark
import com.kushan.vaultpark.ui.theme.TextTertiaryDark
import com.kushan.vaultpark.viewmodel.BillingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(
    viewModel: BillingViewModel = viewModel(),
    onBackPressed: (() -> Unit)? = null,
    onInvoiceSelected: ((String) -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentInvoice = uiState.currentInvoice
    val pricingTier = uiState.userPricingTier
    val paymentMethods = uiState.paymentMethods
    val invoiceHistory = uiState.invoiceHistory
    val isLoading = uiState.isLoading
    val isPaymentProcessing = uiState.isPaymentProcessing
    val paymentSuccess = uiState.paymentSuccess
    val error = uiState.error
    
    var selectedPaymentMethodId by remember { 
        mutableStateOf(paymentMethods.firstOrNull { it.isDefault }?.id) 
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Billing & Invoices",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = TextLight
                    )
                },
                navigationIcon = {
                    if (onBackPressed != null) {
                        IconButton(onClick = onBackPressed) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextLight
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryPurple)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(
                    horizontal = 16.dp,
                    vertical = 16.dp
                )
            ) {
                // Error message
                if (error != null) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = StatusError.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = error,
                                fontFamily = Poppins,
                                fontSize = 12.sp,
                                color = StatusError,
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                
                // Payment success message
                if (paymentSuccess) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = StatusSuccess.copy(alpha = 0.2f),
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
                                    contentDescription = "Success",
                                    tint = StatusSuccess,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Payment successful!",
                                    fontFamily = Poppins,
                                    fontSize = 12.sp,
                                    color = StatusSuccess
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                
                // SECTION 1: Current Month Card
                if (currentInvoice != null) {
                    item {
                        CurrentMonthCard(
                            invoice = currentInvoice,
                            pricingTier = pricingTier
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                
                // SECTION 2: Pricing Info Card
                if (pricingTier != null) {
                    item {
                        PricingInfoCard(pricingTier = pricingTier)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                
                // SECTION 3: Payment Methods
                if (paymentMethods.isNotEmpty()) {
                    item {
                        PaymentMethodsSection(
                            methods = paymentMethods,
                            selectedMethodId = selectedPaymentMethodId,
                            onMethodSelected = { selectedPaymentMethodId = it }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                
                // SECTION 4: Pay Now Button
                if (currentInvoice != null && currentInvoice.status != "PAID") {
                    item {
                        PayNowButton(
                            amount = currentInvoice.totalAmount,
                            isLoading = isPaymentProcessing,
                            enabled = selectedPaymentMethodId != null,
                            onPayClick = {
                                if (selectedPaymentMethodId != null) {
                                    viewModel.processPayment(
                                        currentInvoice.id,
                                        currentInvoice.totalAmount,
                                        selectedPaymentMethodId!!
                                    )
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                } else if (currentInvoice?.status == "PAID") {
                    item {
                        PaidButton()
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                
                // SECTION 5: Invoice History
                if (invoiceHistory.isNotEmpty()) {
                    item {
                        Text(
                            text = "Past Invoices",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp,
                            color = TextLight
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    items(invoiceHistory) { invoice ->
                        InvoiceCard(
                            invoice = invoice,
                            onTap = {
                                viewModel.selectInvoice(invoice)
                                onInvoiceSelected?.invoke(invoice.id)
                            },
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrentMonthCard(
    invoice: com.kushan.vaultpark.model.InvoiceNew,
    pricingTier: com.kushan.vaultpark.model.PricingTier?
) {
    val monthName = getMonthName(invoice.month)
    val animatedAmount by animateFloatAsState(
        targetValue = invoice.totalAmount.toFloat(),
        animationSpec = tween(1000)
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .padding(24.dp)
    ) {
        // Header
        Text(
            text = "Current Month",
            fontFamily = Poppins,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "$monthName ${invoice.year}",
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        // Membership badge
        Box(
            modifier = Modifier
                .background(
                    color = SecondaryGold,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "${pricingTier?.membershipType ?: "Gold"} Member",
                fontFamily = Poppins,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = Color.Black
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Divider
        Divider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
            thickness = 1.dp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Stats Row
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
                label = "Total Hours",
                modifier = Modifier.weight(1f)
            )
            StatColumn(
                icon = Icons.Default.Check,
                value = String.format("$%.2f", pricingTier?.hourlyRate ?: 5.0),
                label = "Per Hour",
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Amount Section
        Text(
            text = "Total Amount",
            fontFamily = Poppins,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = String.format("$%.2f", animatedAmount),
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            fontSize = 48.sp,
            color = PrimaryPurple,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Due by ${(invoice.month % 12) + 1}/5/${invoice.year}",
            fontFamily = Poppins,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Status Badge
        StatusBadge(
            status = invoice.status,
            paidDate = null,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun PricingInfoCard(pricingTier: com.kushan.vaultpark.model.PricingTier) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(20.dp)
    ) {
        Text(
            text = "Your Pricing Plan",
            fontFamily = Poppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        PricingRow(
            label = "Hourly Rate",
            value = String.format("$%.2f/hour", pricingTier.hourlyRate)
        )
        PricingRow(
            label = "Daily Maximum",
            value = String.format("$%.2f/day", pricingTier.dailyCap)
        )
        if (pricingTier.monthlyUnlimited != null) {
            PricingRow(
                label = "Monthly Unlimited",
                value = String.format("$%.2f", pricingTier.monthlyUnlimited)
            )
        }
    }
}

@Composable
private fun PaymentMethodsSection(
    methods: List<com.kushan.vaultpark.model.PaymentMethod>,
    selectedMethodId: String?,
    onMethodSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Payment Methods",
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = TextLight
            )
            Text(
                text = "+ Add New",
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = PrimaryPurple,
                modifier = Modifier.clickable { }
            )
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            methods.forEach { method ->
                PaymentMethodCard(
                    paymentMethod = method,
                    isSelected = selectedMethodId == method.id,
                    onSelect = { onMethodSelected(method.id) }
                )
            }
        }
    }
}

@Composable
private fun PayNowButton(
    amount: Double,
    isLoading: Boolean,
    enabled: Boolean,
    onPayClick: () -> Unit
) {
    Button(
        onClick = onPayClick,
        enabled = enabled && !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryPurple,
            disabledContainerColor = TextTertiaryDark
        ),
        shape = RoundedCornerShape(28.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = TextLight,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Processing...",
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = TextLight
            )
        } else {
            Text(
                text = "Pay $${String.format("%.2f", amount)} Now",
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = TextLight
            )
        }
    }
}

@Composable
private fun PaidButton() {
    Button(
        onClick = {},
        enabled = false,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            disabledContainerColor = TextTertiaryDark.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(28.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Paid",
                tint = StatusSuccess,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Already Paid",
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = TextSecondaryDark
            )
        }
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
