package com.kushan.vaultpark.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kushan.vaultpark.model.UserPreferences
import com.kushan.vaultpark.ui.components.ProfileDivider
import com.kushan.vaultpark.ui.components.ProfileField
import com.kushan.vaultpark.ui.components.ProfilePictureWithUpload
import com.kushan.vaultpark.ui.components.ProfileStatCard
import com.kushan.vaultpark.ui.components.SettingSwitch
import com.kushan.vaultpark.ui.theme.PrimaryPurple
import com.kushan.vaultpark.ui.theme.SecondaryGold
import com.kushan.vaultpark.ui.theme.StatusError
import com.kushan.vaultpark.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverProfileScreen(
    viewModel: ProfileViewModel,
    navController: NavController,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteConfirmation by remember { mutableStateOf("") }

    var editName by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var editVehicle by remember { mutableStateOf("") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadProfilePicture(it) }
    }

    LaunchedEffect(uiState.user) {
        if (uiState.user != null) {
            editName = uiState.user!!.name
            editPhone = uiState.user!!.phone
            editVehicle = uiState.user!!.vehicleNumber
        }
    }

    LaunchedEffect(error) {
        error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.clearError()
            }
        }
    }

    LaunchedEffect(successMessage) {
        successMessage?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.clearSuccess()
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out?", color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("Are you sure you want to sign out?", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                        onLogout()
                    }
                ) {
                    Text("Sign Out", color = StatusError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = PrimaryPurple)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Account?", color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column {
                    Text(
                        "This action cannot be undone. All your data will be permanently deleted.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    androidx.compose.material3.TextField(
                        value = deleteConfirmation,
                        onValueChange = { deleteConfirmation = it },
                        placeholder = { Text("Type DELETE to confirm") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        colors = androidx.compose.material3.TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (deleteConfirmation == "DELETE") {
                            showDeleteDialog = false
                            viewModel.deleteAccount()
                            onLogout()
                        }
                    },
                    enabled = deleteConfirmation == "DELETE"
                ) {
                    Text("Delete Account", color = StatusError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = PrimaryPurple)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", color = MaterialTheme.colorScheme.onSurface) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    if (!uiState.isEditMode) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier
                                .padding(16.dp)
                                .size(24.dp)
                                .clickable { viewModel.toggleEditMode(true) },
                            tint = PrimaryPurple
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Profile Header Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface)
                            ),
                            shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ProfilePictureWithUpload(
                            imageUrl = uiState.user?.let { "profileImageUrl" },
                            initials = uiState.user?.name?.take(2)?.uppercase() ?: "DR",
                            isUploading = uiState.uploadProgress,
                            onUploadClick = { imagePickerLauncher.launch("image/*") }
                        )

                        Text(
                            text = uiState.user?.name ?: "Driver",
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp)
                        )

                        Text(
                            text = uiState.user?.email ?: "",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Box(
                            modifier = Modifier
                                .background(
                                    color = SecondaryGold.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${uiState.user?.membershipType} Member",
                                fontSize = 12.sp,
                                color = SecondaryGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Personal Information Card
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
                        .padding(20.dp)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Personal Information",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )

                    ProfileField(
                        icon = Icons.Filled.Person,
                        label = "Full Name",
                        value = editName,
                        isEditing = uiState.isEditMode,
                        onValueChange = { editName = it }
                    )

                    ProfileDivider()

                    ProfileField(
                        icon = Icons.Filled.Email,
                        label = "Email",
                        value = uiState.user?.email ?: "",
                        isEditing = false,
                        readOnly = true
                    )

                    ProfileDivider()

                    ProfileField(
                        icon = Icons.Filled.Call,
                        label = "Phone",
                        value = editPhone,
                        isEditing = uiState.isEditMode,
                        onValueChange = { editPhone = it }
                    )

                    ProfileDivider()

                    ProfileField(
                        icon = Icons.Filled.DirectionsCar,
                        label = "Vehicle Number",
                        value = editVehicle,
                        isEditing = uiState.isEditMode,
                        onValueChange = { editVehicle = it }
                    )

                    ProfileDivider()

                    ProfileField(
                        icon = Icons.Filled.AttachMoney,
                        label = "Membership Type",
                        value = uiState.user?.membershipType ?: "Standard",
                        isEditing = false,
                        readOnly = true
                    )

                    if (uiState.isEditMode) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.updateUserProfile(editName, editPhone, editVehicle)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryPurple
                                ),
                                enabled = !isLoading
                            ) {
                                Text("Save Changes")
                            }

                            TextButton(
                                onClick = { viewModel.toggleEditMode(false) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }



            // Theme Settings Card
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Appearance",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    
                    val currentThemeMode by viewModel.themeMode.collectAsState()
                    
                    // Theme Mode Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (currentThemeMode == "DARK") Icons.Filled.DarkMode else Icons.Filled.LightMode,
                                contentDescription = "Theme",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Column(modifier = Modifier.padding(start = 16.dp)) {
                                Text(
                                    text = "Theme Mode",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Current: $currentThemeMode",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    // Theme Options
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 40.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        com.kushan.vaultpark.ui.components.ThemeOptionChip(
                            label = "System",
                            isSelected = currentThemeMode == "SYSTEM",
                            onClick = { viewModel.setThemeMode("SYSTEM") }
                        )
                        
                        com.kushan.vaultpark.ui.components.ThemeOptionChip(
                            label = "Light",
                            isSelected = currentThemeMode == "LIGHT",
                            onClick = { viewModel.setThemeMode("LIGHT") }
                        )
                        
                        com.kushan.vaultpark.ui.components.ThemeOptionChip(
                            label = "Dark",
                            isSelected = currentThemeMode == "DARK",
                            onClick = { viewModel.setThemeMode("DARK") }
                        )
                    }
                }
            }

            // Preferences Card
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
                        .padding(16.dp)
                        .padding(top = 20.dp)
                ) {
                    Text(
                        text = "Notifications & Preferences",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    uiState.preferences?.let { prefs ->
                        SettingSwitch(
                            icon = Icons.Filled.NotificationsActive,
                            label = "Push Notifications",
                            isChecked = prefs.notificationsEnabled,
                            onCheckedChange = {
                                viewModel.updatePreferences(prefs.copy(notificationsEnabled = it))
                            }
                        )

                        SettingSwitch(
                            icon = Icons.Filled.NotificationsActive,
                            label = "Entry Alerts",
                            description = "Notify when entering parking",
                            isChecked = prefs.entryAlerts,
                            onCheckedChange = {
                                viewModel.updatePreferences(prefs.copy(entryAlerts = it))
                            },
                            enabled = prefs.notificationsEnabled
                        )

                        SettingSwitch(
                            icon = Icons.Filled.NotificationsActive,
                            label = "Exit Alerts",
                            description = "Notify when exiting parking",
                            isChecked = prefs.exitAlerts,
                            onCheckedChange = {
                                viewModel.updatePreferences(prefs.copy(exitAlerts = it))
                            },
                            enabled = prefs.notificationsEnabled
                        )

                        SettingSwitch(
                            icon = Icons.Filled.AttachMoney,
                            label = "Billing Reminders",
                            description = "Monthly bill notifications",
                            isChecked = prefs.billingReminders,
                            onCheckedChange = {
                                viewModel.updatePreferences(prefs.copy(billingReminders = it))
                            },
                            enabled = prefs.notificationsEnabled
                        )
                    }
                }
            }

            // Statistics Card
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
                        .padding(20.dp)
                        .padding(top = 20.dp)
                ) {
                    Text(
                        text = "My Stats",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ProfileStatCard(
                            icon = Icons.Filled.DirectionsCar,
                            value = "24",
                            label = "Total Visits",
                            modifier = Modifier.weight(1f)
                        )

                        ProfileStatCard(
                            icon = Icons.Filled.AttachMoney,
                            value = "48h",
                            label = "Total Hours",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ProfileStatCard(
                            icon = Icons.Filled.NotificationsActive,
                            value = "5",
                            label = "This Month",
                            modifier = Modifier.weight(1f)
                        )

                        ProfileStatCard(
                            icon = Icons.Filled.Person,
                            value = "6m",
                            label = "Member Since",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Account Management
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
                        .padding(16.dp)
                        .padding(top = 20.dp)
                ) {
                    Text(
                        text = "Account",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    listOf(
                        Triple(Icons.Filled.Lock, "Change Password", { navController.navigate("change_password") }),
                        Triple(Icons.Filled.AttachMoney, "Payment Methods", { navController.navigate("payment_methods") }),
                        Triple(Icons.Filled.Lock, "Privacy Policy", { navController.navigate("privacy_policy") }),
                        Triple(Icons.Filled.Lock, "Terms of Service", { navController.navigate("terms_of_service") }),
                        Triple(Icons.AutoMirrored.Filled.Help, "Help & Support", { navController.navigate("help_support") })
                    ).forEach { (icon, label, action) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { action() }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )

                                Text(
                                    text = label,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Delete Account (Danger Zone)
            item {
                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StatusError.copy(alpha = 0.1f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = StatusError,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 8.dp)
                    )
                    Text("Delete Account", color = StatusError)
                }
            }

            // Logout Button
            item {
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryPurple
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Logout",
                        tint = Color.White,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 8.dp)
                    )
                    Text("Sign Out", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            item {
                Box(modifier = Modifier.height(24.dp))
            }
        }
    }
}
