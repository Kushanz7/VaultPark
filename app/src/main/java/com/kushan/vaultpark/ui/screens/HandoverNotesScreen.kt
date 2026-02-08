package com.kushan.vaultpark.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kushan.vaultpark.model.*
import com.kushan.vaultpark.ui.theme.*
import com.kushan.vaultpark.viewmodel.AdminToolsViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * STEP 4: Guard Handover Notes Screen
 * Features:
 * - Add shift notes/handover messages
 * - View notes from previous shift
 * - Flag important info (VIP arrivals, etc.)
 * - Attach notes to specific sessions
 * - Mark as read/acknowledged
 * - Chronological log
 */
@Composable
fun HandoverNotesScreen(
    viewModel: AdminToolsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateNoteDialog by remember { mutableStateOf(false) }
    var expandedNoteId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadInitialData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Shift Handover",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Communication between shifts",
                        fontFamily = Poppins,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Unread badge
                if (uiState.unreadNotesCount > 0) {
                    Badge(
                        containerColor = StatusError
                    ) {
                        Text(
                            text = "${uiState.unreadNotesCount} new",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Create Note Button
            Button(
                onClick = { showCreateNoteDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RoleTheme.securityColor
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Add Handover Note",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        // Notes List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.shiftNotes.isEmpty()) {
                item {
                    EmptyHandoverNotesState()
                }
            } else {
                items(uiState.shiftNotes, key = { it.id }) { note ->
                    HandoverNoteCard(
                        note = note,
                        isExpanded = expandedNoteId == note.id,
                        onToggleExpand = {
                            expandedNoteId = if (expandedNoteId == note.id) null else note.id
                            if (expandedNoteId == note.id) {
                                viewModel.markNoteAsRead(note.id)
                            }
                        },
                        onAcknowledge = { viewModel.acknowledgeNote(note.id) },
                        currentGuardId = viewModel.uiState.value.currentShiftReport?.guardId ?: ""
                    )
                }
            }
        }
    }

    // Create Note Dialog
    if (showCreateNoteDialog) {
        CreateHandoverNoteDialog(
            onDismiss = { showCreateNoteDialog = false },
            onCreate = { title, message, type, priority, expiresInHours ->
                viewModel.createShiftNote(
                    title = title,
                    message = message,
                    type = type,
                    priority = priority,
                    expiresInHours = expiresInHours
                )
                showCreateNoteDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HandoverNoteCard(
    note: ShiftNote,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onAcknowledge: () -> Unit,
    currentGuardId: String
) {
    val isUnread = !note.readBy.contains(currentGuardId)
    val isAcknowledged = note.acknowledgedBy.contains(currentGuardId)
    val priorityColor = Color(android.graphics.Color.parseColor(note.priority.colorHex))

    Card(
        onClick = onToggleExpand,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnread)
                priorityColor.copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isUnread) 4.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Type Icon
                    Surface(
                        color = priorityColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = note.type.icon,
                                fontSize = 20.sp
                            )
                        }
                    }

                    // Title and metadata
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = note.title,
                                fontFamily = Poppins,
                                fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (isUnread) {
                                Badge(
                                    containerColor = priorityColor
                                ) {
                                    Text("NEW", fontSize = 10.sp)
                                }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = note.guardName,
                                fontFamily = Poppins,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text("•", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = formatNoteTime(note.createdAt),
                                fontFamily = Poppins,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Priority Badge
                Surface(
                    color = priorityColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = note.priority.displayName,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        color = priorityColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Collapsed: Preview
            if (!isExpanded) {
                Text(
                    text = note.message.take(80) + if (note.message.length > 80) "..." else "",
                    fontFamily = Poppins,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Expanded: Full Content
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Divider()

                    // Type label
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Type:",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = note.type.displayName,
                            fontFamily = Poppins,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Full Message
                    Text(
                        text = note.message,
                        fontFamily = Poppins,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )

                    // Expiry warning
                    note.expiresAt?.let { expiry ->
                        if (expiry > System.currentTimeMillis()) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = SecondaryGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Expires: ${formatNoteTime(Date(expiry))}",
                                    fontFamily = Poppins,
                                    fontSize = 11.sp,
                                    color = SecondaryGold
                                )
                            }
                        }
                    }

                    // Read/Acknowledge Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Read by ${note.readBy.size}",
                            fontFamily = Poppins,
                            fontSize = 11.sp,
                            color = TextSecondaryDark
                        )
                        Text("•", fontSize = 11.sp, color = TextSecondaryDark)
                        Text(
                            text = "Acknowledged by ${note.acknowledgedBy.size}",
                            fontFamily = Poppins,
                            fontSize = 11.sp,
                            color = if (note.acknowledgedBy.isNotEmpty()) StatusActive else TextSecondaryDark
                        )
                    }

                    // Acknowledge Button
                    if (!isAcknowledged && note.priority != ShiftNotePriority.LOW) {
                        Button(
                            onClick = onAcknowledge,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StatusActive
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Acknowledge",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else if (isAcknowledged) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = StatusActive.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = StatusActive,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Acknowledged",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.SemiBold,
                                color = StatusActive
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateHandoverNoteDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, ShiftNoteType, ShiftNotePriority, Int?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ShiftNoteType.GENERAL) }
    var selectedPriority by remember { mutableStateOf(ShiftNotePriority.NORMAL) }
    var hasExpiry by remember { mutableStateOf(false) }
    var expiryHours by remember { mutableStateOf(24) }

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
                        Text(
                            text = "New Handover Note",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                // Title
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title", fontFamily = Poppins) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Message
                item {
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Message", fontFamily = Poppins) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp),
                        maxLines = 6,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Type Selection
                item {
                    Text(
                        "Note Type",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = TextSecondaryDark
                    )
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ShiftNoteType.values().forEach { type ->
                            FilterChip(
                                selected = selectedType == type,
                                onClick = { selectedType = type },
                                label = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(type.icon)
                                        Text(
                                            type.displayName,
                                            fontFamily = Poppins,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            )
                        }
                    }
                }

                // Priority Selection
                item {
                    Text(
                        "Priority",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = TextSecondaryDark
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ShiftNotePriority.values().forEach { priority ->
                            val color = Color(android.graphics.Color.parseColor(priority.colorHex))
                            FilterChip(
                                selected = selectedPriority == priority,
                                onClick = { selectedPriority = priority },
                                label = {
                                    Text(
                                        priority.displayName,
                                        fontFamily = Poppins,
                                        fontSize = 12.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = color.copy(alpha = 0.2f),
                                    selectedLabelColor = color
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Expiry Option
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Set expiry time",
                            fontFamily = Poppins,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Switch(
                            checked = hasExpiry,
                            onCheckedChange = { hasExpiry = it }
                        )
                    }
                }

                if (hasExpiry) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Expires in:", fontFamily = Poppins, fontSize = 14.sp)
                            
                            listOf(4, 8, 12, 24, 48).forEach { hours ->
                                FilterChip(
                                    selected = expiryHours == hours,
                                    onClick = { expiryHours = hours },
                                    label = {
                                        Text(
                                            "${hours}h",
                                            fontFamily = Poppins,
                                            fontSize = 12.sp
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                // Create Button
                item {
                    Button(
                        onClick = {
                            if (title.isNotEmpty() && message.isNotEmpty()) {
                                onCreate(
                                    title,
                                    message,
                                    selectedType,
                                    selectedPriority,
                                    if (hasExpiry) expiryHours else null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = title.isNotEmpty() && message.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RoleTheme.securityColor
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Create Handover Note",
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
private fun EmptyHandoverNotesState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.EventNote,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(64.dp)
        )

        Text(
            text = "No Handover Notes",
            fontFamily = Poppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Start a new shift note to communicate with other guards",
            fontFamily = Poppins,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

private fun formatNoteTime(date: Date?): String {
    if (date == null) return "Unknown"
    
    val now = System.currentTimeMillis()
    val timeMillis = date.time
    val diff = (now - timeMillis) / 1000

    return when {
        diff < 60 -> "Just now"
        diff < 3600 -> "${diff / 60}m ago"
        diff < 86400 -> "${diff / 3600}h ago"
        diff < 172800 -> "Yesterday"
        else -> SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(date)
    }
}
