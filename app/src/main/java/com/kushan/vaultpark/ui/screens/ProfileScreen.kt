package com.kushan.vaultpark.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kushan.vaultpark.viewmodel.ProfileViewModel
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kushan.vaultpark.model.User
import com.kushan.vaultpark.ui.components.MindMirrorCard
import com.kushan.vaultpark.ui.components.MindMirrorCard
import com.kushan.vaultpark.ui.theme.NeonLime
import com.kushan.vaultpark.ui.theme.Poppins
import com.kushan.vaultpark.ui.theme.RoleTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackPressed: (() -> Unit)? = null,
    currentUser: User? = null,
    onLogout: (() -> Unit)? = null,
    onNavigateToNotifications: (() -> Unit)? = null,
    onNavigateToChangePassword: (() -> Unit)? = null,
    onNavigateToDriverProfile: (() -> Unit)? = null,
    onNavigateToSecurityProfile: (() -> Unit)? = null,
    viewModel: ProfileViewModel = viewModel()
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val isDarkTheme = themeMode == "DARK" || (themeMode == "SYSTEM" && isSystemInDarkTheme())
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "User Profile",
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
                    IconButton(onClick = {
                        if (currentUser?.role?.name == "SECURITY") {
                             onNavigateToSecurityProfile?.invoke()
                        } else {
                             onNavigateToDriverProfile?.invoke()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = MaterialTheme.colorScheme.primary
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            item {
                // Profile Avatar Card
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    MindMirrorCard(
                        modifier = Modifier.padding(0.dp)
                    ) {
                        Text(
                            "👤",
                            style = MaterialTheme.typography.displayLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                // User Info Card
                MindMirrorCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            "Profile Information",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = if (currentUser?.role?.name == "SECURITY") RoleTheme.securityColor else RoleTheme.driverColor,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        ProfileInfoItem("Name", currentUser?.name ?: "User")
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        ProfileInfoItem("Email", currentUser?.email ?: "N/A")
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        ProfileInfoItem("Role", currentUser?.role?.name ?: "N/A")
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        if (currentUser?.role?.name != "SECURITY") {
                            if (currentUser?.vehicleNumber != null) {
                                ProfileInfoItem("Vehicle", currentUser.vehicleNumber)
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                            
                            ProfileInfoItem("Membership", currentUser?.membershipType ?: "Standard")
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                // Settings Section
                MindMirrorCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Settings",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = if (currentUser?.role?.name == "SECURITY") RoleTheme.securityColor else RoleTheme.driverColor,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        // Theme Toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (isDarkTheme) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                                    contentDescription = "Theme Icon",
                                    modifier = Modifier.size(20.dp),
                                    tint = if (currentUser?.role?.name == "SECURITY") RoleTheme.securityColor else RoleTheme.driverColor
                                )
                                Spacer(modifier = Modifier.size(12.dp))
                                Column {
                                    Text(
                                        text = "Dark Theme",
                                        fontFamily = Poppins,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isDarkTheme) "Enabled" else "Disabled",
                                        fontFamily = Poppins,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            
                            Switch(
                                checked = isDarkTheme,
                                onCheckedChange = { isChecked ->
                                    val newMode = if (isChecked) "DARK" else "LIGHT"
                                    viewModel.setThemeMode(newMode)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = if (currentUser?.role?.name == "SECURITY") RoleTheme.securityColor else RoleTheme.driverColor,
                                    checkedTrackColor = if (currentUser?.role?.name == "SECURITY") RoleTheme.securityColor.copy(alpha = 0.3f) else RoleTheme.driverColor.copy(alpha = 0.3f),
                                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .let {
                                    if (onNavigateToNotifications != null) {
                                        it.clickable { onNavigateToNotifications() }
                                    } else {
                                        it
                                    }
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            SettingsItem("🔔 Notifications", "Manage alerts")
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .let {
                                    if (onNavigateToChangePassword != null) {
                                        it.clickable { onNavigateToChangePassword() }
                                    } else {
                                        it
                                    }
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            SettingsItem("🔒 Security", "Change password")
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                // Sign Out Button
                Button(
                    onClick = { onLogout?.invoke() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        "Sign Out",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ProfileInfoItem(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            label,
            fontFamily = Poppins,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
            fontFamily = Poppins,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SettingsItem(title: String, description: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            title,
            fontFamily = Poppins,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            description,
            fontFamily = Poppins,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
