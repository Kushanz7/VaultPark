package com.kushan.vaultpark.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kushan.vaultpark.model.*
import com.kushan.vaultpark.ui.theme.*
import com.kushan.vaultpark.viewmodel.AdminToolsViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

/**
 * STEP 3: Active Sessions Quick Actions Screen
 * Features:
 * - Manual Exit for any session
 * - Extend Time (grace period)
 * - Add Note / VIP flag
 * - Swipe actions
 * - Bulk actions
 * - Filter by long-stayers, VIP, etc.
 */
@Composable
fun ActiveSessionsScreen(
    viewModel: AdminToolsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showActionDialog by remember { mutableStateOf(false) }
    var selectedSession by remember { mutableStateOf<ParkingSession?>(null) }
    var selectedAction by remember { mutableStateOf<SessionAction?>(null) }
    var showBulkActionMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadActiveSessions()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header with Filter Chips
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
                Text(
                    text = "Active Sessions",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = TextLight
                )

                // Selection count badge
                if (uiState.selectedSessions.isNotEmpty()) {
                    Badge(
                        containerColor = PrimaryPurple
                    ) {
                        Text(
                            text = "${uiState.selectedSessions.size} selected",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActiveSessionFilter.values().forEach { filter ->
                    FilterChip(
                        selected = uiState.currentFilter == filter,
                        onClick = { viewModel.setFilter(filter) },
                        label = {
                            Text(
                                filter.displayName,
                                fontFamily = Poppins,
                                fontWeight = if (uiState.currentFilter == filter) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = {
                            if (uiState.currentFilter == filter) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    )
                }
            }
        }

        // Active Sessions List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "${uiState.filteredSessions.size} Active Sessions",
                    fontFamily = Poppins,
                    fontSize = 14.sp,
                    color = TextSecondaryDark,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(uiState.filteredSessions, key = { it.id }) { session ->
                SwipeableSessionCard(
                    session = session,
                    isSelected = uiState.selectedSessions.contains(session.id),
                    onToggleSelection = { viewModel.toggleSessionSelection(session.id) },
                    onActionClick = { action ->
                        selectedSession = session
                        selectedAction = action
                        showActionDialog = true
                    }
                )
            }

            if (uiState.filteredSessions.isEmpty()) {
                item {
                    EmptyActiveSessionsState(filter = uiState.currentFilter)
                }
            }
        }

        // Bottom Action Bar (when selections exist)
        AnimatedVisibility(
            visible = uiState.selectedSessions.isNotEmpty(),
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            BulkActionsBar(
                selectedCount = uiState.selectedSessions.size,
                onExitAll = {
                    viewModel.performBulkAction(BulkAction.EXIT_ALL, uiState.selectedSessions)
                },
                onExtendAll = {
                    viewModel.performBulkAction(BulkAction.EXTEND_ALL, uiState.selectedSessions)
                },
                onExport = {
                    viewModel.performBulkAction(BulkAction.EXPORT, uiState.selectedSessions)
                },
                isLoading = uiState.isPerformingBulkAction
            )
        }
    }

    // Action Dialog
    if (showActionDialog && selectedSession != null && selectedAction != null) {
        SessionActionDialog(
            session = selectedSession!!,
            action = selectedAction!!,
            onDismiss = {
                showActionDialog = false
                selectedSession = null
                selectedAction = null
            },
            onConfirm = { inputValue ->
                when (selectedAction) {
                    SessionAction.MANUAL_EXIT -> {
                        viewModel.manuallyExitSession(selectedSession!!.id, inputValue)
                    }
                    SessionAction.EXTEND_TIME -> {
                        viewModel.extendSession(selectedSession!!.id, 30, inputValue)
                    }
                    SessionAction.ADD_NOTE -> {
                        viewModel.addNoteToSession(selectedSession!!.id, inputValue, false)
                    }
                    SessionAction.MARK_VIP -> {
                        viewModel.addNoteToSession(selectedSession!!.id, inputValue, true)
                    }
                    SessionAction.FLAG_ISSUE -> {
                        viewModel.addNoteToSession(selectedSession!!.id, "ISSUE: $inputValue", false)
                    }
                    else -> {}
                }
                showActionDialog = false
                selectedSession = null
                selectedAction = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableSessionCard(
    session: ParkingSession,
    isSelected: Boolean,
    onToggleSelection: () -> Unit,
    onActionClick: (SessionAction) -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    val maxSwipeDistance = -200f

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Background Actions (revealed on swipe)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(end = 16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onActionClick(SessionAction.MANUAL_EXIT) }
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Manual Exit",
                    tint = StatusError,
                    modifier = Modifier.size(28.dp)
                )
            }

            IconButton(
                onClick = { onActionClick(SessionAction.EXTEND_TIME) }
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = "Extend Time",
                    tint = SecondaryGold,
                    modifier = Modifier.size(28.dp)
                )
            }

            IconButton(
                onClick = { onActionClick(SessionAction.ADD_NOTE) }
            ) {
                Icon(
                    imageVector = Icons.Default.Notes,
                    contentDescription = "Add Note",
                    tint = PrimaryPurple,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Foreground Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            offsetX = if (offsetX < maxSwipeDistance / 2) {
                                maxSwipeDistance
                            } else {
                                0f
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            val newOffset = offsetX + dragAmount
                            offsetX = newOffset.coerceIn(maxSwipeDistance, 0f)
                        }
                    )
                },
            onClick = onToggleSelection,
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) PrimaryPurple.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Selection Checkbox
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelection() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = PrimaryPurple
                    )
                )

                // Session Info
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = session.driverName,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextLight
                        )

                        // VIP Badge
                        if (session.notes.contains("VIP", ignoreCase = true)) {
                            Badge(containerColor = SecondaryGold) {
                                Text("VIP", fontSize = 10.sp)
                            }
                        }

                        // Issue Flag
                        if (session.notes.contains("ISSUE", ignoreCase = true)) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Issue",
                                tint = StatusError,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = session.vehicleNumber,
                        fontFamily = Poppins,
                        fontSize = 14.sp,
                        color = TextSecondaryDark
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = session.gateLocation,
                                fontFamily = Poppins,
                                fontSize = 12.sp,
                                color = PrimaryPurple
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = TextSecondaryDark,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = calculateParkingDuration(session.entryTime),
                                fontFamily = Poppins,
                                fontSize = 12.sp,
                                color = TextSecondaryDark
                            )
                        }
                    }

                    if (session.notes.isNotEmpty() && !session.notes.contains("VIP") && !session.notes.contains("ISSUE")) {
                        Text(
                            text = session.notes.take(50) + if (session.notes.length > 50) "..." else "",
                            fontFamily = Poppins,
                            fontSize = 11.sp,
                            color = TextSecondaryDark,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Quick Actions Menu
                var showMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Actions",
                            tint = TextSecondaryDark
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Manual Exit", fontFamily = Poppins) },
                            onClick = {
                                showMenu = false
                                onActionClick(SessionAction.MANUAL_EXIT)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.ExitToApp, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Extend Time", fontFamily = Poppins) },
                            onClick = {
                                showMenu = false
                                onActionClick(SessionAction.EXTEND_TIME)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.AccessTime, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Add Note", fontFamily = Poppins) },
                            onClick = {
                                showMenu = false
                                onActionClick(SessionAction.ADD_NOTE)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Notes, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Mark VIP", fontFamily = Poppins) },
                            onClick = {
                                showMenu = false
                                onActionClick(SessionAction.MARK_VIP)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Star, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Flag Issue", fontFamily = Poppins) },
                            onClick = {
                                showMenu = false
                                onActionClick(SessionAction.FLAG_ISSUE)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Warning, contentDescription = null)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BulkActionsBar(
    selectedCount: Int,
    onExitAll: () -> Unit,
    onExtendAll: () -> Unit,
    onExport: () -> Unit,
    isLoading: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$selectedCount",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = PrimaryPurple
            )

            FilledTonalButton(
                onClick = onExitAll,
                enabled = !isLoading,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = StatusError.copy(alpha = 0.2f),
                    contentColor = StatusError
                )
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Exit All", fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            }

            FilledTonalButton(
                onClick = onExtendAll,
                enabled = !isLoading,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = SecondaryGold.copy(alpha = 0.2f),
                    contentColor = SecondaryGold
                )
            ) {
                Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Extend", fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            }

            FilledTonalButton(
                onClick = onExport,
                enabled = !isLoading,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = PrimaryPurple.copy(alpha = 0.2f),
                    contentColor = PrimaryPurple
                )
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Export", fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun SessionActionDialog(
    session: ParkingSession,
    action: SessionAction,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = when (action) {
                    SessionAction.MANUAL_EXIT -> Icons.Default.ExitToApp
                    SessionAction.EXTEND_TIME -> Icons.Default.AccessTime
                    SessionAction.ADD_NOTE -> Icons.Default.Notes
                    SessionAction.MARK_VIP -> Icons.Default.Star
                    SessionAction.FLAG_ISSUE -> Icons.Default.Warning
                },
                contentDescription = null,
                tint = PrimaryPurple,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                action.displayName,
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "${session.driverName} - ${session.vehicleNumber}",
                    fontFamily = Poppins,
                    fontSize = 14.sp,
                    color = TextSecondaryDark
                )

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = {
                        Text(
                            when (action) {
                                SessionAction.MANUAL_EXIT -> "Exit Reason (optional)"
                                SessionAction.EXTEND_TIME -> "Extension Reason"
                                else -> "Note"
                            },
                            fontFamily = Poppins
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(inputText) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryPurple
                )
            ) {
                Text("Confirm", fontFamily = Poppins, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontFamily = Poppins)
            }
        }
    )
}

@Composable
private fun EmptyActiveSessionsState(filter: ActiveSessionFilter) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.EventBusy,
            contentDescription = null,
            tint = TextSecondaryDark,
            modifier = Modifier.size(64.dp)
        )

        Text(
            text = when (filter) {
                ActiveSessionFilter.ALL -> "No Active Sessions"
                ActiveSessionFilter.LONG_STAY -> "No Long-Stay Sessions"
                ActiveSessionFilter.VIP -> "No VIP Sessions"
                ActiveSessionFilter.FLAGGED -> "No Flagged Sessions"
                ActiveSessionFilter.MANUAL_ENTRY -> "No Manual Entries"
            },
            fontFamily = Poppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = TextLight
        )

        Text(
            text = "All clear! No sessions match this filter.",
            fontFamily = Poppins,
            fontSize = 14.sp,
            color = TextSecondaryDark
        )
    }
}

private fun calculateParkingDuration(entryTime: Long): String {
    val durationMillis = System.currentTimeMillis() - entryTime
    val hours = (durationMillis / (1000 * 60 * 60)).toInt()
    val minutes = ((durationMillis % (1000 * 60 * 60)) / (1000 * 60)).toInt()
    return "${hours}h ${minutes}m"
}
