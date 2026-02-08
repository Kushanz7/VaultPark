package com.kushan.vaultpark.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.kushan.vaultpark.model.ParkingLot
import com.kushan.vaultpark.ui.theme.Poppins
import com.kushan.vaultpark.ui.theme.RoleTheme
import com.kushan.vaultpark.ui.theme.StatusActive
import com.kushan.vaultpark.ui.theme.StatusError
import com.kushan.vaultpark.viewmodel.ParkingLotsMapEvent
import com.kushan.vaultpark.viewmodel.ParkingLotsMapViewModel
import com.kushan.vaultpark.viewmodel.ParkingLotsMapUiState
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ParkingLotsMapScreen(
    onMenuClick: () -> Unit,
    viewModel: ParkingLotsMapViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Initialize OSM Configuration
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osm", 0))
    }
    
    // Permission Handling
    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    
    LaunchedEffect(Unit) {
        if (!locationPermissionState.allPermissionsGranted) {
            locationPermissionState.launchMultiplePermissionRequest()
        } else {
            viewModel.requestCurrentLocation(context)
        }
    }
    
    LaunchedEffect(locationPermissionState.allPermissionsGranted) {
        if (locationPermissionState.allPermissionsGranted) {
            viewModel.requestCurrentLocation(context)
        }
    }

    // Bottom Sheet State
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.selectedParkingLot) {
        if (uiState.selectedParkingLot != null) {
            showBottomSheet = true
        } else {
            showBottomSheet = false
        }
    }
    
    // Map View Reference to control programmatically
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    
    // Update map center when user location changes (initially)
    LaunchedEffect(uiState.userLocation) {
        uiState.userLocation?.let { loc ->
            // Only animate if no lot is selected, to avoid disrupting user
            if (uiState.selectedParkingLot == null) {
                mapViewRef?.controller?.animateTo(loc)
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            if (uiState.selectedParkingLot == null) {
                FloatingActionButton(
                    onClick = { 
                        viewModel.requestCurrentLocation(context)
                        uiState.userLocation?.let { 
                             mapViewRef?.controller?.animateTo(it)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.LocationOn, "My Location")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(15.0)
                        
                        // Default center
                        controller.setCenter(GeoPoint(6.9271, 79.8612)) 
                        
                        // Lifecycle
                        val lifecycleObserver = object : DefaultLifecycleObserver {
                            override fun onResume(owner: LifecycleOwner) { super.onResume(owner); this@apply.onResume() }
                            override fun onPause(owner: LifecycleOwner) { super.onPause(owner); this@apply.onPause() }
                        }
                        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
                        
                        mapViewRef = this
                    }
                },
                update = { mapView ->
                    // Clear old overlays (except maybe user location if we managed it separately, but simple is clear all)
                    // Be careful not to clear custom click listeners if they are overlays
                    // For simplicity, we can recreate markers.
                    // Ideally, we manage markers more efficiently, but for this size, recreation is fine.
                    
                    // Keep existing events overlay if any, or just clear markers
                    // Let's filter to remove only Markers
                    mapView.overlays.removeAll { it is Marker }
                    
                    // User Location Marker
                    uiState.userLocation?.let { loc ->
                        val marker = Marker(mapView).apply {
                            position = loc
                            title = "Me"
                            // Custom icon can be set here: icon = ...
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        }
                        mapView.overlays.add(marker)
                    }
                    
                    // Parking Lot Markers
                    uiState.parkingLots.forEach { lot ->
                         val marker = Marker(mapView).apply {
                            position = GeoPoint(lot.latitude, lot.longitude)
                            title = lot.name
                            snippet = "${lot.availableSpaces} spots available"
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            
                            // Determine color/icon based on availability (placeholder logic)
                            // In OSMDroid, we need Drawables. For now, default marker is used.
                            // To change color, we'd need custom Drawables.
                            
                            setOnMarkerClickListener { _, _ ->
                                viewModel.onEvent(ParkingLotsMapEvent.ParkingLotSelected(lot))
                                true
                            }
                        }
                        mapView.overlays.add(marker)
                    }
                    
                    mapView.invalidate()
                }
            )

            // Top Search Bar
            SearchBar(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                onMenuClick = onMenuClick
            )

            // Bottom Sheet for Details
            if (showBottomSheet && uiState.selectedParkingLot != null) {
                ModalBottomSheet(
                    onDismissRequest = { 
                        viewModel.onEvent(ParkingLotsMapEvent.ClearSelection)
                    },
                    sheetState = sheetState
                ) {
                    ParkingLotDetailsContent(
                        lot = uiState.selectedParkingLot!!,
                        distance = uiState.distance,
                        duration = uiState.duration,
                        onNavigate = { lot ->
                            // Open external maps
                            val uri = Uri.parse("geo:${lot.latitude},${lot.longitude}?q=${lot.latitude},${lot.longitude}(${lot.name})")
                            val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                            // Try to find a map app
                            if (mapIntent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(mapIntent)
                            } else {
                                // Fallback to browser
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${lot.latitude},${lot.longitude}"))
                                context.startActivity(browserIntent)
                            }
                        },
                        onClose = {
                            viewModel.onEvent(ParkingLotsMapEvent.ClearSelection)
                        }
                    )
                }
            }
        }
    }
}



@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenuClick, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Search parking...",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun ParkingLotDetailsContent(
    lot: ParkingLot,
    distance: String?,
    duration: String?,
    onNavigate: (ParkingLot) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lot.name,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                Text(
                    text = lot.location, // Address
                    fontFamily = Poppins,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, "Close")
            }
        }
        
        // Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Availability
            Column {
                Text("Available", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "${lot.availableSpaces}/${lot.totalSpaces}",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (lot.availableSpaces > 0) StatusActive else StatusError,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Rate
            Column {
                Text("Rate", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "$${lot.hourlyRate}/hr",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Distance
            if (distance != null) {
                Column {
                    Text("Distance", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        distance,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // Facilities
        if (lot.facilities.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                lot.facilities.forEach {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = it,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Actions
        Button(
            onClick = { onNavigate(lot) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RoleTheme.driverColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Directions, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Navigate" + (if (duration != null) " ($duration)" else ""))
        }
    }
}
