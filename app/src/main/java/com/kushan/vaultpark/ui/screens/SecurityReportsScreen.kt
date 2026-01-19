package com.kushan.vaultpark.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kushan.vaultpark.model.TopDriver
import com.kushan.vaultpark.ui.components.AnimatedMetricCard
import com.kushan.vaultpark.ui.components.ChartCard
import com.kushan.vaultpark.ui.components.DateRangeChip
import com.kushan.vaultpark.ui.components.DateRangePickerDialog
import com.kushan.vaultpark.ui.components.DailyTrendLineChart
import com.kushan.vaultpark.ui.components.EmptyStateCard
import com.kushan.vaultpark.ui.components.EntryExitRatioChart
import com.kushan.vaultpark.ui.components.ExportButton
import com.kushan.vaultpark.ui.components.HourlyBarChart
import com.kushan.vaultpark.ui.components.MetricCard
import com.kushan.vaultpark.ui.components.PulsingMetricCard
import com.kushan.vaultpark.ui.components.ShimmerLoadingCard
import com.kushan.vaultpark.ui.components.TopDriverItem
import com.kushan.vaultpark.ui.theme.DarkBackground
import com.kushan.vaultpark.ui.theme.PrimaryPurple
import com.kushan.vaultpark.ui.theme.SecondaryGold
import com.kushan.vaultpark.ui.theme.StatusSuccess
import com.kushan.vaultpark.ui.theme.TextLight
import com.kushan.vaultpark.ui.theme.TextSecondaryDark
import com.kushan.vaultpark.ui.theme.TextTertiaryDark
import com.kushan.vaultpark.viewmodel.DateRangeFilter
import com.kushan.vaultpark.viewmodel.ReportsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SecurityReportsScreen(
    viewModel: ReportsViewModel = viewModel(),
    onNavigateToDriverHistory: (String) -> Unit = {}
) {
    val selectedDateRange = viewModel.selectedDateRange.collectAsState().value
    val reportStats = viewModel.reportStats.collectAsState().value
    val hourlyData = viewModel.hourlyData.collectAsState().value
    val dailyTrendData = viewModel.dailyTrendData.collectAsState().value
    val topDrivers = viewModel.topDrivers.collectAsState().value
    val isLoading = viewModel.isLoading.collectAsState().value
    val activeNow = viewModel.activeNow.collectAsState().value
    
    val snackbarHostState = remember { SnackbarHostState() }
    val showDatePicker = remember { mutableStateOf(false) }
    val refreshRotation = remember { Animatable(0f) }
    
    LaunchedEffect(isLoading) {
        if (isLoading) {
            refreshRotation.animateTo(
                targetValue = 360f,
                animationSpec = infiniteRepeatable(tween(1000))
            )
        } else {
            refreshRotation.stop()
            refreshRotation.snapTo(0f)
        }
    }
    
    if (showDatePicker.value) {
        DateRangePickerDialog(
            onDateRangeSelected = { startTime, endTime ->
                viewModel.setCustomDateRange(startTime, endTime)
                showDatePicker.value = false
            },
            onDismiss = { showDatePicker.value = false }
        )
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground,
        contentColor = TextLight
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Analytics & Reports",
                            color = TextLight,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = reportStats.dateRange,
                            color = TextSecondaryDark,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                    
                    IconButton(
                        onClick = { viewModel.refreshData() },
                        modifier = Modifier
                            .size(40.dp)
                            .rotate(refreshRotation.value)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = PrimaryPurple,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = PrimaryPurple,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
            
            // Date Range Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DateRangeChip(
                        label = "Today",
                        isSelected = selectedDateRange == DateRangeFilter.TODAY,
                        onClick = { viewModel.selectDateRange(DateRangeFilter.TODAY) }
                    )
                    
                    DateRangeChip(
                        label = "This Week",
                        isSelected = selectedDateRange == DateRangeFilter.THIS_WEEK,
                        onClick = { viewModel.selectDateRange(DateRangeFilter.THIS_WEEK) }
                    )
                    
                    DateRangeChip(
                        label = "This Month",
                        isSelected = selectedDateRange == DateRangeFilter.THIS_MONTH,
                        onClick = { viewModel.selectDateRange(DateRangeFilter.THIS_MONTH) }
                    )
                    
                    DateRangeChip(
                        label = "Custom Range",
                        isSelected = selectedDateRange == DateRangeFilter.CUSTOM,
                        onClick = { showDatePicker.value = true }
                    )
                }
            }
            
            // Key Metrics Grid (2x2)
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Row 1: Total Scans and Entry/Exit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Total Scans Card
                        if (isLoading) {
                            ShimmerLoadingCard(modifier = Modifier.weight(1f))
                        } else {
                            MetricCard(
                                modifier = Modifier.weight(1f),
                                label = "Total Scans",
                                value = reportStats.totalScans.toString(),
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.QueryStats,
                                        contentDescription = "Scans",
                                        tint = PrimaryPurple,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                trend = "↑ 12% from last period",
                                trendColor = StatusSuccess
                            )
                        }
                        
                        // Entry/Exit Ratio Card
                        if (isLoading) {
                            ShimmerLoadingCard(modifier = Modifier.weight(1f), height = 200)
                        } else {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        color = androidx.compose.material3.MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .padding(20.dp)
                            ) {
                                EntryExitRatioChart(
                                    entries = reportStats.totalEntries,
                                    exits = reportStats.totalExits,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                    
                    // Row 2: Average Duration and Active Now
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Average Duration Card
                        if (isLoading) {
                            ShimmerLoadingCard(modifier = Modifier.weight(1f))
                        } else {
                            MetricCard(
                                modifier = Modifier.weight(1f),
                                label = "Avg Parking Time",
                                value = String.format("%.1f", reportStats.averageDuration) + "h",
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = "Duration",
                                        tint = SecondaryGold,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            )
                        }
                        
                        // Active Now Card (Pulsing)
                        if (isLoading) {
                            ShimmerLoadingCard(modifier = Modifier.weight(1f))
                        } else {
                            PulsingMetricCard(
                                modifier = Modifier.weight(1f),
                                label = "Cars Parked Now",
                                value = activeNow,
                                icon = {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(
                                                color = StatusSuccess,
                                                shape = RoundedCornerShape(50)
                                            )
                                    )
                                }
                            )
                        }
                    }
                }
            }
            
            // Busiest Hours Chart
            item {
                ChartCard(
                    title = "Peak Hours",
                    subtitle = "Scan activity by hour"
                ) {
                    if (isLoading) {
                        ShimmerLoadingCard(height = 250)
                    } else if (hourlyData.isEmpty()) {
                        EmptyStateCard(message = "No hourly data available")
                    } else {
                        HourlyBarChart(hourlyData)
                    }
                }
            }
            
            // Daily Trend Chart
            item {
                ChartCard(
                    title = "Scan Trend",
                    subtitle = "Last 7/30 days"
                ) {
                    if (isLoading) {
                        ShimmerLoadingCard(height = 200)
                    } else if (dailyTrendData.isEmpty()) {
                        EmptyStateCard(message = "No trend data available")
                    } else {
                        DailyTrendLineChart(dailyTrendData)
                    }
                }
            }
            
            // Most Frequent Parkers
            item {
                ChartCard(
                    title = "Most Frequent Parkers",
                    subtitle = "Top 5 this period"
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isLoading) {
                            repeat(3) {
                                ShimmerLoadingCard(height = 80)
                            }
                        } else if (topDrivers.isEmpty()) {
                            EmptyStateCard(message = "No frequent parkers yet")
                        } else {
                            val maxVisits = topDrivers.maxOfOrNull { it.visitCount } ?: 1
                            topDrivers.forEachIndexed { index, driver ->
                                TopDriverItem(
                                    rank = index + 1,
                                    driver = driver,
                                    maxVisits = maxVisits,
                                    onItemClick = { onNavigateToDriverHistory(driver.driverId) }
                                )
                            }
                        }
                    }
                }
            }
            
            // Export Button
            item {
                val showExportMessage = remember { mutableStateOf(false) }
                
                ExportButton(
                    onClick = {
                        viewModel.exportReport()
                        showExportMessage.value = true
                    }
                )
                
                LaunchedEffect(showExportMessage.value) {
                    if (showExportMessage.value) {
                        snackbarHostState.showSnackbar(
                            message = "PDF export feature coming soon",
                            duration = SnackbarDuration.Short
                        )
                        showExportMessage.value = false
                    }
                }
            }
            
            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
