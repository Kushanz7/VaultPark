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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.kushan.vaultpark.model.ParkingSession
import com.kushan.vaultpark.ui.components.FilterChip
import com.kushan.vaultpark.ui.components.SessionCard
import com.kushan.vaultpark.ui.components.SessionDetailBottomSheet
import com.kushan.vaultpark.ui.components.ShimmerCard
import com.kushan.vaultpark.ui.components.StatCard
import com.kushan.vaultpark.ui.components.StatsCardContainer
import com.kushan.vaultpark.ui.theme.DarkBackground
import com.kushan.vaultpark.ui.theme.DarkSurface
import com.kushan.vaultpark.ui.theme.NeonLime
import com.kushan.vaultpark.ui.theme.Poppins
import com.kushan.vaultpark.ui.theme.SoftMintGreen
import com.kushan.vaultpark.ui.theme.TextLight
import com.kushan.vaultpark.ui.utils.formatAmount
import com.kushan.vaultpark.viewmodel.HistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverHistoryScreen(
    onBackPressed: (() -> Unit)? = null,
    viewModel: HistoryViewModel = viewModel()
) {
    val parkingSessions by viewModel.parkingSessions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val totalSessions by viewModel.totalSessions.collectAsState()
    val totalHours by viewModel.totalHours.collectAsState()
    val thisMonthAmount by viewModel.thisMonthAmount.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var selectedSession by remember { mutableStateOf<ParkingSession?>(null) }
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
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = TextLight,
                    actionIconContentColor = TextLight,
                    navigationIconContentColor = TextLight
                )
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            // Stats Card
            item {
                StatsCardContainer(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 20.dp, bottom = 24.dp)
                ) {
                    StatCard(
                        icon = Icons.Default.Layers,
                        value = totalSessions.toString(),
                        label = "Total Sessions"
                    )
                    StatCard(
                        icon = Icons.Default.Timer,
                        value = "${totalHours}h",
                        label = "Total Hours"
                    )
                    StatCard(
                        icon = Icons.Default.LocalOffer,
                        value = formatAmount(thisMonthAmount),
                        label = "This Month"
                    )
                }
            }

            // Filter Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                ) {
                    Text(
                        text = "Filter",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = com.kushan.vaultpark.ui.theme.TextSecondaryDark,
                        modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HistoryViewModel.DateFilter.values().forEach { filter ->
                            FilterChip(
                                label = filter.displayName,
                                isSelected = filter == selectedFilter,
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

            // Sessions List or Loading State
            if (isLoading && parkingSessions.isEmpty()) {
                items(5) {
                    ShimmerCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
                }
            } else if (parkingSessions.isEmpty()) {
                item {
                    EmptyState(
                        title = "No Parking History",
                        description = "You haven't parked yet. Start parking to see your history here.",
                        icon = "🅿️"
                    )
                }
            } else {
                items(parkingSessions) { session ->
                    SessionCard(
                        session = session,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        onCardClick = { selectedSession = it }
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
                                    viewModel.loadMoreSessions(currentUser?.uid ?: "")
                                },
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .height(44.dp),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = NeonLime
                                ),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(
                                    text = "Load More",
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = com.kushan.vaultpark.ui.theme.TextDarkLight
                                )
                            }
                        }
                    }
                }

                if (isLoading && parkingSessions.isNotEmpty()) {
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
                            .background(
                                color = com.kushan.vaultpark.ui.theme.StatusError.copy(alpha = 0.1f),
                                shape = MaterialTheme.shapes.medium
                            )
                            .padding(16.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = com.kushan.vaultpark.ui.theme.StatusError
                        )
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
            containerColor = DarkBackground,
            scrimColor = com.kushan.vaultpark.ui.theme.MidnightBlack.copy(alpha = 0.5f)
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
}

@Composable
fun EmptyState(
    title: String,
    description: String,
    icon: String = ""
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                color = TextLight
            )
            Text(
                text = description,
                fontFamily = Poppins,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = com.kushan.vaultpark.ui.theme.TextSecondaryDark,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

// Extension for filter display name
val HistoryViewModel.DateFilter.displayName: String
    get() = when (this) {
        HistoryViewModel.DateFilter.ALL -> "All"
        HistoryViewModel.DateFilter.THIS_MONTH -> "This Month"
        HistoryViewModel.DateFilter.LAST_MONTH -> "Last Month"
        HistoryViewModel.DateFilter.CUSTOM_RANGE -> "Custom"
    }
