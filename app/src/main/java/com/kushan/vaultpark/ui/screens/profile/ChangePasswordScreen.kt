package com.kushan.vaultpark.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kushan.vaultpark.ui.theme.DarkBackground
import com.kushan.vaultpark.ui.theme.DarkSurface
import com.kushan.vaultpark.ui.theme.PrimaryPurple
import com.kushan.vaultpark.ui.theme.StatusError
import com.kushan.vaultpark.ui.theme.StatusSuccess
import com.kushan.vaultpark.ui.theme.TextLight
import com.kushan.vaultpark.ui.theme.TextSecondaryDark
import com.kushan.vaultpark.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    viewModel: ProfileViewModel,
    navController: NavController
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    var validationError by remember { mutableStateOf("") }

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
                // Navigate back after success
                navController.popBackStack()
            }
        }
    }

    val isFormValid = currentPassword.isNotEmpty() &&
            newPassword.isNotEmpty() &&
            confirmPassword.isNotEmpty() &&
            newPassword == confirmPassword &&
            newPassword.length >= 8

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Change Password", color = TextLight) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextLight
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(DarkBackground)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Spacing
                Box(modifier = Modifier.height(24.dp))

                // Title
                Text(
                    text = "Update Your Password",
                    fontSize = 24.sp,
                    color = TextLight,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Enter your current password and choose a new one",
                    fontSize = 14.sp,
                    color = TextSecondaryDark,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // Card Container
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurface, RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    // Current Password Field
                    Text(
                        text = "Current Password",
                        fontSize = 14.sp,
                        color = TextSecondaryDark,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    TextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it; validationError = "" },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        placeholder = { Text("Enter current password", color = TextSecondaryDark) },
                        visualTransformation = if (showCurrentPassword) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                                Icon(
                                    imageVector = if (showCurrentPassword) {
                                        Icons.Filled.Visibility
                                    } else {
                                        Icons.Filled.VisibilityOff
                                    },
                                    contentDescription = "Toggle password visibility",
                                    tint = TextSecondaryDark
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = DarkBackground,
                            unfocusedContainerColor = DarkBackground,
                            focusedIndicatorColor = PrimaryPurple,
                            unfocusedIndicatorColor = TextSecondaryDark,
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight
                        ),
                        singleLine = true
                    )

                    // New Password Field
                    Text(
                        text = "New Password",
                        fontSize = 14.sp,
                        color = TextSecondaryDark,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    TextField(
                        value = newPassword,
                        onValueChange = { newPassword = it; validationError = "" },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        placeholder = { Text("Enter new password (min 8 characters)", color = TextSecondaryDark) },
                        visualTransformation = if (showNewPassword) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(onClick = { showNewPassword = !showNewPassword }) {
                                Icon(
                                    imageVector = if (showNewPassword) {
                                        Icons.Filled.Visibility
                                    } else {
                                        Icons.Filled.VisibilityOff
                                    },
                                    contentDescription = "Toggle password visibility",
                                    tint = TextSecondaryDark
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = DarkBackground,
                            unfocusedContainerColor = DarkBackground,
                            focusedIndicatorColor = PrimaryPurple,
                            unfocusedIndicatorColor = TextSecondaryDark,
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight
                        ),
                        singleLine = true
                    )

                    // Password strength indicator
                    if (newPassword.isNotEmpty()) {
                        val strength = when {
                            newPassword.length < 8 -> "Too short"
                            newPassword.length < 12 -> "Weak"
                            newPassword.any { it.isDigit() } && newPassword.any { it.isUpperCase() } -> "Strong"
                            else -> "Medium"
                        }
                        val strengthColor = when (strength) {
                            "Too short" -> StatusError
                            "Weak" -> StatusError
                            "Medium" -> com.kushan.vaultpark.ui.theme.StatusWarning
                            "Strong" -> StatusSuccess
                            else -> TextSecondaryDark
                        }

                        Text(
                            text = strength,
                            fontSize = 12.sp,
                            color = strengthColor,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )
                    }

                    // Confirm Password Field
                    Text(
                        text = "Confirm New Password",
                        fontSize = 14.sp,
                        color = TextSecondaryDark,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    TextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; validationError = "" },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Confirm new password", color = TextSecondaryDark) },
                        visualTransformation = if (showConfirmPassword) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                Icon(
                                    imageVector = if (showConfirmPassword) {
                                        Icons.Filled.Visibility
                                    } else {
                                        Icons.Filled.VisibilityOff
                                    },
                                    contentDescription = "Toggle password visibility",
                                    tint = TextSecondaryDark
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = DarkBackground,
                            unfocusedContainerColor = DarkBackground,
                            focusedIndicatorColor = PrimaryPurple,
                            unfocusedIndicatorColor = TextSecondaryDark,
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight
                        ),
                        singleLine = true
                    )

                    // Mismatch error
                    if (confirmPassword.isNotEmpty() && newPassword != confirmPassword) {
                        Text(
                            text = "Passwords do not match",
                            fontSize = 12.sp,
                            color = StatusError,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // Validation error message
                    if (validationError.isNotEmpty()) {
                        Text(
                            text = validationError,
                            fontSize = 12.sp,
                            color = StatusError,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }

                // Update Button
                Button(
                    onClick = {
                        when {
                            currentPassword.isEmpty() -> validationError = "Current password required"
                            newPassword.length < 8 -> validationError = "New password must be at least 8 characters"
                            newPassword != confirmPassword -> validationError = "Passwords do not match"
                            else -> {
                                validationError = ""
                                viewModel.changePassword(currentPassword, newPassword)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(top = 32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryPurple
                    ),
                    shape = RoundedCornerShape(28.dp),
                    enabled = isFormValid && !isLoading
                ) {
                    Text(
                        text = if (isLoading) "Updating..." else "Update Password",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLight
                    )
                }

                // Cancel Button
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(top = 12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkSurface
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondaryDark
                    )
                }
            }
        }
    }
}
