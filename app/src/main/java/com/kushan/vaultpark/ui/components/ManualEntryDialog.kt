package com.kushan.vaultpark.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kushan.vaultpark.ui.theme.Poppins
import com.kushan.vaultpark.ui.theme.PrimaryPurple
import com.kushan.vaultpark.ui.theme.TextSecondaryDark

/**
 * Manual Entry Dialog for security guards
 * Used when QR scan fails or driver forgot their phone
 */
@Composable
fun ManualEntryDialog(
    onDismiss: () -> Unit,
    onSubmit: (driverName: String, vehicleNumber: String, entryType: String, gateLocation: String, notes: String) -> Unit
) {
    var driverNameInput by remember { mutableStateOf(TextFieldValue("")) }
    var vehicleNumberInput by remember { mutableStateOf(TextFieldValue("")) }
    var gateExpanded by remember { mutableStateOf(false) }
    var selectedGate by remember { mutableStateOf("Main Entrance") }
    var entryTypeExpanded by remember { mutableStateOf(false) }
    var selectedEntryType by remember { mutableStateOf("ENTRY") }
    var notesInput by remember { mutableStateOf(TextFieldValue("")) }
    var isFormValid by remember(driverNameInput, vehicleNumberInput) {
        mutableStateOf(driverNameInput.text.isNotBlank() && vehicleNumberInput.text.isNotBlank())
    }

    val gates = listOf("Main Entrance", "Exit Gate A", "Exit Gate B")
    val entryTypes = listOf("ENTRY", "EXIT")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .background(Color.Black.copy(alpha = 0.7f))
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .align(Alignment.Center),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header with close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Manual Entry Form",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(
                            onClick = onDismiss
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Text(
                        text = "When QR scan is not possible",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )

                    // Form fields in scrollable column
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                    ) {
                        item {
                            // Driver Name Field
                            TextField(
                                value = driverNameInput,
                                onValueChange = { driverNameInput = it },
                                label = { Text("Driver Name *") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                singleLine = true
                            )
                        }

                        item {
                            // Vehicle Number Field
                            TextField(
                                value = vehicleNumberInput,
                                onValueChange = { vehicleNumberInput = it },
                                label = { Text("Vehicle Number *") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                singleLine = true
                            )
                        }

                        item {
                            // Entry Type Dropdown
                            EntryTypeDropdown(
                                selectedType = selectedEntryType,
                                expanded = entryTypeExpanded,
                                onExpandedChange = { entryTypeExpanded = !entryTypeExpanded },
                                onTypeSelected = {
                                    selectedEntryType = it
                                    entryTypeExpanded = false
                                }
                            )
                        }

                        item {
                            // Gate Location Dropdown
                            GateSelectorDropdownManual(
                                selectedGate = selectedGate,
                                expanded = gateExpanded,
                                onExpandedChange = { gateExpanded = !gateExpanded },
                                onGateSelected = {
                                    selectedGate = it
                                    gateExpanded = false
                                }
                            )
                        }

                        item {
                            // Notes Field
                            TextField(
                                value = notesInput,
                                onValueChange = { notesInput = it },
                                label = { Text("Notes (Optional)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                maxLines = 4
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                text = "Cancel",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = {
                                onSubmit(
                                    driverNameInput.text,
                                    vehicleNumberInput.text,
                                    selectedEntryType,
                                    selectedGate,
                                    notesInput.text
                                )
                                onDismiss()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            enabled = isFormValid,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryPurple,
                                disabledContainerColor = PrimaryPurple.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                text = "Submit",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Entry Type Dropdown
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EntryTypeDropdown(
    selectedType: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onTypeSelected: (String) -> Unit
) {
    val entryTypes = listOf("ENTRY", "EXIT")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            value = selectedType,
            onValueChange = {},
            readOnly = true,
            label = { Text("Entry Type *") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            entryTypes.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type) },
                    onClick = {
                        onTypeSelected(type)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}

/**
 * Gate Selector Dropdown for Manual Entry
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GateSelectorDropdownManual(
    selectedGate: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onGateSelected: (String) -> Unit
) {
    val gates = listOf("Main Entrance", "Exit Gate A", "Exit Gate B")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            value = selectedGate,
            onValueChange = {},
            readOnly = true,
            label = { Text("Gate Location *") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            gates.forEach { gate ->
                DropdownMenuItem(
                    text = { Text(gate) },
                    onClick = {
                        onGateSelected(gate)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}
