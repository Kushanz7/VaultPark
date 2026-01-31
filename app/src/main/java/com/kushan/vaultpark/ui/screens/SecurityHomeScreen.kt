package com.kushan.vaultpark.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.kushan.vaultpark.ui.components.DashboardStatCard
import com.kushan.vaultpark.ui.components.ManualEntryDialog
import com.kushan.vaultpark.ui.components.OfflineBannerComponent
import com.kushan.vaultpark.ui.components.QuickActionButton
import com.kushan.vaultpark.ui.components.RecentActivityItem
import com.kushan.vaultpark.ui.components.ScannerBottomSheet
import com.kushan.vaultpark.ui.components.StatCardSkeleton
import com.kushan.vaultpark.ui.theme.DarkBackground
import com.kushan.vaultpark.ui.theme.Poppins
import com.kushan.vaultpark.ui.theme.PrimaryPurple
import com.kushan.vaultpark.ui.theme.SecondaryGold
import com.kushan.vaultpark.ui.theme.StatusActive
import com.kushan.vaultpark.ui.theme.TextLight
import com.kushan.vaultpark.ui.theme.TextSecondaryDark
import com.kushan.vaultpark.ui.theme.StatusError
import com.kushan.vaultpark.viewmodel.SecurityHomeViewModel
import com.kushan.vaultpark.viewmodel.AdminToolsViewModel
import com.kushan.vaultpark.ui.components.EnhancedManualEntryDialog
import com.kushan.vaultpark.ui.components.EndShiftButton
import com.kushan.vaultpark.ui.components.ShiftReportDialog
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.EventNote
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Badge
import androidx.compose.ui.unit.offset
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityHomeScreen(
    onNavigateToLogs: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToActiveSessions: () -> Unit = {},
    onNavigateToHandover: () -> Unit = {},
    viewModel: SecurityHomeViewModel = viewModel(),
    adminViewModel: AdminToolsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val adminState by adminViewModel.uiState.collectAsState()
    var showManualEntryDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

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
                // Guard Header
                GuardDashboardHeader(
                    guardName = uiState.guard?.name ?: "Guard",
                    gateLocation = uiState.assignedGate,
                    currentTime = getCurrentTime()
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                // Scan Action Card
                ScanActionCard(
                    onOpenScanner = { viewModel.showScannerDialog() }
                )

                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                // Today's Statistics Grid
                if (uiState.isLoadingStats) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        repeat(4) { StatCardSkeleton() }
                    }
                } else {
                    TodayStatsGrid(
                        totalScans = uiState.todayStats.totalScans.toString(),
                        activeCount = uiState.todayStats.activeCount.toString(),
                        entries = uiState.todayStats.entriesCount.toString(),
                        exits = uiState.todayStats.exitsCount.toString()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                // Activity Chart Section
                ActivityChartSection(
                    hourlyData = uiState.hourlyActivityData
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            if (uiState.recentScans.isNotEmpty()) {
                item {
                    // Recent Scans Section
                    RecentScansSection(
                        scans = uiState.recentScans,
                        onViewAll = onNavigateToLogs
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            item {
                // Quick Actions Row
                QuickActionsRowSecurity(
                    onLogsTap = onNavigateToLogs,
                    onReportsTap = onNavigateToReports,
                    onManualEntry = { showManualEntryDialog = true },
                    onActiveSessions = onNavigateToActiveSessions,
                    onHandover = onNavigateToHandover,
                    unreadNotesCount = adminState.unreadNotesCount
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
            
            item {
                 EndShiftButton(
                    onClick = {
                        adminViewModel.generateShiftReport()
                        showReportDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Error Banner
        if (uiState.error != null) {
            OfflineBannerComponent()
        }
    }

    // Scanner Bottom Sheet
    if (uiState.isShowScannerDialog) {
        ScannerBottomSheet(
            selectedGate = uiState.assignedGate,
            recentScans = uiState.recentScans,
            onGateSelected = {},
            onScan = { qrData ->
                viewModel.hideScannerDialog()
                // Process scanned data
            },
            onDismiss = { viewModel.hideScannerDialog() }
        )
    }

    // Manual Entry Dialog
    // Manual Entry Dialog
    if (showManualEntryDialog) {
        EnhancedManualEntryDialog(
            onDismiss = { showManualEntryDialog = false }
        )
    }
    
    // Shift Report Dialog
    if (showReportDialog && adminState.currentShiftReport != null) {
        ShiftReportDialog(
            report = adminState.currentShiftReport!!,
            onDismiss = { showReportDialog = false },
            onShare = { reportText ->
                 val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, reportText)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, "Share Shift Report")
                context.startActivity(shareIntent)
            }
        )
    }
}

/**
 * Guard Dashboard Header with gradient background
 */
@Composable
private fun GuardDashboardHeader(
    guardName: String,
    gateLocation: String,
    currentTime: String
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
            text = "Guard Dashboard",
            fontFamily = Poppins,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = guardName,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gate Location
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = "Gate",
                    tint = PrimaryPurple,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = gateLocation,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = PrimaryPurple
                )
            }

            // Current Time
            Text(
                text = currentTime,
                fontFamily = Poppins,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = TextSecondaryDark
            )
        }
    }
}

/**
 * Scan Action Hero Card
 */
@Composable
private fun ScanActionCard(onOpenScanner: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = PrimaryPurple
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.QrCode,
                contentDescription = "QR Scan",
                modifier = Modifier.size(80.dp),
                tint = Color.White
            )

            Text(
                text = "Ready to Scan",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color.White
            )

            Text(
                text = "Tap to open scanner",
                fontFamily = Poppins,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )

            Button(
                onClick = onOpenScanner,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "Open Scanner",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = PrimaryPurple
                )
            }
        }
    }
}

