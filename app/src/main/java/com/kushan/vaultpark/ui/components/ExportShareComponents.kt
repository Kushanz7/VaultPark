package com.kushan.vaultpark.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.kushan.vaultpark.model.ParkingSession
import com.kushan.vaultpark.ui.theme.*
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

// ============ ✨ STEP 4: EXPORT & SHARE SESSIONS ============

/**
 * Export Options Dialog
 */
@Composable
fun ExportOptionsDialog(
    sessions: List<ParkingSession>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isExporting by remember { mutableStateOf(false) }
    var exportSuccess by remember { mutableStateOf<String?>(null) }
    var exportError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Export Sessions",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = TextLight
                        )
                        Text(
                            text = "${sessions.size} sessions",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            color = TextSecondaryDark
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = TextSecondaryDark
                        )
                    }
                }

                if (exportSuccess != null) {
                    SuccessMessage(message = exportSuccess!!)
                } else if (exportError != null) {
                    ErrorMessage(message = exportError!!)
                }

                // Export Options
                ExportOption(
                    icon = Icons.Filled.TableChart,
                    title = "Export as CSV",
                    description = "Spreadsheet format for Excel/Sheets",
                    onClick = {
                        isExporting = true
                        exportToCSV(context, sessions) { success, message ->
                            isExporting = false
                            if (success) {
                                exportSuccess = message
                                exportError = null
                            } else {
                                exportError = message
                                exportSuccess = null
                            }
                        }
                    },
                    enabled = !isExporting
                )

                ExportOption(
                    icon = Icons.Filled.PictureAsPdf,
                    title = "Export as PDF",
                    description = "Professional PDF report",
                    onClick = {
                        isExporting = true
                        exportToPDF(context, sessions) { success, message ->
                            isExporting = false
                            if (success) {
                                exportSuccess = message
                                exportError = null
                            } else {
                                exportError = message
                                exportSuccess = null
                            }
                        }
                    },
                    enabled = !isExporting
                )

                ExportOption(
                    icon = Icons.Filled.Email,
                    title = "Email Report",
                    description = "Send via email",
                    onClick = {
                        emailSessionSummary(context, sessions)
                    },
                    enabled = !isExporting
                )

                if (isExporting) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = PrimaryPurple
                    )
                }
            }
        }
    }
}

@Composable
private fun ExportOption(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(
            alpha = if (enabled) 1f else 0.5f
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = PrimaryPurple,
                modifier = Modifier.size(32.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = TextLight
                )
                Text(
                    text = description,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = TextSecondaryDark
                )
            }

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Go",
                tint = TextTertiaryDark,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Share Single Session Sheet
 */
@Composable
fun ShareSessionSheet(
    session: ParkingSession,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Share Session",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = TextLight
            )

            // Session Preview
            SessionReceiptPreview(session = session)

            Spacer(modifier = Modifier.height(8.dp))

            // Share Options
            ShareOption(
                icon = Icons.Filled.Share,
                title = "Share Details",
                description = "Share via any app",
                onClick = {
                    shareSessionText(context, session)
                    onDismiss()
                }
            )

            ShareOption(
                icon = Icons.Filled.Receipt,
                title = "Generate Receipt",
                description = "Create printable receipt",
                onClick = {
                    generateReceipt(context, session)
                    onDismiss()
                }
            )

            ShareOption(
                icon = Icons.Filled.Email,
                title = "Email Receipt",
                description = "Send receipt via email",
                onClick = {
                    emailReceipt(context, session)
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ShareOption(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = PrimaryPurple,
                modifier = Modifier.size(28.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = TextLight
                )
                Text(
                    text = description,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = TextSecondaryDark
                )
            }
        }
    }
}

@Composable
private fun SessionReceiptPreview(session: ParkingSession) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ReceiptRow("Gate", session.gateLocation)
            ReceiptRow("Entry", formatDateTime(session.entryTime))
            if (session.exitTime != null) {
                ReceiptRow("Exit", formatDateTime(session.exitTime))
                ReceiptRow("Duration", calculateDuration(session.entryTime, session.exitTime))
            }
            ReceiptRow("Vehicle", session.vehicleNumber)
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = TextLight
        )
    }
}

