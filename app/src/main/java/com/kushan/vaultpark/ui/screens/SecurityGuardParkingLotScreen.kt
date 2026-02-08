package com.kushan.vaultpark.ui.screens

import android.location.Geocoder
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.kushan.vaultpark.ui.theme.RoleTheme
import com.kushan.vaultpark.viewmodel.ParkingLotViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.util.Locale

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
    val context = LocalContext.current
    
    // Initialize OSM
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osm", 0))
    }
    
    var isCreating by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    
    // Form fields
    var lotName by remember { mutableStateOf("") }
    var locationAddress by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf<GeoPoint?>(null) }
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
                            onClick = { 
                                isCreating = true
                                // Default location (Colombo)
                                selectedLocation = GeoPoint(6.9271, 79.8612)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Create Parking Lot")
                        }
                    }
                }
            } else if (isCreating) {
                // Show creation form with Map
                ParkingLotFormMap(
                    title = "Create Your Parking Lot",
                    lotName = lotName,
                    onLotNameChange = { lotName = it },
                    locationAddress = locationAddress,
                    onLocationAddressChange = { locationAddress = it },
                    selectedLocation = selectedLocation,
                    onLocationSelected = { geoPoint, address -> 
                        selectedLocation = geoPoint
                        locationAddress = address
                    },
                    totalSpaces = totalSpaces,
                    onTotalSpacesChange = { totalSpaces = it },
                    hourlyRate = hourlyRate,
                    onHourlyRateChange = { hourlyRate = it },
                    dailyCap = dailyCap,
                    onDailyCapChange = { dailyCap = it },
                    isLoading = uiState.isLoading,
                    actionButtonText = "Create",
                    onActionClick = {
                        val spaces = totalSpaces.toIntOrNull() ?: 0
                        val rate = hourlyRate.toDoubleOrNull() ?: 0.0
                        val cap = dailyCap.toDoubleOrNull() ?: 0.0
                        
                        if (lotName.isBlank() || locationAddress.isBlank() || spaces <= 0 || rate <= 0 || cap <= 0 || selectedLocation == null) {
                            validationError = "Please fill all fields correctly and select a location on the map"
                            return@ParkingLotFormMap
                        }
                        
                        viewModel.createParkingLot(
                            securityGuardId = guardId,
                            securityGuardName = guardName,
                            name = lotName,
                            location = locationAddress,
                            latitude = selectedLocation!!.latitude,
                            longitude = selectedLocation!!.longitude,
                            totalSpaces = spaces,
                            hourlyRate = rate,
                            dailyCap = cap
                        )
                        isCreating = false
                        lotName = ""
                        locationAddress = ""
                        selectedLocation = null
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
                        locationAddress = uiState.myParkingLot!!.location
                        // Use existing lat/lon if available, else default
                        val lat = uiState.myParkingLot!!.latitude ?: 6.9271
                        val lon = uiState.myParkingLot!!.longitude ?: 79.8612
                        selectedLocation = GeoPoint(lat, lon)
                        
                        totalSpaces = uiState.myParkingLot!!.totalSpaces.toString()
                        hourlyRate = uiState.myParkingLot!!.hourlyRate.toString()
                        dailyCap = uiState.myParkingLot!!.dailyCap.toString()
                    },
                    onStatusChange = { newStatus ->
                        viewModel.toggleParkingLotStatus(uiState.myParkingLot!!.id, newStatus)
                    }
                )
            } else if (uiState.myParkingLot != null && isEditing) {
                // Show edit form with Map
                ParkingLotFormMap(
                    title = "Edit Parking Lot",
                    lotName = lotName,
                    onLotNameChange = { lotName = it },
                    locationAddress = locationAddress,
                    onLocationAddressChange = { locationAddress = it },
                    selectedLocation = selectedLocation,
                    onLocationSelected = { geoPoint, address -> 
                        selectedLocation = geoPoint
                        locationAddress = address
                    },
                    totalSpaces = totalSpaces,
                    onTotalSpacesChange = { totalSpaces = it },
                    hourlyRate = hourlyRate,
                    onHourlyRateChange = { hourlyRate = it },
                    dailyCap = dailyCap,
                    onDailyCapChange = { dailyCap = it },
                    isLoading = uiState.isLoading,
                    actionButtonText = "Update",
                    onActionClick = {
                        val spaces = totalSpaces.toIntOrNull() ?: 0
                        val rate = hourlyRate.toDoubleOrNull() ?: 0.0
                        val cap = dailyCap.toDoubleOrNull() ?: 0.0
                        
                        if (lotName.isBlank() || locationAddress.isBlank() || spaces <= 0 || rate <= 0 || cap <= 0 || selectedLocation == null) {
                            validationError = "Please fill all fields correctly"
                            return@ParkingLotFormMap
                        }
                        
                        viewModel.updateParkingLot(
                            lotId = uiState.myParkingLot!!.id,
                            name = lotName,
                            location = locationAddress,
                            latitude = selectedLocation!!.latitude,
                            longitude = selectedLocation!!.longitude,
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
private fun ParkingLotFormMap(
    title: String,
    lotName: String,
    onLotNameChange: (String) -> Unit,
    locationAddress: String,
    onLocationAddressChange: (String) -> Unit,
    selectedLocation: GeoPoint?,
    onLocationSelected: (GeoPoint, String) -> Unit,
    totalSpaces: String,
    onTotalSpacesChange: (String) -> Unit,
    hourlyRate: String,
    onHourlyRateChange: (String) -> Unit,
    dailyCap: String,
    onDailyCapChange: (String) -> Unit,
    isLoading: Boolean,
    actionButtonText: String,
    onActionClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    val context = LocalContext.current
    val geocoder = remember { Geocoder(context, Locale.getDefault()) }
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Map Section (Top)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color.LightGray)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(15.0)
                        
                        // Handle Lifecycle
                        val lifecycleObserver = object : DefaultLifecycleObserver {
                            override fun onResume(owner: LifecycleOwner) { super.onResume(owner); this@apply.onResume() }
                            override fun onPause(owner: LifecycleOwner) { super.onPause(owner); this@apply.onPause() }
                        }
                        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
                        
                        // Add Touch Overlay
                        val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                                p?.let { geoPoint ->
                                    // Reverse geocode
                                    scope.launch(Dispatchers.IO) {
                                        try {
                                            @Suppress("DEPRECATION")
                                            val addresses = geocoder.getFromLocation(geoPoint.latitude, geoPoint.longitude, 1)
                                            val addressText = if (!addresses.isNullOrEmpty()) {
                                                addresses[0].getAddressLine(0) ?: "Unknown Location"
                                            } else {
                                                "Lat: ${geoPoint.latitude}, Lon: ${geoPoint.longitude}"
                                            }
                                            
                                            withContext(Dispatchers.Main) {
                                                onLocationSelected(geoPoint, addressText)
                                            }
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) {
                                                onLocationSelected(geoPoint, "Lat: ${geoPoint.latitude}, Lon: ${geoPoint.longitude}")
                                            }
                                        }
                                    }
                                }
                                return true
                            }
                            override fun longPressHelper(p: GeoPoint?): Boolean = false
                        })
                        overlays.add(mapEventsOverlay)
                    }
                },
                update = { mapView ->
                    mapView.overlays.removeAll { it is Marker && it.title == "Selected Location" }
                    
                    selectedLocation?.let { location ->
                        mapView.controller.animateTo(location)
                        val marker = Marker(mapView).apply {
                            position = location
                            setTitle("Selected Location")
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        }
                        mapView.overlays.add(marker)
                    }
                    mapView.invalidate()
                }
            )
            
            // Overlay text if no location selected
            if (selectedLocation == null) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "Tap on map to set location",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
        
        // Form Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(vertical = 8.dp)
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
                value = locationAddress,
                onValueChange = onLocationAddressChange,
                label = { Text("Location Address") },
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
                    onClick = onActionClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(24.dp).width(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(actionButtonText)
                    }
                }
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
