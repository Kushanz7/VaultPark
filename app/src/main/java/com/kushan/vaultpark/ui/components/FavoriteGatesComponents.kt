package com.kushan.vaultpark.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.kushan.vaultpark.model.ParkingSession
import com.kushan.vaultpark.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

// ============ ✨ STEP 1: FAVORITE GATES & QUICK ACCESS ============

/**
 * Favorite Gate Card - Shows the driver's favorite gate with quick QR generation
 */
@Composable
fun FavoriteGateCard(
    favoriteGate: String?,
    favoriteGateNote: String?,
    onSetFavorite: () -> Unit,
    onGenerateQRForFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        if (favoriteGate != null) {
            // Has Favorite Gate
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Favorite",
                            tint = SecondaryGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Favorite Gate",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = TextSecondaryDark
                        )
                    }
                    
                    IconButton(
                        onClick = onSetFavorite,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit",
                            tint = PrimaryPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text = favoriteGate,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextLight
                )

                if (!favoriteGateNote.isNullOrEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Note",
                            tint = TextTertiaryDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = favoriteGateNote,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            color = TextSecondaryDark,
                            lineHeight = 18.sp
                        )
                    }
                }

                // Quick QR Button
                Button(
                    onClick = onGenerateQRForFavorite,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryPurple
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.QrCode,
                            contentDescription = "QR",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Quick QR for $favoriteGate",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }
        } else {
            // No Favorite Gate Set
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .clickable { onSetFavorite() },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.StarBorder,
                    contentDescription = "Set Favorite",
                    tint = TextTertiaryDark,
                    modifier = Modifier.size(48.dp)
                )
                
                Text(
                    text = "Set Your Favorite Gate",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = TextLight
                )
                
                Text(
                    text = "Save time by marking your most-used parking gate",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = TextSecondaryDark,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

/**
 * Recent Gates Row - Shows recently used gates for quick access
 */
@Composable
fun RecentGatesRow(
    recentGates: List<String>,
    onGateClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (recentGates.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Recent Gates",
            fontFamily = Poppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = TextLight
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) {
            items(recentGates) { gate ->
                RecentGateChip(
                    gateName = gate,
                    onClick = { onGateClick(gate) }
                )
            }
        }
    }
}

@Composable
private fun RecentGateChip(
    gateName: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp,
        modifier = Modifier.height(40.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = PrimaryPurple,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = gateName,
                fontFamily = Poppins,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = TextLight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Last 3 Sessions Quick Access
 */
@Composable
fun LastThreeSessionsCard(
    sessions: List<ParkingSession>,
    onViewSession: (ParkingSession) -> Unit,
    modifier: Modifier = Modifier
) {
    if (sessions.isEmpty()) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Last 3 Parking Sessions",
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = TextLight
            )

            sessions.take(3).forEach { session ->
                QuickSessionItem(
                    session = session,
                    onClick = { onViewSession(session) }
                )
            }
        }
    }
}

@Composable
private fun QuickSessionItem(
    session: ParkingSession,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = session.gateLocation,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = TextLight
                )
                Text(
                    text = formatSessionDate(session.entryTime),
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = TextSecondaryDark
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatSessionDuration(session.entryTime, session.exitTime),
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = PrimaryPurple
                )
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "View",
                    tint = TextTertiaryDark,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Set Favorite Gate Dialog
 */
@Composable
fun SetFavoriteGateDialog(
    currentFavorite: String?,
    currentNote: String?,
    availableGates: List<String> = listOf(
        "Main Gate",
        "Exit Gate A",
        "Exit Gate B",
        "VIP Entrance",
        "East Wing Gate",
        "West Wing Gate"
    ),
    onDismiss: () -> Unit,
    onConfirm: (gate: String, note: String) -> Unit,
    onRemoveFavorite: (() -> Unit)? = null
) {
    var selectedGate by remember { mutableStateOf(currentFavorite ?: "") }
    var gateNote by remember { mutableStateOf(currentNote ?: "") }

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
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (currentFavorite != null) "Edit Favorite Gate" else "Set Favorite Gate",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TextLight
                    )
                    
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

                // Gate Selection
                Text(
                    text = "Select Gate",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = TextSecondaryDark
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableGates.forEach { gate ->
                        GateSelectionItem(
                            gateName = gate,
                            isSelected = selectedGate == gate,
                            onClick = { selectedGate = gate }
                        )
                    }
                }

                // Optional Note
                OutlinedTextField(
                    value = gateNote,
                    onValueChange = { gateNote = it },
                    label = {
                        Text(
                            "Add Note (Optional)",
                            fontFamily = Poppins,
                            fontSize = 14.sp
                        )
                    },
                    placeholder = {
                        Text(
                            "e.g., Closest to elevator",
                            fontFamily = Poppins,
                            fontSize = 13.sp,
                            color = TextTertiaryDark
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (currentFavorite != null && onRemoveFavorite != null) {
                        Arrangement.SpaceBetween
                    } else {
                        Arrangement.End
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Remove Button (if editing)
                    if (currentFavorite != null && onRemoveFavorite != null) {
                        TextButton(onClick = {
                            onRemoveFavorite()
                            onDismiss()
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Remove",
                                tint = Color(0xFFEF5350),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Remove",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = Color(0xFFEF5350)
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(
                                "Cancel",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = TextSecondaryDark
                            )
                        }

                        Button(
                            onClick = {
                                if (selectedGate.isNotEmpty()) {
                                    onConfirm(selectedGate, gateNote)
                                    onDismiss()
                                }
                            },
                            enabled = selectedGate.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryPurple
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Save",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GateSelectionItem(
    gateName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) PrimaryPurple.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
        border = if (isSelected) BorderStroke(2.dp, PrimaryPurple) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = if (isSelected) PrimaryPurple else TextSecondaryDark,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = gateName,
                    fontFamily = Poppins,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 14.sp,
                    color = if (isSelected) PrimaryPurple else TextLight
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Selected",
                    tint = PrimaryPurple,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// Utility Functions
private fun formatSessionDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatSessionDuration(entryTime: Long, exitTime: Long?): String {
    val duration = ((exitTime ?: System.currentTimeMillis()) - entryTime) / (1000 * 60)
    return if (duration < 60) {
        "${duration}m"
    } else {
        "${duration / 60}h ${duration % 60}m"
    }
}