/**
 * Today's Statistics Grid (2x2)
 */
@Composable
private fun TodayStatsGrid(
    totalScans: String,
    activeCount: String,
    entries: String,
    exits: String
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
                icon = Icons.Filled.QrCode,
                value = totalScans,
                label = "Scans Today",
                valueColor = PrimaryPurple,
                trend = "↑ 5 from yesterday",
                trendColor = StatusActive
            )

            PulsingStatCard(
                modifier = Modifier.weight(1f),
                value = activeCount,
                label = "Cars Parked Now"
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardStatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Speed,
                value = entries,
                label = "Entries",
                valueColor = StatusActive
            )

            DashboardStatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.LocalFireDepartment,
                value = exits,
                label = "Exits",
                valueColor = StatusError
            )
        }
    }
}

/**
 * Pulsing Stat Card for active cars
 */
@Composable
private fun PulsingStatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_car")
    val _scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "pulse_scale_car"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
            .animateContentSize(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Speed,
            contentDescription = label,
            tint = SecondaryGold,
            modifier = Modifier
                .size(32.dp)
                .animateContentSize()
        )

        Text(
            text = value,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = SecondaryGold
        )

        Text(
            text = label,
            fontFamily = Poppins,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = TextSecondaryDark
        )
    }
}

/**
 * Activity Chart Section
 */
@Composable
private fun ActivityChartSection(
    hourlyData: List<com.kushan.vaultpark.viewmodel.HourlyActivityData>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Activity Last 6 Hours",
            fontFamily = Poppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = TextLight
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            if (hourlyData.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    val maxScans = (hourlyData.maxOfOrNull { it.scans } ?: 1).coerceAtLeast(1)
                    hourlyData.forEach { data ->
                        val barHeight = ((data.scans.toFloat() / maxScans) * 120f).dp
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(120.dp),
                            verticalArrangement = Arrangement.Bottom,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(barHeight)
                                    .background(
                                        color = PrimaryPurple,
                                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${data.hour}h",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Normal,
                                fontSize = 8.sp,
                                color = TextSecondaryDark
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Recent Scans Section
 */
@Composable
private fun RecentScansSection(
    scans: List<com.kushan.vaultpark.model.ParkingSession>,
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
                text = "Recent Scans",
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
            scans.take(5).forEach { scan ->
                RecentActivityItem(
                    date = formatTimestamp(scan.entryTime),
                    gateLocation = scan.gateLocation,
                    duration = if (scan.exitTime != null) "Exit" else "Entry",
                    amount = scan.driverName
                )
            }
        }
    }
}

/**
 * Quick Actions Row for Security
 */
@Composable
private fun QuickActionsRowSecurity(
    onLogsTap: () -> Unit,
    onReportsTap: () -> Unit,
    onManualEntry: () -> Unit,
    onActiveSessions: () -> Unit,
    onHandover: () -> Unit,
    unreadNotesCount: Int = 0
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
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
             QuickActionButton(
                icon = Icons.Filled.Receipt,
                label = "Manual Entry",
                onClick = onManualEntry
            )
            
            QuickActionButton(
                icon = Icons.Filled.ManageAccounts,
                label = "Active Sessions",
                onClick = onActiveSessions
            )
            
            Box {
                QuickActionButton(
                    icon = Icons.Filled.EventNote,
                    label = "Handover",
                    onClick = onHandover
                )
                
                if (unreadNotesCount > 0) {
                    Badge(
                        containerColor = StatusError,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-4).dp, y = 4.dp)
                    ) {
                        Text("$unreadNotesCount")
                    }
                }
            }
            
            QuickActionButton(
                icon = Icons.Filled.HistoryEdu,
                label = "View Logs",
                onClick = onLogsTap
            )

            QuickActionButton(
                icon = Icons.Filled.Assessment,
                label = "Reports",
                onClick = onReportsTap
            )
        }
    }
}

// Utility Functions
private fun getCurrentTime(): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(java.util.Date())
}

private fun formatTimestamp(timeMillis: Long): String {
    val now = System.currentTimeMillis()
    val diff = (now - timeMillis) / 1000

    return when {
        diff < 60 -> "Now"
        diff < 3600 -> "${diff / 60} min ago"
        diff < 86400 -> "${diff / 3600} hour ago"
        else -> {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            sdf.format(java.util.Date(timeMillis))
        }
    }
}
