package com.kushan.vaultpark.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kushan.vaultpark.ui.theme.*
import com.kushan.vaultpark.viewmodel.QuickStats

// ============ ✨ STEP 3: QUICK ACTIONS WIDGET ============

/**
 * Quick Stats Widget - Shows at-a-glance information
 */
@Composable
fun QuickStatsWidget(
    quickStats: QuickStats,
    onViewLastSession: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            PrimaryPurple.copy(alpha = 0.05f),
                            SecondaryGold.copy(alpha = 0.05f)
                        )
                    )
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Quick Insights",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextLight
            )

            // Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickStatItem(
                    icon = Icons.Filled.CalendarToday,
                    label = "Last Parked",
                    value = if (quickStats.lastParkedDaysAgo == 0) "Today" 
                           else if (quickStats.lastParkedDaysAgo == 1) "Yesterday"
                           else "${quickStats.lastParkedDaysAgo} days ago",
                    iconTint = NeonLime,
                    modifier = Modifier.weight(1f)
                )

                QuickStatItem(
                    icon = Icons.Filled.Timer,
                    label = "Avg. Time",
                    value = quickStats.averageParkingTime,
                    iconTint = SecondaryGold,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickStatItem(
                    icon = Icons.Filled.LocationOn,
                    label = "Most Used",
                    value = quickStats.mostUsedGate,
                    iconTint = NeonLime,
                    modifier = Modifier.weight(1f)
                )

                QuickStatItem(
                    icon = Icons.Filled.TrendingUp,
                    label = "This Week",
                    value = "${quickStats.thisWeekVisits} visits",
                    iconTint = SecondaryGold,
                    modifier = Modifier.weight(1f)
                )
            }

            // View Last Session Button
            if (quickStats.lastParkedDaysAgo >= 0) {
                Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                TextButton(
                    onClick = onViewLastSession,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Visibility,
                            contentDescription = "View",
                            tint = NeonLime,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "View Last Session",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = NeonLime
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickStatItem(
    icon: ImageVector,
    label: String,
    value: String,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            
            Text(
                text = value,
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextLight,
                maxLines = 1
            )
            
            Text(
                text = label,
                fontFamily = Poppins,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = TextSecondaryDark,
                maxLines = 1
            )
        }
    }
}

/**
 * One-Tap Quick Actions Card
 */
@Composable
fun OneTapActionsCard(
    onParkNow: () -> Unit,
    onViewLastSession: () -> Unit,
    onPayBill: () -> Unit,
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Quick Actions",
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = TextLight
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionTile(
                    icon = Icons.Filled.LocalParking,
                    label = "Park Now",
                    color = PrimaryPurple,
                    onClick = onParkNow,
                    modifier = Modifier.weight(1f)
                )

                QuickActionTile(
                    icon = Icons.Filled.History,
                    label = "Last Session",
                    color = SecondaryGold,
                    onClick = onViewLastSession,
                    modifier = Modifier.weight(1f)
                )

                QuickActionTile(
                    icon = Icons.Filled.Payment,
                    label = "Pay Bill",
                    color = Color(0xFF4CAF50),
                    onClick = onPayBill,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun QuickActionTile(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = color,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

/**
 * Daily Parking Reminder Notification (represented as a card)
 */
@Composable
fun DailyReminderCard(
    isEnabled: Boolean,
    reminderTime: String,
    onToggle: (Boolean) -> Unit,
    onTimeChange: (String) -> Unit,
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Reminder",
                        tint = if (isEnabled) PrimaryPurple else TextTertiaryDark,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "Daily Parking Reminder",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = TextLight
                        )
                        Text(
                            text = "Get notified to park",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = TextSecondaryDark
                        )
                    }
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = PrimaryPurple,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            androidx.compose.animation.AnimatedVisibility(visible = isEnabled) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = "Time",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Every day at $reminderTime",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = PrimaryPurple
                    )
                }
            }
        }
    }
}

/**
 * Compact Action Buttons Row
 */
@Composable
fun CompactActionButtonsRow(
    onGenerateQR: () -> Unit,
    onViewHistory: () -> Unit,
    onViewBilling: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CompactActionButton(
            icon = Icons.Filled.QrCode,
            label = "QR Code",
            onClick = onGenerateQR,
            color = PrimaryPurple,
            modifier = Modifier.weight(1f)
        )

        CompactActionButton(
            icon = Icons.Filled.History,
            label = "History",
            onClick = onViewHistory,
            color = SecondaryGold,
            modifier = Modifier.weight(1f)
        )

        CompactActionButton(
            icon = Icons.Filled.Receipt,
            label = "Bills",
            onClick = onViewBilling,
            color = Color(0xFF4CAF50),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CompactActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color
        ),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = Color.White
            )
        }
    }
}

/**
 * Animated Parking Status Indicator
 */
@Composable
fun AnimatedParkingStatus(
    isParked: Boolean,
    duration: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "parking_status")
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha_animation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isParked) StatusActive.copy(alpha = 0.1f) 
                           else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .alpha(if (isParked) alpha else 1f)
                    .background(
                        color = if (isParked) StatusActive else TextTertiaryDark,
                        shape = CircleShape
                    )
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isParked) "Currently Parked" else "Not Parked",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = if (isParked) StatusActive else TextLight
                )
                if (isParked) {
                    Text(
                        text = "Duration: $duration",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )
                }
            }
        }
    }
}

/**
 * Parking Stats Summary Card (Alternative to Quick Stats Widget)
 */
@Composable
fun ParkingStatsSummaryCard(
    totalVisits: Int,
    totalHours: Double,
    totalAmount: Double,
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatsSummaryItem(
                icon = Icons.Filled.DirectionsCar,
                value = totalVisits.toString(),
                label = "Visits",
                color = PrimaryPurple
            )

            VerticalDivider(
                modifier = Modifier.height(50.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            )

            StatsSummaryItem(
                icon = Icons.Filled.AccessTime,
                value = String.format("%.1f", totalHours),
                label = "Hours",
                color = SecondaryGold
            )

            VerticalDivider(
                modifier = Modifier.height(50.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            )

            StatsSummaryItem(
                icon = Icons.Filled.AttachMoney,
                value = "$${String.format("%.0f", totalAmount)}",
                label = "Amount",
                color = Color(0xFF4CAF50)
            )
        }
    }
}

@Composable
private fun StatsSummaryItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = value,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = TextLight
        )
        Text(
            text = label,
            fontFamily = Poppins,
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
            color = TextSecondaryDark
        )
    }
}