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
import androidx.compose.ui.unit.dp
import com.kushan.vaultpark.model.User
import com.kushan.vaultpark.ui.components.*
import com.kushan.vaultpark.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDriverDialog(
    driver: User,
    onDismiss: () -> Unit,
    onUpdateDriver: (fullName: String, phone: String, vehicleNumber: String, membershipType: String) -> Unit,
    isLoading: Boolean = false
) {
    var fullName by remember { mutableStateOf(driver.name) }
    var phone by remember { mutableStateOf(driver.phone) }
    var vehicleNumber by remember { mutableStateOf(driver.vehicleNumber) }
    var membershipType by remember { mutableStateOf(driver.membershipType) }
    var hasChanges by remember { mutableStateOf(false) }

    // Check for changes
    LaunchedEffect(fullName, phone, vehicleNumber, membershipType) {
        hasChanges = fullName != driver.name ||
                phone != driver.phone ||
                vehicleNumber != driver.vehicleNumber ||
                membershipType != driver.membershipType
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
                        text = "Edit Driver",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Driver Email (Read-only)
                Text(
                    text = "Email",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = driver.email,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Divider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

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
                        isSelected = membershipType.equals("Gold", ignoreCase = true),
                        onClick = { membershipType = "Gold" },
                        modifier = Modifier.weight(1f)
                    )
                    MembershipTypeCard(
                        title = "Platinum",
                        isSelected = membershipType.equals("Platinum", ignoreCase = true),
                        onClick = { membershipType = "Platinum" },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Additional Info Section
                ModernCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Additional Information",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        InfoRow(
                            icon = Icons.Default.CalendarToday,
                            label = "Member Since",
                            value = driver.createdAt?.let { 
                                java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                                    .format(it) 
                            } ?: "Unknown"
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        InfoRow(
                            icon = if (driver.isActive == true) Icons.Default.CheckCircle else Icons.Default.Block,
                            label = "Account Status",
                            value = if (driver.isActive == true) "Active" else "Inactive",
                            valueColor = if (driver.isActive == true) StatusSuccess else Color.Gray
                        )
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
                        text = "Save Changes",
                        onClick = {
                            onUpdateDriver(fullName, phone, vehicleNumber, membershipType)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading && hasChanges && isFormValid(fullName, phone, vehicleNumber),
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

private fun isFormValid(
    fullName: String,
    phone: String,
    vehicleNumber: String
): Boolean {
    return fullName.isNotBlank() &&
            phone.isNotBlank() &&
            vehicleNumber.isNotBlank()
}