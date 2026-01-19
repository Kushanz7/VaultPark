package com.kushan.vaultpark.ui.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.kushan.vaultpark.ui.theme.DarkSurface
import com.kushan.vaultpark.ui.theme.PrimaryPurple
import com.kushan.vaultpark.ui.theme.TextLight
import com.kushan.vaultpark.ui.theme.TextSecondaryDark

/**
 * Request notification permission for Android 13+
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionState = rememberPermissionState(
            Manifest.permission.POST_NOTIFICATIONS
        )

        LaunchedEffect(Unit) {
            if (permissionState.status != PermissionStatus.Granted) {
                permissionState.launchPermissionRequest()
            }
        }

        if (permissionState.status != PermissionStatus.Granted) {
            NotificationPermissionDialog(permissionState)
        }
    }
}

/**
 * Notification permission dialog
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun NotificationPermissionDialog(
    permissionState: PermissionState
) {
    val showDialog = remember { mutableStateOf(true) }

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = {
                Text(
                    "Enable Notifications",
                    color = TextLight,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Icon(
                        imageVector = Icons.Filled.NotificationsActive,
                        contentDescription = "Notifications",
                        tint = PrimaryPurple,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 16.dp)
                    )

                    Text(
                        text = "VaultPark needs notification permission to:",
                        color = TextLight,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    listOf(
                        "• Alert you when entering parking",
                        "• Notify on parking exits",
                        "• Send billing reminders",
                        "• Security scan updates"
                    ).forEach { item ->
                        Text(
                            text = item,
                            color = TextSecondaryDark,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        permissionState.launchPermissionRequest()
                        showDialog.value = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryPurple
                    )
                ) {
                    Text("Allow", color = TextLight)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog.value = false }
                ) {
                    Text("Later", color = TextSecondaryDark)
                }
            },
            containerColor = DarkSurface,
            titleContentColor = TextLight
        )
    }
}

/**
 * Request camera permission
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestCameraPermission() {
    val permissionState = rememberPermissionState(
        Manifest.permission.CAMERA
    )

    LaunchedEffect(Unit) {
        if (permissionState.status != PermissionStatus.Granted) {
            permissionState.launchPermissionRequest()
        }
    }
}

/**
 * Request storage permission
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestStoragePermission() {
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val permissionState = rememberPermissionState(permission)

    LaunchedEffect(Unit) {
        if (permissionState.status != PermissionStatus.Granted) {
            permissionState.launchPermissionRequest()
        }
    }
}

/**
 * Permission helper functions
 */
object PermissionHelper {

    /**
     * Check if notification permission is granted
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Check if camera permission is granted
     */
    fun hasCameraPermission(context: Context): Boolean {
        return context.checkSelfPermission(Manifest.permission.CAMERA) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if storage permission is granted
     */
    fun hasStoragePermission(context: Context): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        return context.checkSelfPermission(permission) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    /**
     * Open app settings to enable permission manually
     */
    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }

    /**
     * Show permission denied dialog with settings option
     */
    @Composable
    fun PermissionDeniedDialog(
        title: String,
        message: String,
        onDismiss: () -> Unit,
        onOpenSettings: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title, color = TextLight) },
            text = { Text(message, color = TextSecondaryDark) },
            confirmButton = {
                Button(
                    onClick = onOpenSettings,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryPurple
                    )
                ) {
                    Text("Open Settings", color = TextLight)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = TextSecondaryDark)
                }
            },
            containerColor = DarkSurface
        )
    }
}
