package com.kushan.vaultpark.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kushan.vaultpark.model.User
import com.kushan.vaultpark.model.UserRole
import com.kushan.vaultpark.ui.components.ModernPrimaryButton
import com.kushan.vaultpark.ui.components.ModernTextField
import com.kushan.vaultpark.ui.components.ModernCard
import com.kushan.vaultpark.ui.theme.Poppins
import com.kushan.vaultpark.viewmodel.AuthViewModel

@Composable
fun SignUpScreen(
    authViewModel: AuthViewModel,
    onSignUpSuccess: (User) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()
    
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var vehicleNumber by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf<UserRole?>(null) }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(isAuthenticated, currentUser) {
        if (isAuthenticated && currentUser != null) {
            onSignUpSuccess(currentUser!!)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 40.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // VaultPark Logo
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -30 })
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    Text(
                        text = "VaultPark",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Poppins,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Create Your Account",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Normal,
                        fontFamily = Poppins
                    )
                }
            }
            
            // Sign Up Form Card
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 30 })
            ) {
                ModernCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Text(
                            text = "Sign Up",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = Poppins,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        // Role Selection
                        Text(
                            text = "Select Your Role",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = Poppins,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Driver Role Card
                            RoleSelectionCard(
                                modifier = Modifier.weight(1f),
                                title = "DRIVER",
                                isSelected = selectedRole == UserRole.DRIVER,
                                icon = Icons.Default.DirectionsCar,
                                onClick = { selectedRole = UserRole.DRIVER }
                            )
                            
                            // Security Role Card
                            RoleSelectionCard(
                                modifier = Modifier.weight(1f),
                                title = "SECURITY",
                                isSelected = selectedRole == UserRole.SECURITY,
                                icon = Icons.Default.Security,
                                onClick = { selectedRole = UserRole.SECURITY }
                            )
                        }
                        
                        // Name Field
                        ModernTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = "Full Name",
                            placeholder = "Enter your full name",
                            enabled = !isLoading,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            )
                        )
                        
                        // Email Field
                        ModernTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = "Email",
                            placeholder = "Enter your email",
                            enabled = !isLoading,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            )
                        )
                        
                        // Vehicle Number Field (Only for Drivers)
                        if (selectedRole == UserRole.DRIVER) {
                            ModernTextField(
                                value = vehicleNumber,
                                onValueChange = { vehicleNumber = it },
                                label = "Vehicle Number",
                                placeholder = "e.g., ABC-001",
                                enabled = !isLoading,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                )
                            )
                        }
                        
                        // Password Field
                        ModernTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = "Password",
                            placeholder = "Enter your password",
                            enabled = !isLoading,
                            visualTransformation = if (showPassword) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = { showPassword = !showPassword },
                                    enabled = !isLoading
                                ) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = "Toggle password visibility",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            )
                        )
                        
                        // Confirm Password Field
                        ModernTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = "Confirm Password",
                            placeholder = "Re-enter your password",
                            enabled = !isLoading,
                            visualTransformation = if (showConfirmPassword) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = { showConfirmPassword = !showConfirmPassword },
                                    enabled = !isLoading
                                ) {
                                    Icon(
                                        imageVector = if (showConfirmPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = "Toggle confirm password visibility",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            )
                        )
                        
                        // Validation Error Message
                        if (validationError != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                )
                            ) {
                                Text(
                                    text = validationError!!,
                                    modifier = Modifier.padding(12.dp),
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = Poppins
                                )
                            }
                        }
                        
                        // Error Message from ViewModel
                        if (errorMessage != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                )
                            ) {
                                Text(
                                    text = errorMessage!!,
                                    modifier = Modifier.padding(12.dp),
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = Poppins
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Sign Up Button
                        ModernPrimaryButton(
                            text = "Sign Up",
                            onClick = {
                                validationError = null
                                authViewModel.clearError()
                                
                                // Validate inputs
                                when {
                                    selectedRole == null -> {
                                        validationError = "Please select your role (DRIVER or SECURITY)"
                                    }
                                    name.isBlank() -> {
                                        validationError = "Please enter your full name"
                                    }
                                    email.isBlank() -> {
                                        validationError = "Please enter your email"
                                    }
                                    !email.contains("@") -> {
                                        validationError = "Please enter a valid email address"
                                    }
                                    password.isBlank() -> {
                                        validationError = "Please enter a password"
                                    }
                                    password.length < 6 -> {
                                        validationError = "Password must be at least 6 characters"
                                    }
                                    password != confirmPassword -> {
                                        validationError = "Passwords do not match"
                                    }
                                    selectedRole == UserRole.DRIVER && vehicleNumber.isBlank() -> {
                                        validationError = "Please enter your vehicle number"
                                    }
                                    else -> {
                                        // All validations passed, proceed with sign up
                                        authViewModel.signUp(
                                            email = email,
                                            password = password,
                                            name = name,
                                            role = selectedRole!!,
                                            vehicleNumber = vehicleNumber
                                        )
                                    }
                                }
                            },
                            enabled = !isLoading,
                            isLoading = isLoading
                        )
                    }
                }
            }
            
            // Already have an account link
            Spacer(modifier = Modifier.height(24.dp))
            
            val annotatedText = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        fontFamily = Poppins
                    )
                ) {
                    append("Already have an account? ")
                }
                pushStringAnnotation(tag = "LOGIN", annotation = "login")
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = Poppins
                    )
                ) {
                    append("Sign In")
                }
                pop()
            }
            
            ClickableText(
                text = annotatedText,
                onClick = { offset ->
                    annotatedText.getStringAnnotations(tag = "LOGIN", start = offset, end = offset)
                        .firstOrNull()?.let {
                            onNavigateToLogin()
                        }
                }
            )
        }
    }
}

@Composable
fun RoleSelectionCard(
    modifier: Modifier = Modifier,
    title: String,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .clickable { onClick() }
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(16.dp)
                    )
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(bottom = 8.dp)
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(bottom = 8.dp)
                    )
                }
                
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    fontFamily = Poppins,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}