@Composable
private fun SuccessMessage(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF4CAF50).copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Success",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = message,
                fontFamily = Poppins,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = Color(0xFF4CAF50)
            )
        }
    }
}

@Composable
private fun ErrorMessage(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFEF5350).copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = "Error",
                tint = Color(0xFFEF5350),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = message,
                fontFamily = Poppins,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = Color(0xFFEF5350)
            )
        }
    }
}

// ============ EXPORT UTILITY FUNCTIONS ============

/**
 * Export sessions to CSV file
 */
private fun exportToCSV(
    context: Context,
    sessions: List<ParkingSession>,
    callback: (Boolean, String) -> Unit
) {
    try {
        val fileName = "parking_sessions_${System.currentTimeMillis()}.csv"
        val file = File(context.getExternalFilesDir(null), fileName)
        
        FileWriter(file).use { writer ->
            // CSV Header
            writer.append("Date,Gate,Entry Time,Exit Time,Duration,Vehicle,Status,Notes,Tags\n")
            
            // CSV Rows
            sessions.forEach { session ->
                writer.append("\"${formatDate(session.entryTime)}\",")
                writer.append("\"${session.gateLocation}\",")
                writer.append("\"${formatTime(session.entryTime)}\",")
                writer.append("\"${if (session.exitTime != null) formatTime(session.exitTime) else "Active"}\",")
                writer.append("\"${if (session.exitTime != null) calculateDuration(session.entryTime, session.exitTime) else "Ongoing"}\",")
                writer.append("\"${session.vehicleNumber}\",")
                writer.append("\"${session.status}\",")
                writer.append("\"${session.notes.replace("\"", "\"\"")}\",")
                writer.append("\"${session.tags.joinToString(", ")}\"\n")
            }
        }
        
        // Share the file
        shareFile(context, file, "text/csv")
        callback(true, "CSV exported successfully")
        
    } catch (e: Exception) {
        callback(false, "Export failed: ${e.message}")
    }
}

/**
 * Export sessions to PDF (simplified version)
 */
private fun exportToPDF(
    context: Context,
    sessions: List<ParkingSession>,
    callback: (Boolean, String) -> Unit
) {
    // Note: For a full PDF implementation, you would use a library like iText or PDFDocument
    // This is a placeholder that exports as text file with .pdf extension
    try {
        val fileName = "parking_report_${System.currentTimeMillis()}.txt"
        val file = File(context.getExternalFilesDir(null), fileName)
        
        FileWriter(file).use { writer ->
            writer.append("═══════════════════════════════════════\n")
            writer.append("        VAULTPARK PARKING REPORT\n")
            writer.append("═══════════════════════════════════════\n\n")
            writer.append("Generated: ${formatDateTime(System.currentTimeMillis())}\n")
            writer.append("Total Sessions: ${sessions.size}\n\n")
            
            sessions.forEachIndexed { index, session ->
                writer.append("───────────────────────────────────────\n")
                writer.append("Session ${index + 1}\n")
                writer.append("───────────────────────────────────────\n")
                writer.append("Gate: ${session.gateLocation}\n")
                writer.append("Vehicle: ${session.vehicleNumber}\n")
                writer.append("Entry: ${formatDateTime(session.entryTime)}\n")
                if (session.exitTime != null) {
                    writer.append("Exit: ${formatDateTime(session.exitTime)}\n")
                    writer.append("Duration: ${calculateDuration(session.entryTime, session.exitTime)}\n")
                } else {
                    writer.append("Status: Active\n")
                }
                if (session.notes.isNotEmpty()) {
                    writer.append("Notes: ${session.notes}\n")
                }
                if (session.tags.isNotEmpty()) {
                    writer.append("Tags: ${session.tags.joinToString(", ")}\n")
                }
                writer.append("\n")
            }
        }
        
        shareFile(context, file, "text/plain")
        callback(true, "Report generated successfully")
        
    } catch (e: Exception) {
        callback(false, "Export failed: ${e.message}")
    }
}

