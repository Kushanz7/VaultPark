package com.kushan.vaultpark.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Help
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kushan.vaultpark.model.User
import com.kushan.vaultpark.ui.components.DashboardStatCard
import com.kushan.vaultpark.ui.components.ManualEntryDialog
import com.kushan.vaultpark.ui.components.OfflineBannerComponent
import com.kushan.vaultpark.ui.components.QRCodeDialog
import com.kushan.vaultpark.ui.components.QuickActionButton
import com.kushan.vaultpark.ui.components.RecentActivityItem
import com.kushan.vaultpark.ui.components.StatCardSkeleton
import com.kushan.vaultpark.ui.theme.DarkBackground
import com.kushan.vaultpark.ui.theme.Poppins
import com.kushan.vaultpark.ui.theme.PrimaryPurple
import com.kushan.vaultpark.ui.theme.SecondaryGold
import com.kushan.vaultpark.ui.theme.DarkSurface
import com.kushan.vaultpark.ui.theme.TextLight
import com.kushan.vaultpark.ui.theme.TextSecondaryDark
import com.kushan.vaultpark.ui.theme.TextTertiaryDark
import com.kushan.vaultpark.ui.theme.StatusActive
import com.kushan.vaultpark.viewmodel.DriverHomeViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale
import com.kushan.vaultpark.ui.components.*
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverHomeScreen(
    onNavigateToBilling: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToSupport: () -> Unit = {},
    viewModel: DriverHomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSetFavoriteDialog by remember { mutableStateOf(false) }

    // Fetch data on screen load
    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            viewModel.refreshAllData()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            item {
                // Welcome Header
                DriverWelcomeHeader(
                    userName = uiState.user?.name ?: "Guest",
                    vehicleNumber = uiState.user?.vehicleNumber ?: "XX-0000",
                    membershipType = uiState.user?.membershipType ?: "Standard"
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                // Active Parking Status Card
                ActiveParkingCard(
                    isParked = uiState.activeSession != null,
                    gateLocation = uiState.activeSession?.gateLocation ?: "",
                    entryTime = uiState.activeSession?.entryTime ?: 0L,
                    durationMinutes = uiState.sessionDurationMinutes,
                    onGenerateQR = { viewModel.showQRDialog() }
                )

            }

            item {
                // Favorite Gate Card
                FavoriteGateCard(
                    favoriteGate = uiState.favoriteGate,
                    favoriteGateNote = uiState.favoriteGateNote,
                    onSetFavorite = { showSetFavoriteDialog = true },
                    onGenerateQRForFavorite = { viewModel.generateQRForFavoriteGate() }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                // Recent Gates
                RecentGatesRow(
                    recentGates = uiState.recentGates,
                    onGateClick = { gate ->
                        // In a real app, this might pre-fill the QR generator or start a flow
                        // For now we can just show a toast or log it, or perhaps set it as favorite
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                // Quick Stats Widget
                QuickStatsWidget(
                    quickStats = uiState.quickStats,
                    onViewLastSession = onNavigateToHistory
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                // Last 3 Sessions
                LastThreeSessionsCard(
                    sessions = uiState.lastThreeSessions,
                    onViewSession = { /* Navigate to details - for now just history */ onNavigateToHistory() }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                // One-Tap Actions
                OneTapActionsCard(
                    onParkNow = { viewModel.showQRDialog() },
                    onViewLastSession = onNavigateToHistory,
                    onPayBill = onNavigateToBilling
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Error Banner
        if (uiState.error != null) {
            OfflineBannerComponent()
        }
    }

    // Set Favorite Gate Dialog
    if (showSetFavoriteDialog) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        SetFavoriteGateDialog(
            currentFavorite = uiState.favoriteGate,
            currentNote = uiState.favoriteGateNote,
            onDismiss = { showSetFavoriteDialog = false },
            onConfirm = { gate, note ->
                if (userId != null) {
                    viewModel.setFavoriteGate(userId, gate, note)
                }
            },
            onRemoveFavorite = if (uiState.favoriteGate != null) {
                { if (userId != null) viewModel.removeFavoriteGate(userId) }
            } else null
        )
    }

    // QR Code Dialog
    if (uiState.isShowQRDialog) {
        QRCodeDialog(
            qrCodeData = uiState.qrCodeData,
            qrCodeImageUrl = uiState.qrCodeImageUrl,
            onDismiss = { viewModel.hideQRDialog() },
            onRegenerateQR = { viewModel.generateQRCode() }
        )
    }
}

/**
 * Welcome Header with gradient background
 */
@Composable
private fun DriverWelcomeHeader(
    userName: String,
    vehicleNumber: String,
    membershipType: String
) {
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
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Welcome back,",
            fontFamily = Poppins,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = TextSecondaryDark
        )

        Text(
            text = userName,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = TextLight
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Membership Badge
            MembershipBadgePill(membershipType = membershipType)

            // Vehicle Number
            Text(
                text = vehicleNumber,
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = PrimaryPurple
            )
        }
    }
}

/**
 * Membership Badge Pill
 */
@Composable
private fun MembershipBadgePill(membershipType: String) {
    val badgeColor = when (membershipType?.uppercase()) {
        "PLATINUM" -> SecondaryGold
        "GOLD" -> Color(0xFFFF9800)
        else -> Color(0xFF9C27B0)
    }

    Row(
        modifier = Modifier
            .background(
                color = badgeColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "★",
            fontSize = 12.sp,
            color = badgeColor
        )
        Text(
            text = membershipType ?: "Standard",
            fontFamily = Poppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = badgeColor
        )
    }
}

/**
 * Active Parking Status Card
 */
@Composable
private fun ActiveParkingCard(
    isParked: Boolean,
    gateLocation: String,
    entryTime: Long,
    durationMinutes: Long,
    onGenerateQR: () -> Unit
) {
    Card(
        modifier = Modifier
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isParked) {
                // Parked Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PulsingDot()
                    Text(
                        text = "Currently Parked",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = StatusActive
                    )
                }

                // Gate Location
                Text(
                    text = gateLocation,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextLight
                )

                // Entry Time
                Text(
                    text = "Entered at " + formatTime(entryTime),
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = TextSecondaryDark
                )

                // Duration Counter
                Text(
                    text = formatDuration(durationMinutes),
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = PrimaryPurple
                )

                // Exit QR Button
                Button(
                    onClick = onGenerateQR,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryPurple
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.QrCode,
                            contentDescription = "QR Code",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Generate Exit QR",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                }
            } else {
                // Not Parked Status
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowRight,
                    contentDescription = "Not parked",
                    modifier = Modifier.size(64.dp),
                    tint = TextSecondaryDark
                )

                Text(
                    text = "Not Currently Parked",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = TextLight
                )

                Text(
                    text = "Generate QR code to enter parking",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = TextSecondaryDark
                )

                // Generate QR Button
                Button(
                    onClick = onGenerateQR,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryPurple
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.QrCode,
                            contentDescription = "QR Code",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Generate QR Code",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Quick Stats Grid (2x2)
 */
@Composable
private fun QuickStatsGrid(
    sessionsCount: String,
    totalHours: String,
    monthlyAmount: String,
    memberSince: String,
    onBillingTap: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardStatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.CalendarMonth,
                value = sessionsCount,
                label = "Visits This Month",
                valueColor = PrimaryPurple
            )

            DashboardStatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.LocalOffer,
                value = totalHours,
                label = "Total Hours",
                valueColor = SecondaryGold
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardStatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Receipt,
                value = monthlyAmount,
                label = "Current Bill",
                valueColor = PrimaryPurple,
                onTap = onBillingTap
            )

            DashboardStatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Star,
                value = memberSince,
                label = "Member Since",
                valueColor = TextLight
            )
        }
    }
}

/**
 * Recent Activity Section
 */
@Composable
private fun RecentActivitySection(
    sessions: List<com.kushan.vaultpark.model.ParkingSession>,
    onViewAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Activity",
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = TextLight
            )

            Button(
                onClick = onViewAll,
                modifier = Modifier.height(32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                )
            ) {
                Text(
                    text = "View All",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = PrimaryPurple
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sessions.take(3).forEach { session ->
                RecentActivityItem(
                    date = formatDate(session.entryTime),
                    gateLocation = session.gateLocation,
                    duration = formatSessionDuration(session.entryTime, session.exitTime),
                    amount = formatBillingAmount(session.entryTime, session.exitTime)
                )
            }
        }
    }
}

