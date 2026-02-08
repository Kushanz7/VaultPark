package com.kushan.vaultpark.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.kushan.vaultpark.ui.theme.RoleTheme
import com.kushan.vaultpark.viewmodel.ParkingLotViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityGuardParkingLotScreen(
    guardId: String,
    guardName: String,
    viewModel: ParkingLotViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var isCreating by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    
    // Form fields
    var lotName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var totalSpaces by remember { mutableStateOf("") }
    var hourlyRate by remember { mutableStateOf("") }
    var dailyCap by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf("") }
    
    // Load the guard's parking lot on init
    LaunchedEffect(Unit) {
        viewModel.loadParkingLot(guardId)
    }
    
    // Show success/error messages
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }
    
    LaunchedEffect(validationError) {
        if (validationError.isNotEmpty()) {
            snackbarHostState.showSnackbar(validationError)
            validationError = ""
        }
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Parking Lot Management") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = RoleTheme.securityColor,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.surface)
        ) {
            if (uiState.myParkingLot == null && !isCreating) {
                // No parking lot exists - show create button
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalParking,
                            contentDescription = "No Parking Lot",
                            modifier = Modifier
                                .height(80.dp)
                                .width(80.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Parking Lot Created",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Create your first parking lot to start managing parking spaces",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                        Button(
                            onClick = { isCreating = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Create Parking Lot")
                        }
                    }
                }
            } else if (isCreating) {
                // Show creation form
                CreateParkingLotForm(
                    guardId = guardId,
                    guardName = guardName,
                    lotName = lotName,
                    onLotNameChange = { lotName = it },
                    location = location,
                    onLocationChange = { location = it },
                    totalSpaces = totalSpaces,
                    onTotalSpacesChange = { totalSpaces = it },
                    hourlyRate = hourlyRate,
                    onHourlyRateChange = { hourlyRate = it },
                    dailyCap = dailyCap,
                    onDailyCapChange = { dailyCap = it },
                    isLoading = uiState.isLoading,
                    onCreateClick = {
                        val spaces = totalSpaces.toIntOrNull() ?: 0
                        val rate = hourlyRate.toDoubleOrNull() ?: 0.0
                        val cap = dailyCap.toDoubleOrNull() ?: 0.0
                        
                        if (lotName.isBlank() || location.isBlank() || spaces <= 0 || rate <= 0 || cap <= 0) {
                            validationError = "Please fill all fields correctly"
                            return@CreateParkingLotForm
                        }
                        
                        viewModel.createParkingLot(
                            securityGuardId = guardId,
                            securityGuardName = guardName,
                            name = lotName,
                            location = location,
                            totalSpaces = spaces,
                            hourlyRate = rate,
                            dailyCap = cap
                        )
                        isCreating = false
                        lotName = ""
                        location = ""
                        totalSpaces = ""
                        hourlyRate = ""
                        dailyCap = ""
                    },
                    onCancelClick = { isCreating = false }
                )
            } else if (uiState.myParkingLot != null && !isEditing) {
                // Show parking lot details
                ParkingLotDetailsCard(
                    lot = uiState.myParkingLot!!,
                    onEditClick = {
                        isEditing = true
                        lotName = uiState.myParkingLot!!.name
                        location = uiState.myParkingLot!!.location
                        totalSpaces = uiState.myParkingLot!!.totalSpaces.toString()
                        hourlyRate = uiState.myParkingLot!!.hourlyRate.toString()
                        dailyCap = uiState.myParkingLot!!.dailyCap.toString()
                    },
                    onStatusChange = { newStatus ->
                        viewModel.toggleParkingLotStatus(uiState.myParkingLot!!.id, newStatus)
                    }
                )
            } else if (uiState.myParkingLot != null && isEditing) {
                // Show edit form
                EditParkingLotForm(
                    lot = uiState.myParkingLot!!,
                    lotName = lotName,
                    onLotNameChange = { lotName = it },
                    location = location,
                    onLocationChange = { location = it },
                    totalSpaces = totalSpaces,
                    onTotalSpacesChange = { totalSpaces = it },
                    hourlyRate = hourlyRate,
                    onHourlyRateChange = { hourlyRate = it },
                    dailyCap = dailyCap,
                    onDailyCapChange = { dailyCap = it },
                    isLoading = uiState.isLoading,
                    onUpdateClick = {
                        val spaces = totalSpaces.toIntOrNull() ?: 0
                        val rate = hourlyRate.toDoubleOrNull() ?: 0.0
                        val cap = dailyCap.toDoubleOrNull() ?: 0.0
                        
                        if (lotName.isBlank() || location.isBlank() || spaces <= 0 || rate <= 0 || cap <= 0) {
                            validationError = "Please fill all fields correctly"
                            return@EditParkingLotForm
                        }
                        
                        viewModel.updateParkingLot(
                            lotId = uiState.myParkingLot!!.id,
                            name = lotName,
                            location = location,
                            totalSpaces = spaces,
                            hourlyRate = rate,
                            dailyCap = cap
                        )
                        isEditing = false
                    },
                    onCancelClick = { isEditing = false }
                )
            }
        }
    }
}

