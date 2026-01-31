package com.kushan.vaultpark.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kushan.vaultpark.model.ShiftReport
import com.kushan.vaultpark.ui.theme.*
import com.kushan.vaultpark.viewmodel.AdminToolsViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * STEP 2: Shift Report Dialog
 * Features:
 * - End-of-shift report generation
 * - Summary of scans, entries, exits
 * - Session list
 * - Share via WhatsApp/Email
 * - View past reports
 */
@Composable
fun ShiftReportDialog(
    report: ShiftReport,
    onDismiss: () -> Unit,
    onShare: (String) -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 700.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Shift Report",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = TextLight
                            )
                            Text(
                                text = report.shiftDate,
                                fontFamily = Poppins,
                                fontSize = 14.sp,
                                color = TextSecondaryDark
                            )
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextSecondaryDark
                            )
                        }
                    }
                }

                // Guard Info
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = PrimaryPurple.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Badge,
                                    contentDescription = null,
                                    tint = PrimaryPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = report.guardName,
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = TextLight
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = SecondaryGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = report.gateLocation,
                                    fontFamily = Poppins,
                                    fontSize = 14.sp,
                                    color = TextSecondaryDark
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = TextSecondaryDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "${formatTime(report.shiftStartTime)} - ${formatTime(report.shiftEndTime ?: System.currentTimeMillis())}",
                                    fontFamily = Poppins,
                                    fontSize = 14.sp,
                                    color = TextSecondaryDark
                                )
                            }
                        }
                    }
                }

                // Statistics Grid
                item {
                    Text(
                        text = "Summary",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = TextLight
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ShiftStatCard(
                            modifier = Modifier.weight(1f),
                            value = report.totalScans.toString(),
                            label = "Total Scans",
                            icon = Icons.Default.QrCode,
                            color = PrimaryPurple
                        )

                        ShiftStatCard(
                            modifier = Modifier.weight(1f),
                            value = report.totalEntries.toString(),
                            label = "Entries",
                            icon = Icons.Default.Login,
                            color = StatusActive
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ShiftStatCard(
                            modifier = Modifier.weight(1f),
                            value = report.totalExits.toString(),
                            label = "Exits",
                            icon = Icons.Default.Logout,
                            color = StatusError
                        )

                        ShiftStatCard(
                            modifier = Modifier.weight(1f),
                            value = report.manualEntries.toString(),
                            label = "Manual",
                            icon = Icons.Default.Edit,
                            color = SecondaryGold
                        )
                    }
                }

                // Session Summary
                if (report.sessionSummaries.isNotEmpty()) {
                    item {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sessions (${report.sessionSummaries.size})",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = TextLight
                            )
                        }
                    }

                    items(report.sessionSummaries.take(10)) { session ->
                        SessionSummaryItem(session = session)
                    }

                    if (report.sessionSummaries.size > 10) {
                        item {
                            Text(
                                text = "+ ${report.sessionSummaries.size - 10} more sessions",
                                fontFamily = Poppins,
                                fontSize = 12.sp,
                                color = TextSecondaryDark,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }

                // Issues
                if (report.issues.isNotEmpty()) {
                    item {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        Text(
                            text = "Issues",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = StatusError
                        )
                    }

                    items(report.issues) { issue ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = StatusError.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = StatusError,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = issue,
                                    fontFamily = Poppins,
                                    fontSize = 14.sp,
                                    color = TextLight
                                )
                            }
                        }
                    }
                }

                // Notes
                if (report.notes.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notes,
                                        contentDescription = null,
                                        tint = TextSecondaryDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Notes",
                                        fontFamily = Poppins,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = TextLight
                                    )
                                }
                                Text(
                                    text = report.notes,
                                    fontFamily = Poppins,
                                    fontSize = 14.sp,
                                    color = TextSecondaryDark
                                )
                            }
                        }
                    }
                }

                // Share Button
                item {
                    Button(
                        onClick = {
                            val reportText = generateReportText(report)
                            onShare(reportText)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryPurple
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Share Report",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShiftStatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = value,
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = color
            )

            Text(
                text = label,
                fontFamily = Poppins,
                fontSize = 12.sp,
                color = TextSecondaryDark
            )
        }
    }
}

