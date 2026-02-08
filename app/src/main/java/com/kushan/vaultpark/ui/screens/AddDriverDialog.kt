package com.kushan.vaultpark.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.kushan.vaultpark.ui.components.*
import com.kushan.vaultpark.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDriverDialog(
    onDismiss: () -> Unit,
    onAddDriver: (fullName: String, email: String, phone: String, vehicleNumber: String, membershipType: String, password: String) -> Unit,
    isLoading: Boolean = false
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var vehicleNumber by remember { mutableStateOf("") }
    var membershipType by remember { mutableStateOf("Gold") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var autoGeneratePassword by remember { mutableStateOf(true) }

    // Generate random password
    fun generatePassword(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*"
        return (1..12).map { chars.random() }.joinToString("")
    }

    LaunchedEffect(autoGeneratePassword) {
        if (autoGeneratePassword) {
            password = generatePassword()
            confirmPassword = password
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add New Driver",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Full Name
                ModernTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    placeholder = "Full Name",
                    leadingIcon = Icons.Default.Person,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Email
                ModernTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "Email Address",
                    leadingIcon = Icons.Default.Email,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = email.isNotBlank() && !isValidEmail(email)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Phone Number
                ModernTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = "Phone Number",
                    leadingIcon = Icons.Default.Phone,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Vehicle Number
                ModernTextField(
                    value = vehicleNumber,
                    onValueChange = { vehicleNumber = it },
                    placeholder = "Vehicle Number",
                    leadingIcon = Icons.Default.DirectionsCar,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Membership Type
                Text(
                    text = "Membership Type",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MembershipTypeCard(
                        title = "Gold",
                        isSelected = membershipType == "Gold",
                        onClick = { membershipType = "Gold" },
                        modifier = Modifier.weight(1f)
                    )
                    MembershipTypeCard(
                        title = "Platinum",
                        isSelected = membershipType == "Platinum",
                        onClick = { membershipType = "Platinum" },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Password Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Password",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = autoGeneratePassword,
                            onCheckedChange = { 
                                autoGeneratePassword = it
                                if (it) {
                                    password = generatePassword()
                                    confirmPassword = password
                                }
                            }
                        )
                        Text(
                            text = "Auto-generate",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (!autoGeneratePassword) {
                    ModernTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = "Password",
                        leadingIcon = Icons.Default.Lock,
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showPassword) "Hide password" else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ModernTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = "Confirm Password",
                        leadingIcon = Icons.Default.Lock,
                        trailingIcon = {
                            IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                Icon(
                                    if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showConfirmPassword) "Hide password" else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        isError = confirmPassword.isNotBlank() && password != confirmPassword
                    )
                } else {
                    // Show generated password
                    ModernCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Generated Password:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = password,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = NeonLime
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            ModernPrimaryButton(
                                text = "Regenerate",
                                onClick = { 
                                    password = generatePassword()
                                    confirmPassword = password
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        Text("Cancel")
                    }
                    
                    ModernPrimaryButton(
                        text = "Create Account",
                        onClick = {
                            val finalPassword = if (autoGeneratePassword) password else confirmPassword
                            onAddDriver(fullName, email, phone, vehicleNumber, membershipType, finalPassword)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading && isFormValid(fullName, email, phone, vehicleNumber, password, confirmPassword, autoGeneratePassword),
                        isLoading = isLoading
                    )
                }
            }
        }
    }
}

@Composable
private fun MembershipTypeCard(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        when (title) {
            "Gold" -> StatusWarning
            "Platinum" -> PrimaryPurple
            else -> MaterialTheme.colorScheme.primary
        }
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = when (title) {
                    "Gold" -> Icons.Default.Star
                    "Platinum" -> Icons.Default.Diamond
                    else -> Icons.Default.CardMembership
                },
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

private fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

private fun isFormValid(
    fullName: String,
    email: String,
    phone: String,
    vehicleNumber: String,
    password: String,
    confirmPassword: String,
    autoGeneratePassword: Boolean
): Boolean {
    return fullName.isNotBlank() &&
            isValidEmail(email) &&
            phone.isNotBlank() &&
            vehicleNumber.isNotBlank() &&
            password.isNotBlank() &&
            (autoGeneratePassword || password == confirmPassword)
}