@Composable
private fun CreateParkingLotForm(
    guardId: String,
    guardName: String,
    lotName: String,
    onLotNameChange: (String) -> Unit,
    location: String,
    onLocationChange: (String) -> Unit,
    totalSpaces: String,
    onTotalSpacesChange: (String) -> Unit,
    hourlyRate: String,
    onHourlyRateChange: (String) -> Unit,
    dailyCap: String,
    onDailyCapChange: (String) -> Unit,
    isLoading: Boolean,
    onCreateClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Create Your Parking Lot",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        OutlinedTextField(
            value = lotName,
            onValueChange = onLotNameChange,
            label = { Text("Lot Name") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            singleLine = true
        )
        
        OutlinedTextField(
            value = location,
            onValueChange = onLocationChange,
            label = { Text("Location") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.LocationOn, "Location") }
        )
        
        OutlinedTextField(
            value = totalSpaces,
            onValueChange = onTotalSpacesChange,
            label = { Text("Total Parking Spaces") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        
        OutlinedTextField(
            value = hourlyRate,
            onValueChange = onHourlyRateChange,
            label = { Text("Hourly Rate ($)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )
        
        OutlinedTextField(
            value = dailyCap,
            onValueChange = onDailyCapChange,
            label = { Text("Daily Cap ($)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onCancelClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
            
            Button(
                onClick = onCreateClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                enabled = !isLoading
            ) {
                Text(if (isLoading) "Creating..." else "Create")
            }
        }
    }
}

@Composable
private fun EditParkingLotForm(
    lot: com.kushan.vaultpark.model.ParkingLot,
    lotName: String,
    onLotNameChange: (String) -> Unit,
    location: String,
    onLocationChange: (String) -> Unit,
    totalSpaces: String,
    onTotalSpacesChange: (String) -> Unit,
    hourlyRate: String,
    onHourlyRateChange: (String) -> Unit,
    dailyCap: String,
    onDailyCapChange: (String) -> Unit,
    isLoading: Boolean,
    onUpdateClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Edit Parking Lot",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        OutlinedTextField(
            value = lotName,
            onValueChange = onLotNameChange,
            label = { Text("Lot Name") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            singleLine = true
        )
        
        OutlinedTextField(
            value = location,
            onValueChange = onLocationChange,
            label = { Text("Location") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.LocationOn, "Location") }
        )
        
        OutlinedTextField(
            value = totalSpaces,
            onValueChange = onTotalSpacesChange,
            label = { Text("Total Parking Spaces") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        
        OutlinedTextField(
            value = hourlyRate,
            onValueChange = onHourlyRateChange,
            label = { Text("Hourly Rate ($)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )
        
        OutlinedTextField(
            value = dailyCap,
            onValueChange = onDailyCapChange,
            label = { Text("Daily Cap ($)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onCancelClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
            
            Button(
                onClick = onUpdateClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                enabled = !isLoading
            ) {
                Text(if (isLoading) "Updating..." else "Update")
            }
        }
    }
}

@Composable
private fun ParkingLotDetailsCard(
    lot: com.kushan.vaultpark.model.ParkingLot,
    onEditClick: () -> Unit,
    onStatusChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main parking lot info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = RoleTheme.securityColor
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = lot.name,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                modifier = Modifier
                                    .height(16.dp)
                                    .width(16.dp),
                                tint = Color.White
                            )
                            Text(
                                text = lot.location,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 4.dp),
                                color = Color.White
                            )
                        }
                    }
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit"
                        )
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Available Spaces",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = "${lot.availableSpaces} / ${lot.totalSpaces}",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hourly Rate",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = "$%.2f".format(lot.hourlyRate),
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Daily Cap",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = "$%.2f".format(lot.dailyCap),
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Status",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = lot.status,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (lot.status == "ACTIVE") {
                                MaterialTheme.colorScheme.tertiary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    }
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (lot.status == "ACTIVE") "Parking Lot Active" else "Parking Lot Inactive",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Switch(
                        checked = lot.status == "ACTIVE",
                        onCheckedChange = { isActive ->
                            onStatusChange(if (isActive) "ACTIVE" else "INACTIVE")
                        }
                    )
                }
            }
        }
    }
}
