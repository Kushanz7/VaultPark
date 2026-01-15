package com.kushan.vaultpark.ui.screens

import android.Manifest
import android.graphics.Rect
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.kushan.vaultpark.data.repository.FirestoreRepository
import com.kushan.vaultpark.model.ParkingSession
import com.kushan.vaultpark.ui.components.CameraPreview
import com.kushan.vaultpark.ui.components.ErrorDialog
import com.kushan.vaultpark.ui.components.QRScanResultDialog
import com.kushan.vaultpark.viewmodel.QRScannerViewModel
import com.kushan.vaultpark.viewmodel.ScanState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SecurityScannerScreen(
    currentGuardId: String,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val firestoreRepository = remember { FirestoreRepository() }
    val qrScannerViewModel: QRScannerViewModel = remember {
        QRScannerViewModel(context.applicationContext as android.app.Application, firestoreRepository, currentGuardId)
    }
    
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    
    val scanState by qrScannerViewModel.scanState.collectAsState()
    val selectedGate by qrScannerViewModel.selectedGate.collectAsState()
    val isFlashEnabled by qrScannerViewModel.isFlashEnabled.collectAsState()
    val recentScans by qrScannerViewModel.recentScans.collectAsState()
    
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var lastScannedSession by remember { mutableStateOf<ParkingSession?>(null) }
    var lastErrorMessage by remember { mutableStateOf("") }
    
    LaunchedEffect(scanState) {
        when (scanState) {
            is ScanState.Success -> {
                lastScannedSession = (scanState as ScanState.Success).session
                showSuccessDialog = true
                showErrorDialog = false
            }
            is ScanState.Error -> {
                lastErrorMessage = (scanState as ScanState.Error).message
                showErrorDialog = true
                showSuccessDialog = false
            }
            else -> {
                showSuccessDialog = false
                showErrorDialog = false
            }
        }
    }
    
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
        qrScannerViewModel.loadRecentScans()
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (cameraPermissionState.status.isGranted) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header with gate selector
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "QR Scanner",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    GateSelector(
                        selectedGate = selectedGate,
                        onGateSelected = { qrScannerViewModel.setSelectedGate(it) }
                    )
                }
                
                // Camera Preview
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    CameraPreview(
                        onQRCodeDetected = { qrCode, _ ->
                            qrScannerViewModel.scanQRCode(qrCode)
                        },
                        isFlashEnabled = isFlashEnabled,
                        onFlashToggle = { qrScannerViewModel.toggleFlash() }
                    )
                    
                    // Scanning status indicator
                    when (scanState) {
                        is ScanState.Processing -> {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(16.dp)
                                    .background(
                                        Color(0xFFFFC107),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "Processing...",
                                    color = Color.Black,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        else -> {}
                    }
                }
                
                // Recent scans list
                if (recentScans.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Recent Scans",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(recentScans.take(5)) { session ->
                                RecentScanItem(session)
                            }
                        }
                    }
                }
            }
        } else {
            // Permission not granted
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Camera Permission Required",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Camera access is needed to scan QR codes",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = { cameraPermissionState.launchPermissionRequest() }
                ) {
                    Text("Grant Permission")
                }
            }
        }
        
        // Success Dialog
        if (showSuccessDialog && lastScannedSession != null) {
            QRScanResultDialog(
                session = lastScannedSession!!,
                onDismiss = {
                    showSuccessDialog = false
                    qrScannerViewModel.resetScanState()
                }
            )
        }
        
        // Error Dialog
        if (showErrorDialog) {
            ErrorDialog(
                errorMessage = lastErrorMessage,
                onDismiss = {
                    showErrorDialog = false
                    qrScannerViewModel.resetScanState()
                }
            )
        }
    }
}

@Composable
private fun GateSelector(
    selectedGate: String,
    onGateSelected: (String) -> Unit
) {
    val gates = listOf("Main Entrance", "Exit Gate A", "Exit Gate B")
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        TextButton(
            onClick = { expanded = true },
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedGate,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            gates.forEach { gate ->
                DropdownMenuItem(
                    text = { Text(gate) },
                    onClick = {
                        onGateSelected(gate)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun RecentScanItem(session: ParkingSession) {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val entryTime = dateFormat.format(Date(session.entryTime))
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = session.vehicleNumber,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = entryTime,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Text(
            text = if (session.exitTime != null) "Exit" else "Entry",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (session.exitTime != null) Color(0xFF4CAF50) else Color(0xFF2196F3),
            modifier = Modifier
                .background(
                    if (session.exitTime != null) Color(0xFF4CAF50).copy(alpha = 0.2f)
                    else Color(0xFF2196F3).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}
