package com.kushan.vaultpark.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kushan.vaultpark.data.firestore.ProfileFirestoreQueries
import com.kushan.vaultpark.model.NotificationData
import com.kushan.vaultpark.ui.theme.DarkBackground
import com.kushan.vaultpark.ui.theme.DarkSurface
import com.kushan.vaultpark.ui.theme.PrimaryPurple
import com.kushan.vaultpark.ui.theme.SecondaryGold
import com.kushan.vaultpark.ui.theme.StatusError
import com.kushan.vaultpark.ui.theme.StatusSuccess
import com.kushan.vaultpark.ui.theme.TextLight
import com.kushan.vaultpark.ui.theme.TextSecondaryDark
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotificationsUiState(
    val notifications: List<NotificationData> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class NotificationsViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                val result = ProfileFirestoreQueries.getRecentNotifications(userId, limit = 50)
                
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        notifications = result.getOrNull() ?: emptyList(),
                        isLoading = false,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                ProfileFirestoreQueries.markNotificationAsRead(notificationId)
                // Reload notifications
                loadNotifications()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            try {
                ProfileFirestoreQueries.deleteNotification(notificationId)
                // Reload notifications
                loadNotifications()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadNotifications()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", color = TextLight) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface
                )
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        if (uiState.notifications.isEmpty() && !uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(DarkBackground),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "No notifications",
                        tint = TextSecondaryDark,
                        modifier = Modifier.size(64.dp)
                    )

                    Text(
                        text = "No Notifications",
                        fontSize = 20.sp,
                        color = TextLight,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    Text(
                        text = "You're all caught up! Check back later.",
                        fontSize = 14.sp,
                        color = TextSecondaryDark,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(DarkBackground)
            ) {
                items(uiState.notifications) { notification ->
                    NotificationItem(
                        notification = notification,
                        onMarkAsRead = {
                            viewModel.markAsRead(notification.id)
                        },
                        onDelete = {
                            viewModel.deleteNotification(notification.id)
                        },
                        onTap = {
                            // Navigate based on notification type
                            when (notification.type) {
                                "ENTRY", "EXIT" -> navController.navigate("history")
                                "BILLING" -> navController.navigate("billing")
                                else -> {}
                            }
                        }
                    )
                }

                item {
                    Box(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(
    notification: NotificationData,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit,
    onTap: () -> Unit
) {
    val (icon, iconColor) = when (notification.type) {
        "ENTRY" -> Icons.Filled.Check to StatusSuccess
        "EXIT" -> Icons.Filled.Check to StatusError
        "BILLING" -> Icons.Filled.AttachMoney to SecondaryGold
        else -> Icons.Filled.Notifications to PrimaryPurple
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (notification.isRead) DarkSurface else DarkSurface.copy(alpha = 0.8f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onTap() }
            .padding(16.dp)
            .padding(start = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Unread indicator
                if (!notification.isRead) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(PrimaryPurple, CircleShape)
                    )
                }

                // Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(iconColor.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = notification.type,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    Text(
                        text = notification.title,
                        fontSize = 16.sp,
                        color = TextLight,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = notification.message,
                        fontSize = 13.sp,
                        color = TextSecondaryDark,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Text(
                        text = formatTimestamp(notification.timestamp?.time ?: 0),
                        fontSize = 11.sp,
                        color = TextSecondaryDark.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            // Actions
            Row(
                modifier = Modifier.padding(start = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (!notification.isRead) {
                    Button(
                        onClick = onMarkAsRead,
                        modifier = Modifier
                            .size(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryPurple.copy(alpha = 0.2f)
                        ),
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Mark as read",
                            tint = PrimaryPurple,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Button(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StatusError.copy(alpha = 0.2f)
                    ),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "Delete",
                        tint = StatusError,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }

    Divider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        color = TextSecondaryDark.copy(alpha = 0.1f),
        thickness = 1.dp
    )
}

/**
 * Format notification timestamp
 */
private fun formatTimestamp(timestamp: Long): String {
    if (timestamp == 0L) return "Just now"

    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        diff < 604_800_000 -> "${diff / 86_400_000}d ago"
        else -> "${diff / 604_800_000}w ago"
    }
}
