package com.kushan.vaultpark.viewmodel

import android.app.Application
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kushan.vaultpark.data.repository.FirestoreRepository
import com.kushan.vaultpark.model.ParkingSession
import com.kushan.vaultpark.model.SessionStatus
import com.kushan.vaultpark.model.User
import com.kushan.vaultpark.util.ParsedQRCode
import com.kushan.vaultpark.util.QRCodeParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

sealed class ScanState {
    object Idle : ScanState()
    object Scanning : ScanState()
    object Processing : ScanState()
    data class Success(val session: ParkingSession) : ScanState()
    data class Error(val message: String) : ScanState()
}

class QRScannerViewModel(
    application: Application,
    private val firestoreRepository: FirestoreRepository,
    val currentGuardId: String
) : AndroidViewModel(application) {
    
    private val vibrator = getVibrator(application)
    
    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()
    
    private val _selectedGate = MutableStateFlow("Main Entrance")
    val selectedGate: StateFlow<String> = _selectedGate.asStateFlow()
    
    private val _recentScans = MutableStateFlow<List<ParkingSession>>(emptyList())
    val recentScans: StateFlow<List<ParkingSession>> = _recentScans.asStateFlow()
    
    private val _isFlashEnabled = MutableStateFlow(false)
    val isFlashEnabled: StateFlow<Boolean> = _isFlashEnabled.asStateFlow()
    
    private var lastScannedQR: String? = null
    private var lastScannedTime: Long = 0
    private val scanDebounceMs = 3000L
    
    fun setSelectedGate(gate: String) {
        _selectedGate.value = gate
    }
    
    fun toggleFlash() {
        _isFlashEnabled.value = !_isFlashEnabled.value
    }
    
    fun scanQRCode(qrString: String) {
        // Prevent scanning if already processing or showing result
        if (_scanState.value !is ScanState.Idle) {
            return
        }
        
        // Debounce: prevent scanning same QR code within 3 seconds
        if (qrString == lastScannedQR && System.currentTimeMillis() - lastScannedTime < scanDebounceMs) {
            return
        }
        
        lastScannedQR = qrString
        lastScannedTime = System.currentTimeMillis()
        
        viewModelScope.launch {
            _scanState.value = ScanState.Processing
            
            try {
                // Parse QR code
                val parsed = QRCodeParser.parseQRCode(qrString)
                if (!parsed.isValid) {
                    vibrator?.vibrate(longArrayOf(0, 100, 100, 100), -1)
                    _scanState.value = ScanState.Error(parsed.validationError ?: "Invalid QR code")
                    resetScanAfterDelay(2000)
                    return@launch
                }
                
                // Check if QR code is expired
                if (QRCodeParser.isExpired(parsed)) {
                    vibrator?.vibrate(longArrayOf(0, 100, 100, 100), -1)
                    _scanState.value = ScanState.Error("QR code expired. Please request a new one")
                    resetScanAfterDelay(2000)
                    return@launch
                }
                
                // Check if driver exists
                val driver = firestoreRepository.getUserById(parsed.userId)
                if (driver == null) {
                    vibrator?.vibrate(longArrayOf(0, 100, 100, 100), -1)
                    _scanState.value = ScanState.Error("Driver not found in system")
                    resetScanAfterDelay(2000)
                    return@launch
                }
                
                // Check for active session
                val activeSession = firestoreRepository.getActiveSessionForDriver(parsed.userId)
                
                if (activeSession != null) {
                    // Exit scan
                    handleExitScan(activeSession, parsed)
                } else {
                    // Entry scan
                    handleEntryScan(driver, parsed)
                }
                
                vibrator?.vibrate(longArrayOf(0, 50, 100, 50), -1)
                
            } catch (e: Exception) {
                vibrator?.vibrate(longArrayOf(0, 100, 100, 100), -1)
                _scanState.value = ScanState.Error("Error: ${e.message}")
                resetScanAfterDelay(2000)
            }
        }
    }
    
    private suspend fun handleEntryScan(driver: User, parsedQR: ParsedQRCode) {
        try {
            val newSession = ParkingSession(
                driverId = driver.id,
                driverName = driver.name,
                vehicleNumber = parsedQR.vehicleNumber,
                entryTime = System.currentTimeMillis(),
                gateLocation = _selectedGate.value,
                scannedByGuardId = currentGuardId,
                status = SessionStatus.ACTIVE.name
            )
            
            val savedSession = firestoreRepository.createParkingSession(newSession)
            
            _scanState.value = ScanState.Success(savedSession)
            loadRecentScans()
        } catch (e: Exception) {
            _scanState.value = ScanState.Error("Failed to create parking session: ${e.message}")
            resetScanAfterDelay(2000)
        }
    }
    
    private suspend fun handleExitScan(activeSession: ParkingSession, parsedQR: ParsedQRCode) {
        try {
            // Validate that it's the same vehicle
            if (activeSession.vehicleNumber != parsedQR.vehicleNumber) {
                _scanState.value = ScanState.Error("Vehicle number mismatch. Expected ${activeSession.vehicleNumber}")
                resetScanAfterDelay(2000)
                return
            }
            
            val exitTime = System.currentTimeMillis()
            val duration = exitTime - activeSession.entryTime
            val durationMinutes = TimeUnit.MILLISECONDS.toMinutes(duration)
            
            val updatedSession = activeSession.copy(
                exitTime = exitTime,
                status = SessionStatus.COMPLETED.name
            )
            
            firestoreRepository.updateParkingSession(updatedSession)
            
            _scanState.value = ScanState.Success(updatedSession)
            loadRecentScans()
        } catch (e: Exception) {
            _scanState.value = ScanState.Error("Failed to complete parking session: ${e.message}")
            resetScanAfterDelay(2000)
        }
    }
    
    fun loadRecentScans() {
        viewModelScope.launch {
            try {
                val scans = firestoreRepository.getRecentSessions(limit = 10L)
                _recentScans.value = scans
            } catch (e: Exception) {
                // Silent fail for recent scans
            }
        }
    }
    
    fun resetScanState() {
        _scanState.value = ScanState.Idle
        lastScannedQR = null
    }
    
    private fun resetScanAfterDelay(delayMs: Long) {
        viewModelScope.launch {
            kotlinx.coroutines.delay(delayMs)
            if (_scanState.value !is ScanState.Idle) {
                _scanState.value = ScanState.Idle
            }
        }
    }
    
    private fun getVibrator(application: Application): Vibrator? {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val vibratorManager = application.getSystemService(VibratorManager::class.java)
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                application.getSystemService(Vibrator::class.java)
            }
        } catch (e: Exception) {
            null
        }
    }
}
