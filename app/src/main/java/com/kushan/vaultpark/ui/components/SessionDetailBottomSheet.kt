package com.kushan.vaultpark.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kushan.vaultpark.model.ParkingSession
import com.kushan.vaultpark.ui.theme.NeonLime
import com.kushan.vaultpark.ui.theme.Poppins
import com.kushan.vaultpark.ui.theme.SecurityPurple
import com.kushan.vaultpark.ui.utils.formatDateTime
import com.kushan.vaultpark.ui.utils.formatDurationBreakdown
import com.kushan.vaultpark.ui.utils.calculateBillingAmount
import com.kushan.vaultpark.ui.utils.formatAmount

/**
 * Detailed Bottom Sheet for Driver History
 */
@Composable
fun SessionDetailBottomSheet(
    session: ParkingSession,
    onDismiss: () -> Unit,
    onReportIssue: (ParkingSession) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.large
            )
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header with close button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Session Details",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Location Card
        DetailSection(
            title = "Location",
            content = session.gateLocation,
            icon = "📍"
        )

        DetailSection(
            title = "Vehicle",
            content = session.vehicleNumber,
            icon = "🚗"
        )

        // Entry Details
        DetailSectionWithTime(
            title = "Entry",
            time = session.entryTime,
            icon = "🟢"
        )

        // Exit Details (if exists)
        if (session.exitTime != null && session.exitTime > 0) {
            DetailSectionWithTime(
                title = "Exit",
                time = session.exitTime,
                icon = "🔴"
            )

            // Duration Breakdown
            val (hours, minutes, seconds) = formatDurationBreakdown(session.exitTime - session.entryTime)
            DetailSection(
                title = "Duration",
                content = "${hours}h ${minutes}m ${seconds}s",
                icon = "⏱️"
            )

            // Billing Amount
            val amount = calculateBillingAmount(session.exitTime - session.entryTime)
            DetailSection(
                title = "Amount",
                content = formatAmount(amount),
                icon = "💰",
                contentColor = NeonLime
            )
        } else {
            DetailSection(
                title = "Exit",
                content = "Session ongoing",
                icon = "🟡"
            )
        }

        // Guard Info
        if (!session.guardName.isNullOrEmpty()) {
            DetailSection(
                title = "Scanned By",
                content = session.guardName ?: "N/A",
                icon = "👤"
            )
        }

        // QR Code Data
        if (session.qrCodeDataUsed.isNotEmpty()) {
            DetailSection(
                title = "QR Code",
                content = session.qrCodeDataUsed.take(20) + "...",
                icon = "📱"
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            androidx.compose.material3.Button(
                onClick = { onReportIssue(session) },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF6B6B)
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "Report Issue",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }

            androidx.compose.material3.Button(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "Close",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Detail Section Helper
 */
@Composable
fun DetailSection(
    title: String,
    content: String,
    icon: String = "",
    contentColor: Color = MaterialTheme.colorScheme.primary
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (icon.isNotEmpty()) {
                Text(
                    text = icon,
                    fontSize = 20.sp,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = title,
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = content,
            fontFamily = Poppins,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            color = if (contentColor == MaterialTheme.colorScheme.primary) MaterialTheme.colorScheme.onSurface else contentColor,
            modifier = Modifier.padding(top = 8.dp, start = 32.dp)
        )
    }
}

/**
 * Detail Section with Time formatting
 */
@Composable
fun DetailSectionWithTime(
    title: String,
    time: Long,
    icon: String = ""
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (icon.isNotEmpty()) {
                Text(
                    text = icon,
                    fontSize = 20.sp,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = title,
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = formatDateTime(time),
            fontFamily = Poppins,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 8.dp, start = 32.dp)
        )
    }
}
