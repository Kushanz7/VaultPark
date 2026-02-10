package com.kushan.vaultpark.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.kushan.vaultpark.model.ParkingSession
import com.kushan.vaultpark.ui.components.FilterChip
import com.kushan.vaultpark.ui.components.LogCard
import com.kushan.vaultpark.ui.components.LogDetailBottomSheet
import com.kushan.vaultpark.ui.components.MindMirrorCardElevated
import com.kushan.vaultpark.ui.components.ShimmerCard
import com.kushan.vaultpark.ui.components.StatCard
import com.kushan.vaultpark.ui.components.StatsCardContainer
import com.kushan.vaultpark.ui.theme.DarkBackground
import com.kushan.vaultpark.ui.theme.DarkSurface
import com.kushan.vaultpark.ui.theme.Poppins
import com.kushan.vaultpark.ui.theme.RoleTheme
import com.kushan.vaultpark.ui.theme.TextSecondaryDark
import com.kushan.vaultpark.ui.theme.SecurityPurple
import com.kushan.vaultpark.ui.theme.SecurityColorLight
import com.kushan.vaultpark.viewmodel.LogsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityLogsScreen(
    onBackPressed: (() -> Unit)? = null,
    viewModel: LogsViewModel = viewModel()
) {
    val scanLogs by viewModel.scanLogs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()
    val selectedDateFilter by viewModel.selectedDateFilter.collectAsState()
    val selectedScanTypeFilter by viewModel.selectedScanTypeFilter.collectAsState()
    val todayScansCount by viewModel.todayScansCount.collectAsState()
    val entriesCount by viewModel.entriesCount.collectAsState()
    val exitsCount by viewModel.exitsCount.collectAsState()
    val activeNowCount by viewModel.activeNowCount.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var selectedLog by remember { mutableStateOf<ParkingSession?>(null) }
    val bottomSheetState = rememberModalBottomSheetState()

    val currentUser = FirebaseAuth.getInstance().currentUser

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            viewModel.fetchScanLogs(currentUser.uid)
        }
    }

    val filteredLogs = viewModel.getFilteredLogs()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Scan Logs",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    )
                },
                navigationIcon = {
                    if (onBackPressed != null) {
                        IconButton(onClick = onBackPressed) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        val refreshState = rememberPullToRefreshState()

        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { 
                currentUser?.uid?.let { viewModel.fetchScanLogs(it) }
            },
            state = refreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 20.dp, bottom = 24.dp)
            ) {
                item {
                    MindMirrorCardElevated(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatCard(
                                icon = Icons.Default.QrCode,
                                value = todayScansCount.toString(),
                                label = "Today's Scans",
                                iconColor = SecurityColorLight,
                                valueColor = SecurityPurple
                            )
                            StatCard(
                                icon = Icons.Default.CallMade,
                                value = entriesCount.toString(),
                                label = "Entries",
                                iconColor = SecurityColorLight,
                                valueColor = SecurityPurple
                            )
                            StatCard(
                                icon = Icons.Default.CallReceived,
                                value = exitsCount.toString(),
                                label = "Exits",
                                iconColor = SecurityColorLight,
                                valueColor = SecurityPurple
                            )
                            StatCard(
                                icon = Icons.Default.Speed,
                                value = activeNowCount.toString(),
                                label = "Active Now",
                                iconColor = SecurityColorLight,
                                valueColor = SecurityPurple
                            )
                        }
                    }
                }

            // Filter Section - Date Filters
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Text(
                        text = "Date Filter",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LogsViewModel.DateFilter.values().forEach { filter ->
                            FilterChip(
                                label = filter.getDisplayName(),
                                isSelected = filter == selectedDateFilter,
                                useSecurity = true,
                                onClick = {
                                    viewModel.fetchScanLogs(
                                        currentUser?.uid ?: "",
                                        filter
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Filter Section - Scan Type Filters
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Text(
                        text = "Scan Type",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LogsViewModel.ScanTypeFilter.values().forEach { filter ->
                            FilterChip(
                                label = filter.getDisplayName(),
                                isSelected = filter == selectedScanTypeFilter,
                                useSecurity = true,
                                onClick = {
                                    viewModel.filterByScanType(filter)
                                }
                            )
                        }
                    }
                }
            }

            // Logs List or Loading State
            if (isLoading && scanLogs.isEmpty()) {
                items(5) {
                    ShimmerCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
                }
            } else if (filteredLogs.isEmpty()) {
                item {
                    LogsEmptyState(
                        title = "No Logs Found",
                        description = "No scan logs match your selected filters.",
                        icon = "📋"
                    )
                }
            } else {
                items(filteredLogs) { log ->
                    LogCard(
                        session = log,
                        isEntryOnly = selectedScanTypeFilter == LogsViewModel.ScanTypeFilter.ENTRY_ONLY,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        onCardClick = { selectedLog = it }
                    )
                }

                // Load More Button
                if (hasMore && !isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            androidx.compose.material3.Button(
                                onClick = {
                                    viewModel.loadMoreLogs(currentUser?.uid ?: "")
                                },
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .height(44.dp),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = RoleTheme.driverColor
                                ),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(
                                    text = "Load More",
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }

                if (isLoading && scanLogs.isNotEmpty()) {
                    items(3) {
                        ShimmerCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
                    }
                }
            }

            // Error Message
            if (!errorMessage.isNullOrEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .background(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = MaterialTheme.shapes.medium
                            )
                            .padding(16.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
    }

    // Detail Bottom Sheet
    if (selectedLog != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedLog = null },
            sheetState = bottomSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)
        ) {
            LogDetailBottomSheet(
                session = selectedLog!!,
                onDismiss = { selectedLog = null },
                onAddNote = { log ->
                    // Handle add note
                    selectedLog = null
                }
            )
        }
}
}

@Composable
fun LogsEmptyState(
    title: String,
    description: String,
    icon: String = ""
) {
    MindMirrorCardElevated(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = icon,
                fontSize = 64.sp
            )
            Text(
                text = title,
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                fontFamily = Poppins,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Extension for filter display name
fun LogsViewModel.DateFilter.getDisplayName(): String = when (this) {
    LogsViewModel.DateFilter.ALL -> "All"
    LogsViewModel.DateFilter.TODAY -> "Today"
    LogsViewModel.DateFilter.THIS_WEEK -> "This Week"
    LogsViewModel.DateFilter.THIS_MONTH -> "This Month"
}

fun LogsViewModel.ScanTypeFilter.getDisplayName(): String = when (this) {
    LogsViewModel.ScanTypeFilter.ALL_SCANS -> "All Scans"
    LogsViewModel.ScanTypeFilter.ENTRY_ONLY -> "Entry Only"
    LogsViewModel.ScanTypeFilter.EXIT_ONLY -> "Exit Only"
}

