package com.kushan.vaultpark.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kushan.vaultpark.model.ManualEntryTemplate
import com.kushan.vaultpark.model.RecentDriver
import com.kushan.vaultpark.model.User
import com.kushan.vaultpark.ui.theme.*
import com.kushan.vaultpark.viewmodel.AdminToolsViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

/**
 * STEP 1: Enhanced Manual Entry Dialog
 * Features:
 * - Search driver by name or vehicle
 * - Auto-complete from existing users
 * - Recent drivers list
 * - Save as template
 * - Quick entry form
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedManualEntryDialog(
    onDismiss: () -> Unit,
    viewModel: AdminToolsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedDriver by remember { mutableStateOf<User?>(null) }
    var driverName by remember { mutableStateOf("") }
    var vehicleNumber by remember { mutableStateOf("") }
    var selectedGate by remember { mutableStateOf("Main Entrance") }
    var entryType by remember { mutableStateOf("ENTRY") } // ENTRY or EXIT
    var notes by remember { mutableStateOf("") }
    var showTemplateDialog by remember { mutableStateOf(false) }

    // Trigger search with debounce
    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 2) {
            delay(300)
            viewModel.searchDrivers(searchQuery)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 700.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Manual Entry",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = TextLight
                        )

                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextSecondaryDark
                            )
                        }
                    }
                }

                // Entry/Exit Toggle
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = entryType == "ENTRY",
                            onClick = { entryType = "ENTRY" },
                            label = {
                                Text(
                                    text = "Entry",
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Login,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = entryType == "EXIT",
                            onClick = { entryType = "EXIT" },
                            label = {
                                Text(
                                    text = "Exit",
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Logout,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Search Bar
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text(
                                "Search Driver or Vehicle",
                                fontFamily = Poppins
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Search Results
                if (uiState.driverSearchResults.isNotEmpty()) {
                    item {
                        Text(
                            text = "Search Results",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = TextSecondaryDark
                        )
                    }

                    items(uiState.driverSearchResults) { driver ->
                        DriverSearchResultItem(
                            driver = driver,
                            onClick = {
                                selectedDriver = driver
                                driverName = driver.name
                                vehicleNumber = driver.vehicleNumber
                                searchQuery = ""
                            }
                        )
                    }
                }

                // Recent Drivers
                if (uiState.recentDrivers.isNotEmpty() && searchQuery.isEmpty()) {
                    item {
                        Text(
                            text = "Recent Drivers",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = TextSecondaryDark
                        )
                    }

                    items(uiState.recentDrivers.take(5)) { recentDriver ->
                        RecentDriverItem(
                            driver = recentDriver,
                            onClick = {
                                driverName = recentDriver.name
                                vehicleNumber = recentDriver.vehicleNumber
                            }
                        )
                    }
                }

                // Saved Templates
                if (uiState.savedTemplates.isNotEmpty() && searchQuery.isEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Saved Templates",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = TextSecondaryDark
                            )

                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                tint = SecondaryGold,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    items(uiState.savedTemplates.take(3)) { template ->
                        TemplateItem(
                            template = template,
                            onClick = {
                                driverName = template.driverName
                                vehicleNumber = template.vehicleNumber
                                selectedGate = template.defaultGate
                                notes = template.notes
                            }
                        )
                    }
                }

                // Manual Entry Form
                item {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    Text(
                        text = "Entry Details",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = TextLight
                    )
                }

                item {
                    OutlinedTextField(
                        value = driverName,
                        onValueChange = { driverName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Driver Name", fontFamily = Poppins) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = vehicleNumber,
                        onValueChange = { vehicleNumber = it.uppercase() },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Vehicle Number", fontFamily = Poppins) },
                        leadingIcon = {
                            Icon(Icons.Default.DirectionsCar, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                item {
                    GateDropdownSelector(
                        selectedGate = selectedGate,
                        onGateSelected = { selectedGate = it }
                    )
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Notes (Optional)", fontFamily = Poppins) },
                        leadingIcon = {
                            Icon(Icons.Default.Notes, contentDescription = null)
                        },
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Action Buttons
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Save as Template
                        OutlinedButton(
                            onClick = { showTemplateDialog = true },
                            modifier = Modifier.weight(1f),
                            enabled = driverName.isNotEmpty() && vehicleNumber.isNotEmpty(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Save",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Submit
                        Button(
                            onClick = {
                                if (driverName.isNotEmpty() && vehicleNumber.isNotEmpty()) {
                                    viewModel.createManualEntry(
                                        driverId = selectedDriver?.id ?: UUID.randomUUID().toString(),
                                        driverName = driverName,
                                        vehicleNumber = vehicleNumber,
                                        gateLocation = selectedGate,
                                        entryType = entryType,
                                        notes = notes
                                    )
                                    onDismiss()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = driverName.isNotEmpty() && vehicleNumber.isNotEmpty() && !uiState.isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryPurple
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    "Record $entryType",
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Save Template Dialog
    if (showTemplateDialog) {
        AlertDialog(
            onDismissRequest = { showTemplateDialog = false },
            title = {
                Text(
                    "Save as Template",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Save this driver info for quick access in future entries?",
                    fontFamily = Poppins
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveAsTemplate(
                            driverName = driverName,
                            vehicleNumber = vehicleNumber,
                            defaultGate = selectedGate,
                            notes = notes
                        )
                        showTemplateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryPurple
                    )
                ) {
                    Text("Save", fontFamily = Poppins, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTemplateDialog = false }) {
                    Text("Cancel", fontFamily = Poppins)
                }
            }
        )
    }
}

@Composable
private fun DriverSearchResultItem(
    driver: User,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = driver.name,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = TextLight
                )
                Text(
                    text = driver.vehicleNumber,
                    fontFamily = Poppins,
                    fontSize = 12.sp,
                    color = TextSecondaryDark
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = PrimaryPurple
            )
        }
    }
}

@Composable
private fun RecentDriverItem(
    driver: RecentDriver,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = SecondaryGold,
                    modifier = Modifier.size(20.dp)
                )

                Column {
                    Text(
                        text = driver.name,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = TextLight
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = driver.vehicleNumber,
                            fontFamily = Poppins,
                            fontSize = 12.sp,
                            color = TextSecondaryDark
                        )
                        Text(
                            text = "•",
                            fontSize = 12.sp,
                            color = TextSecondaryDark
                        )
                        Text(
                            text = formatTimeAgo(driver.lastVisit),
                            fontFamily = Poppins,
                            fontSize = 12.sp,
                            color = TextSecondaryDark
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = PrimaryPurple
            )
        }
    }
}

@Composable
private fun TemplateItem(
    template: ManualEntryTemplate,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = SecondaryGold.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = null,
                    tint = SecondaryGold,
                    modifier = Modifier.size(20.dp)
                )

                Column {
                    Text(
                        text = template.driverName,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = TextLight
                    )
                    Text(
                        text = "${template.vehicleNumber} • ${template.defaultGate}",
                        fontFamily = Poppins,
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )
                }
            }

            Text(
                text = "${template.useCount} uses",
                fontFamily = Poppins,
                fontSize = 10.sp,
                color = SecondaryGold,
                modifier = Modifier
                    .background(
                        color = SecondaryGold.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GateDropdownSelector(
    selectedGate: String,
    onGateSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    val gates = listOf(
        "Main Entrance",
        "Exit Gate A",
        "Exit Gate B",
        "Visitor Parking",
        "Compact Parking"
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedGate,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            label = { Text("Gate Location", fontFamily = Poppins) },
            leadingIcon = {
                Icon(Icons.Default.LocationOn, contentDescription = null)
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            shape = RoundedCornerShape(12.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            gates.forEach { gate ->
                DropdownMenuItem(
                    text = {
                        Text(
                            gate,
                            fontFamily = Poppins
                        )
                    },
                    onClick = {
                        onGateSelected(gate)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun formatTimeAgo(timeMillis: Long): String {
    val now = System.currentTimeMillis()
    val diff = (now - timeMillis) / 1000

    return when {
        diff < 60 -> "Now"
        diff < 3600 -> "${diff / 60}m ago"
        diff < 86400 -> "${diff / 3600}h ago"
        else -> "${diff / 86400}d ago"
    }
}
