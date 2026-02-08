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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.FileDownload
import kotlinx.coroutines.delay
import com.kushan.vaultpark.ui.components.FilterChip
import com.kushan.vaultpark.ui.components.SessionCard
import com.kushan.vaultpark.ui.components.SessionDetailBottomSheet
import com.kushan.vaultpark.ui.components.ShimmerCard
import com.kushan.vaultpark.ui.components.StatCard
import com.kushan.vaultpark.ui.components.StatsCardContainer
import com.kushan.vaultpark.ui.components.MindMirrorCard
import com.kushan.vaultpark.ui.components.MindMirrorCardElevated
import com.kushan.vaultpark.ui.components.TagFilterRow
import com.kushan.vaultpark.ui.components.MonthlyCategorySummaryCard
import com.kushan.vaultpark.ui.components.ExportOptionsDialog
import com.kushan.vaultpark.ui.components.AddSessionNotesDialog
import com.kushan.vaultpark.ui.components.ShareSessionSheet
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.kushan.vaultpark.model.ParkingSession
import com.kushan.vaultpark.ui.theme.NeonLime
import com.kushan.vaultpark.ui.theme.Poppins
import com.kushan.vaultpark.ui.utils.formatAmount
import com.kushan.vaultpark.viewmodel.HistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverHistoryScreen(
    onBackPressed: (() -> Unit)? = null,
    viewModel: HistoryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var selectedSession by remember { mutableStateOf<ParkingSession?>(null) }
    var selectedSessionToShare by remember { mutableStateOf<ParkingSession?>(null) }
    val bottomSheetState = rememberModalBottomSheetState()

    val currentUser = FirebaseAuth.getInstance().currentUser

    LaunchedEffect(Unit) {
        if (currentUser != null) {
            viewModel.fetchParkingSessions(currentUser.uid)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Parking History",
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
                    IconButton(onClick = { viewModel.showExportDialog() }) {
                        Icon(Icons.Default.FileDownload, "Export")
                    }
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
                isRefreshing = uiState.isLoading,
                onRefresh = { 
                    if (currentUser != null) {
                        viewModel.fetchParkingSessions(currentUser.uid)
                    }
                },
                state = refreshState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(bottom = 20.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
            // Stats Card
            item {
                MindMirrorCardElevated(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 20.dp, bottom = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                icon = Icons.Default.Layers,
                                value = uiState.totalSessions.toString(),
                                label = "Total Sessions",
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                icon = Icons.Default.Timer,
                                value = String.format("%.1f", uiState.totalHours) + "h",
                                label = "Total Hours",
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                icon = Icons.Default.LocalOffer,
                                value = formatAmount(uiState.thisMonthAmount),
                                label = "This Month",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Filter Section
            item {
                MindMirrorCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Filter by Date",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HistoryViewModel.DateFilter.values().forEach { filter ->
                                FilterChip(
                                    label = filter.getDisplayName(),
                                    isSelected = filter == uiState.selectedFilter,
                                    onClick = {
                                        viewModel.fetchParkingSessions(
                                            currentUser?.uid ?: "",
                                            filter
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Sessions List or Loading State
            if (uiState.isLoading && uiState.parkingSessions.isEmpty()) {
                items(5) {
                    ShimmerCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
                }
            } else if (uiState.filteredSessions.isEmpty() && uiState.parkingSessions.isEmpty()) {
                item {
                    HistoryEmptyState(
                        title = "No Parking History",
                        description = "You haven't parked yet. Start parking to see your history here.",
                        icon = "🅿️"
                    )
                }
            } else if (uiState.filteredSessions.isEmpty() && uiState.parkingSessions.isNotEmpty()) {
                item {
                    HistoryEmptyState(
                        title = "No Matches",
                        description = "No sessions match your selected filters.",
                        icon = "🔍"
                    )
                }
            } else {
                items(uiState.filteredSessions) { session ->
                    SessionCard(
                        session = session,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        onCardClick = { selectedSession = it },
                        onLongPress = { selectedSessionToShare = it }
                    )
                }

                // Load More Button
                if (uiState.hasMore && !uiState.isLoading) {
                    item {
                        MindMirrorCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            onClick = {
                                viewModel.loadMoreSessions(currentUser?.uid ?: "")
                            }
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Load More Sessions",
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = NeonLime
                                )
                            }
                        }
                    }
                }

                if (uiState.isLoading && uiState.parkingSessions.isNotEmpty()) {
                    items(3) {
                        ShimmerCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
                    }
                }
            }

            // Error Message
            if (!uiState.errorMessage.isNullOrEmpty()) {
                item {
                    MindMirrorCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = uiState.errorMessage ?: "",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            }
        }
    }

    // Detail Bottom Sheet
    if (selectedSession != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedSession = null },
            sheetState = bottomSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)
        ) {
            SessionDetailBottomSheet(
                session = selectedSession!!,
                onDismiss = { selectedSession = null },
                onReportIssue = { session ->
                    // Handle report issue
                    selectedSession = null
                }
            )
        }
    }

    // Add Share Sheet
    if (selectedSessionToShare != null) {
        ShareSessionSheet(
            session = selectedSessionToShare!!,
            onDismiss = { selectedSessionToShare = null }
        )
    }

    // Export Dialog
    if (uiState.showExportDialog) {
        ExportOptionsDialog(
            sessions = uiState.filteredSessions,
            onDismiss = { viewModel.hideExportDialog() }
        )
    }

    // Add Notes Dialog
    if (uiState.showAddNotesDialog && uiState.sessionToEdit != null) {
        AddSessionNotesDialog(
            onDismiss = { viewModel.hideAddNotesDialog() },
            onSave = { notes, tags ->
                viewModel.addSessionNotes(uiState.sessionToEdit!!.id, notes, tags)
            }
        )
    }
}

@Composable
fun HistoryEmptyState(
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
fun HistoryViewModel.DateFilter.getDisplayName(): String = when (this) {
    HistoryViewModel.DateFilter.ALL -> "All"
    HistoryViewModel.DateFilter.THIS_MONTH -> "This Month"
    HistoryViewModel.DateFilter.LAST_MONTH -> "Last Month"
}