@Composable
private fun SessionSummaryItem(
    session: com.kushan.vaultpark.model.SessionSummary
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = if (session.status == "ACTIVE") StatusActive else TextSecondaryDark,
                    modifier = Modifier.size(20.dp)
                )

                Column {
                    Text(
                        text = session.driverName,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = TextLight
                    )
                    Text(
                        text = session.vehicleNumber,
                        fontFamily = Poppins,
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatTime(session.entryTime),
                    fontFamily = Poppins,
                    fontSize = 12.sp,
                    color = TextSecondaryDark
                )
                if (session.exitTime != null) {
                    Text(
                        text = "→ ${formatTime(session.exitTime)}",
                        fontFamily = Poppins,
                        fontSize = 12.sp,
                        color = StatusActive
                    )
                }
            }
        }
    }
}

/**
 * End Shift Button for Security Home
 */
@Composable
fun EndShiftButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = SecondaryGold
        ),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.AssignmentTurnedIn,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            "End Shift & Generate Report",
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

/**
 * Past Reports List Component
 */
@Composable
fun PastReportsList(
    reports: List<ShiftReport>,
    onReportClick: (ShiftReport) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(reports) { report ->
            PastReportItem(
                report = report,
                onClick = { onReportClick(report) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PastReportItem(
    report: ShiftReport,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = report.shiftDate,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = TextLight
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "${report.totalScans} scans",
                        fontFamily = Poppins,
                        fontSize = 12.sp,
                        color = PrimaryPurple
                    )
                    Text(
                        text = "${report.totalEntries} in, ${report.totalExits} out",
                        fontFamily = Poppins,
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondaryDark
            )
        }
    }
}

// Utility Functions
private fun formatTime(timeMillis: Long): String {
    return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timeMillis))
}

private fun generateReportText(report: ShiftReport): String {
    return buildString {
        appendLine("🛡️ VAULTPARK SHIFT REPORT")
        appendLine("=" .repeat(40))
        appendLine()
        appendLine("Guard: ${report.guardName}")
        appendLine("Date: ${report.shiftDate}")
        appendLine("Gate: ${report.gateLocation}")
        appendLine("Shift: ${formatTime(report.shiftStartTime)} - ${formatTime(report.shiftEndTime ?: System.currentTimeMillis())}")
        appendLine()
        appendLine("📊 SUMMARY")
        appendLine("-".repeat(40))
        appendLine("Total Scans: ${report.totalScans}")
        appendLine("Entries: ${report.totalEntries}")
        appendLine("Exits: ${report.totalExits}")
        appendLine("Manual Entries: ${report.manualEntries}")
        appendLine()
        
        if (report.sessionSummaries.isNotEmpty()) {
            appendLine("🚗 SESSIONS")
            appendLine("-".repeat(40))
            report.sessionSummaries.take(20).forEach { session ->
                val status = if (session.exitTime != null) "✓" else "⏳"
                appendLine("$status ${session.driverName} - ${session.vehicleNumber}")
                appendLine("   ${formatTime(session.entryTime)}" +
                    if (session.exitTime != null) " → ${formatTime(session.exitTime)}" else " (Active)")
            }
            if (report.sessionSummaries.size > 20) {
                appendLine("... and ${report.sessionSummaries.size - 20} more")
            }
            appendLine()
        }
        
        if (report.issues.isNotEmpty()) {
            appendLine("⚠️ ISSUES")
            appendLine("-".repeat(40))
            report.issues.forEach { issue ->
                appendLine("• $issue")
            }
            appendLine()
        }
        
        if (report.notes.isNotEmpty()) {
            appendLine("📝 NOTES")
            appendLine("-".repeat(40))
            appendLine(report.notes)
            appendLine()
        }
        
        appendLine("=" .repeat(40))
        appendLine("Generated by VaultPark Security System")
    }
}
