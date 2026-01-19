package com.kushan.vaultpark.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kushan.vaultpark.ui.theme.PrimaryPurple

/**
 * Generic empty state composable
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionButton: Pair<String, () -> Unit>? = null
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + scaleIn(),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            if (actionButton != null) {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = actionButton.second,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryPurple
                    )
                ) {
                    Text(
                        text = actionButton.first,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Empty History State
 */
@Composable
fun EmptyHistoryState(
    onScanQR: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Default.Schedule,
        title = "No Parking History",
        message = "Your parking sessions will appear here",
        modifier = modifier,
        actionButton = if (onScanQR != null) {
            "Scan QR to Park" to onScanQR
        } else null
    )
}

/**
 * Empty Logs State
 */
@Composable
fun EmptyLogsState(
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Default.QrCode,
        title = "No Scans Yet",
        message = "Start scanning driver QR codes to see activity",
        modifier = modifier
    )
}

/**
 * Empty Invoices State
 */
@Composable
fun EmptyInvoicesState(
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Default.Note,
        title = "No Invoices Yet",
        message = "Your monthly invoices will appear here",
        modifier = modifier
    )
}

/**
 * Empty Notifications State
 */
@Composable
fun EmptyNotificationsState(
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Default.Notifications,
        title = "No Notifications",
        message = "You're all caught up!",
        modifier = modifier
    )
}

/**
 * Empty Search Results State
 */
@Composable
fun EmptySearchResultsState(
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Default.Search,
        title = "No Results Found",
        message = "Try adjusting your search or filters",
        modifier = modifier
    )
}

/**
 * No Payment Methods State
 */
@Composable
fun NoPaymentMethodsState(
    onAddPaymentMethod: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Default.CreditCard,
        title = "No Payment Methods",
        message = "Add a payment method to pay invoices",
        modifier = modifier,
        actionButton = if (onAddPaymentMethod != null) {
            "Add Payment Method" to onAddPaymentMethod
        } else null
    )
}