/**
 * Share session text
 */
private fun shareSessionText(context: Context, session: ParkingSession) {
    val text = buildString {
        append("🅿️ VaultPark Session Details\n\n")
        append("Gate: ${session.gateLocation}\n")
        append("Vehicle: ${session.vehicleNumber}\n")
        append("Entry: ${formatDateTime(session.entryTime)}\n")
        if (session.exitTime != null) {
            append("Exit: ${formatDateTime(session.exitTime)}\n")
            append("Duration: ${calculateDuration(session.entryTime, session.exitTime)}\n")
        } else {
            append("Status: Currently Active\n")
        }
        if (session.notes.isNotEmpty()) {
            append("\nNotes: ${session.notes}\n")
        }
        if (session.tags.isNotEmpty()) {
            append("Categories: ${session.tags.joinToString(", ")}\n")
        }
    }
    
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        putExtra(Intent.EXTRA_SUBJECT, "Parking Session - ${formatDate(session.entryTime)}")
    }
    context.startActivity(Intent.createChooser(intent, "Share Session"))
}

/**
 * Email session summary
 */
private fun emailSessionSummary(context: Context, sessions: List<ParkingSession>) {
    val summary = buildString {
        append("VaultPark Session Summary\n\n")
        append("Total Sessions: ${sessions.size}\n")
        append("Period: ${formatDate(sessions.minOfOrNull { it.entryTime } ?: 0L)} - ")
        append("${formatDate(sessions.maxOfOrNull { it.entryTime } ?: 0L)}\n\n")
        
        sessions.forEach { session ->
            append("• ${formatDate(session.entryTime)} - ${session.gateLocation}\n")
        }
    }
    
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "message/rfc822"
        putExtra(Intent.EXTRA_SUBJECT, "VaultPark Session Summary")
        putExtra(Intent.EXTRA_TEXT, summary)
    }
    context.startActivity(Intent.createChooser(intent, "Email Session Summary"))
}

/**
 * Generate receipt for session
 */
private fun generateReceipt(context: Context, session: ParkingSession) {
    val receipt = buildString {
        append("═══════════════════════════════════════\n")
        append("           PARKING RECEIPT\n")
        append("═══════════════════════════════════════\n\n")
        append("VaultPark Parking System\n\n")
        append("Receipt ID: ${session.id}\n")
        append("Date: ${formatDate(session.entryTime)}\n")
        append("Vehicle: ${session.vehicleNumber}\n\n")
        append("───────────────────────────────────────\n")
        append("Gate Location: ${session.gateLocation}\n")
        append("Entry Time: ${formatTime(session.entryTime)}\n")
        if (session.exitTime != null) {
            append("Exit Time: ${formatTime(session.exitTime)}\n")
            append("Duration: ${calculateDuration(session.entryTime, session.exitTime)}\n")
        }
        append("───────────────────────────────────────\n\n")
        append("Thank you for parking with VaultPark!\n")
        append("═══════════════════════════════════════\n")
    }
    
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, receipt)
        putExtra(Intent.EXTRA_SUBJECT, "Parking Receipt - ${formatDate(session.entryTime)}")
    }
    context.startActivity(Intent.createChooser(intent, "Share Receipt"))
}

/**
 * Email receipt
 */
private fun emailReceipt(context: Context, session: ParkingSession) {
    generateReceipt(context, session)
}

/**
 * Share file helper
 */
private fun shareFile(context: Context, file: File, mimeType: String) {
    val uri = androidx.core.content.FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share File"))
}

// ============ FORMATTING UTILITIES ============

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun calculateDuration(entryTime: Long, exitTime: Long): String {
    val durationMinutes = (exitTime - entryTime) / (1000 * 60)
    val hours = durationMinutes / 60
    val minutes = durationMinutes % 60
    return "${hours}h ${minutes}m"
}