/**
 * Quick Actions Row
 */
@Composable
private fun QuickActionsRow(
    onBillingTap: () -> Unit,
    onHistoryTap: () -> Unit,
    onSupportTap: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Quick Actions",
            fontFamily = Poppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = TextLight,
            modifier = Modifier.padding(start = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                icon = Icons.Filled.AttachMoney,
                label = "Pay Bill",
                onClick = onBillingTap
            )

            QuickActionButton(
                icon = Icons.Filled.HistoryEdu,
                label = "History",
                onClick = onHistoryTap
            )

            QuickActionButton(
                icon = Icons.Filled.Help,
                label = "Support",
                onClick = onSupportTap
            )
        }
    }
}

/**
 * Pulsing Dot Animation
 */
@Composable
private fun PulsingDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = Modifier
            .size((8.dp.value * scale).dp)
            .background(
                color = StatusActive,
                shape = RoundedCornerShape(50)
            )
    )
}

// Utility Functions
private fun formatTime(timeMillis: Long): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(java.util.Date(timeMillis))
}

private fun formatDate(timeMillis: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return sdf.format(java.util.Date(timeMillis))
}

private fun formatDuration(minutes: Long): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return "${hours}h ${mins}m"
}

private fun formatSessionDuration(entryTime: Long, exitTime: Long?): String {
    val duration = ((exitTime ?: System.currentTimeMillis()) - entryTime) / (1000 * 60)
    return "${duration / 60}h ${duration % 60}m"
}

private fun formatBillingAmount(entryTime: Long, exitTime: Long?): String {
    val durationHours = ((exitTime ?: System.currentTimeMillis()) - entryTime) / (1000.0 * 60 * 60)
    val amount = durationHours * 50.0 // $50 per hour
    return "$" + String.format("%.2f", amount)
}
