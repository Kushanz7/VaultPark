package com.kushan.vaultpark.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.kushan.vaultpark.model.User
import com.kushan.vaultpark.ui.components.*
import com.kushan.vaultpark.ui.theme.*
import com.kushan.vaultpark.viewmodel.AdminUserManagementViewModel
import com.kushan.vaultpark.viewmodel.UserFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageUsersScreen(
    viewModel: AdminUserManagementViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDriverActions by remember { mutableStateOf(false) }
    var selectedUserForActions by remember { mutableStateOf<User?>(null) }

    // Show success/error messages
    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        uiState.successMessage?.let { /* Show success snackbar */ }
        uiState.errorMessage?.let { /* Show error snackbar */ }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Manage Users",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                // Statistics
                AnimatedVisibility(uiState.statistics.totalUsers > 0) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatCard(label = "Total", value = uiState.statistics.totalUsers.toString())
                        StatCard(label = "Drivers", value = uiState.statistics.totalDrivers.toString())
                        StatCard(label = "Security", value = uiState.statistics.totalSecurity.toString())
                        StatCard(label = "Active", value = uiState.statistics.activeUsers.toString())
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            ModernTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.searchDrivers(it) },
                placeholder = "Search by name, email, or vehicle number...",
                leadingIcon = Icons.Default.Search,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                items(UserFilter.values().toList()) { filter: UserFilter ->
                    FilterChip(
                        selected = uiState.selectedFilter == filter,
                        onClick = { viewModel.setFilter(filter) },
                        label = { Text(filter.name.replace("_", " ")) },
                        modifier = Modifier.clip(RoundedCornerShape(20.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User List
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = NeonLime)
                }
            } else if (uiState.filteredUsers.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.PersonOff,
                    title = if (uiState.searchQuery.isBlank()) "No users found" else "No users match your search",
                    subtitle = if (uiState.searchQuery.isBlank()) "Add your first user to get started" else "Try adjusting your search or filters"
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.filteredUsers) { user ->
                        DriverCard(
                            driver = user,
                            onTap = { viewModel.selectUser(user) },
                            onEdit = { viewModel.showEditUserDialog(user) },
                            onMoreActions = { 
                                selectedUserForActions = user
                                showDriverActions = true 
                            }
                        )
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { viewModel.showAddUserDialog() },
            containerColor = NeonLime,
            contentColor = MidnightBlack,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add User")
        }

        // User Actions Bottom Sheet
        if (showDriverActions && selectedUserForActions != null) {
            DriverActionsBottomSheet(
                driver = selectedUserForActions!!,
                onDismiss = { 
                    showDriverActions = false
                    selectedUserForActions = null
                },
                onViewDetails = { user ->
                    // Handle view details
                    showDriverActions = false
                },
                onEdit = { user ->
                    viewModel.showEditUserDialog(user)
                    showDriverActions = false
                },
                onResetPassword = { user ->
                    viewModel.resetDriverPassword(user.email)
                    showDriverActions = false
                },
                onToggleStatus = { user ->
                    if (user.isActive == true) {
                        viewModel.deactivateDriver(user.id)
                    } else {
                        viewModel.activateDriver(user.id)
                    }
                    showDriverActions = false
                },
                onDelete = { user ->
                    viewModel.showDeleteConfirmation(user)
                    showDriverActions = false
                }
            )
        }
    }

    // Add User Dialog
    if (uiState.showAddUserDialog) {
        AddDriverDialog(
            onDismiss = { viewModel.hideAddUserDialog() },
            onAddDriver = { fullName, email, phone, vehicleNumber, membershipType, password ->
                viewModel.createNewUser(fullName, email, phone, vehicleNumber, membershipType, password)
            },
            isLoading = uiState.isLoading
        )
    }

    // Edit User Dialog
    if (uiState.showEditUserDialog && uiState.selectedUser != null) {
        EditDriverDialog(
            driver = uiState.selectedUser!!,
            onDismiss = { viewModel.hideEditUserDialog() },
            onUpdateDriver = { fullName, phone, vehicleNumber, membershipType ->
                viewModel.updateDriver(
                    userId = uiState.selectedUser!!.id,
                    fullName = fullName,
                    phone = phone,
                    vehicleNumber = vehicleNumber,
                    membershipType = membershipType
                )
            },
            isLoading = uiState.isLoading
        )
    }

    // Delete Confirmation Dialog
    if (uiState.showDeleteConfirmation && uiState.selectedUser != null) {
        DeleteDriverDialog(
            driver = uiState.selectedUser!!,
            onDismiss = { viewModel.hideDeleteConfirmation() },
            onConfirm = {
                viewModel.deleteDriver(
                    userId = uiState.selectedUser!!.id,
                    email = uiState.selectedUser!!.email
                )
            },
            isLoading = uiState.isLoading
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String
) {
    Card(
        modifier = Modifier.padding(horizontal = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NeonLime
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DriverCard(
    driver: User,
    onTap: () -> Unit,
    onEdit: () -> Unit,
    onMoreActions: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        onClick = onTap,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Photo
            if (driver.profileImageUrl != null) {
                AsyncImage(
                    model = driver.profileImageUrl,
                    contentDescription = "Profile photo",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(24.dp)),
                    error = rememberAsyncImagePainter(Icons.Default.Person)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profile photo",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Driver Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = driver.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = driver.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Vehicle Number
                    if (driver.vehicleNumber.isNotBlank()) {
                         InfoChip(
                            text = driver.vehicleNumber,
                            icon = Icons.Default.DirectionsCar
                        )
                    }
                   
                    // Role Chip (New)
                    RoleChip(role = driver.role.name)
                    
                    // Membership Type
                    if (driver.membershipType.isNotBlank()) {
                        MembershipChip(
                            membershipType = driver.membershipType
                        )
                    }
                    
                    // Status
                    StatusChip(
                        isActive = driver.isActive ?: false
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Actions
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onMoreActions) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More actions",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoChip(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RoleChip(
    role: String
) {
    val (backgroundColor, contentColor) = when (role) {
        "SECURITY" -> SecurityPurple to Color.White
        "DRIVER" -> NeonLime to MidnightBlack
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = role,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = contentColor
        )
    }
}


@Composable
private fun MembershipChip(
    membershipType: String
) {
    val (backgroundColor, contentColor) = when (membershipType.lowercase()) {
        "gold" -> StatusWarning to Color.White
        "platinum" -> PrimaryPurple to Color.White
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = membershipType,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = contentColor
        )
    }
}

@Composable
private fun StatusChip(
    isActive: Boolean
) {
    val (backgroundColor, contentColor) = if (isActive) {
        StatusSuccess to Color.White
    } else {
        Color.Gray to Color.White
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isActive) "Active" else "Inactive",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = contentColor
        )
    }
}