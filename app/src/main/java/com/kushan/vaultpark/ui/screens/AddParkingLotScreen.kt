package com.kushan.vaultpark.ui.screens

import android.location.Geocoder
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kushan.vaultpark.ui.theme.Poppins
import com.kushan.vaultpark.viewmodel.AddParkingLotEvent
import com.kushan.vaultpark.viewmodel.AddParkingLotViewModel
import com.kushan.vaultpark.utils.MapUtils
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
fun AddParkingLotScreen(
    onBackClick: () -> Unit,
    viewModel: AddParkingLotViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val geocoder = remember { Geocoder(context, Locale.getDefault()) }
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Initialize OSM Configuration
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osm", 0))
    }
    
    // Success Navigation
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onBackClick() // Or navigate to success screen
        }
    }

    // Effect to reverse geocode when location selected
    LaunchedEffect(uiState.selectedLocation) {
        uiState.selectedLocation?.let { geoPoint ->
            viewModel.reverseGeocodeLocation(geoPoint, geocoder)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add Parking Lot", fontFamily = Poppins) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.onEvent(AddParkingLotEvent.SaveClicked) },
                icon = { Icon(Icons.Default.Save, contentDescription = null) },
                text = { Text("Save Parking Lot", fontFamily = Poppins) },
                expanded = uiState.selectedLocation != null && uiState.name.isNotBlank()
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Map Section (Top Half)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            controller.setZoom(15.0)
                            
                            // Initialize map center (try to get user location or default)
                            // We can use runBlocking or similar if we need immediate location, 
                            // but better to default and then update.
                            controller.setCenter(GeoPoint(6.9271, 79.8612)) // Default Colombo
                            
                            // Handle Lifecycle
                            val lifecycleObserver = object : DefaultLifecycleObserver {
                                override fun onResume(owner: LifecycleOwner) { super.onResume(owner); this@apply.onResume() }
                                override fun onPause(owner: LifecycleOwner) { super.onPause(owner); this@apply.onPause() }
                            }
                            lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
                            
                            // Add Touch Overlay for clicks
                            val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                                override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                                    p?.let {
                                        viewModel.onEvent(AddParkingLotEvent.LocationSelected(it))
                                    }
                                    return true
                                }
                                override fun longPressHelper(p: GeoPoint?): Boolean = false
                            })
                            overlays.add(mapEventsOverlay)
                        }
                    },
                    update = { mapView ->
                        // Update Markers based on state
                        // Remove old "Selected Location" markers (keep events overlay)
                        mapView.overlays.removeAll { it is Marker && it.title == "Selected Location" }
                        
                        uiState.selectedLocation?.let { location ->
                            val marker = Marker(mapView).apply {
                                position = location
                                title = "Selected Location"
                                snippet = uiState.address
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            }
                            mapView.overlays.add(marker)
                            mapView.controller.animateTo(location)
                        }
                        
                        mapView.invalidate()
                    }
                )
                
                // Overlay text if no location selected
                if (uiState.selectedLocation == null) {
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                OutlinedTextField(
                    value = uiState.address,
                    onValueChange = { viewModel.onEvent(AddParkingLotEvent.AddressChanged(it)) },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = { Icon(Icons.Default.PinDrop, contentDescription = null) }
                )

                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = { viewModel.onEvent(AddParkingLotEvent.NameChanged(it)) },
                    label = { Text("Parking Lot Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.totalSpaces,
                        onValueChange = { viewModel.onEvent(AddParkingLotEvent.SpacesChanged(it)) },
                        label = { Text("Total Spots") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = uiState.hourlyRate,
                        onValueChange = { viewModel.onEvent(AddParkingLotEvent.RateChanged(it)) },
                        label = { Text("Rate ($/hr)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }
                
                Text(
                    text = "Facilities",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val facilities = listOf("24/7", "CCTV", "Covered", "EV Charging")
                    facilities.forEach { facility ->
                        FilterChip(
                            selected = uiState.facilities.contains(facility),
                            onClick = { viewModel.onEvent(AddParkingLotEvent.FacilityToggled(facility)) },
                            label = { Text(facility) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
            }
        }
    }
}
