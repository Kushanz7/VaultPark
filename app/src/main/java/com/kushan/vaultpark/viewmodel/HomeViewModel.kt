package com.kushan.vaultpark.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.kushan.vaultpark.data.repository.FirestoreRepository
import com.kushan.vaultpark.model.ParkingStatus
import com.kushan.vaultpark.model.QRCodeData
import com.kushan.vaultpark.model.User
import com.kushan.vaultpark.utils.QRCodeUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val user: User? = null,
    val qrCodeData: QRCodeData? = null,
    val qrCodeBitmap: Bitmap? = null,
    val parkingStatus: ParkingStatus = ParkingStatus(isParked = false),
    val secondsUntilRefresh: Int = 30,
    val isLoadingQR: Boolean = false,
    val error: String? = null
)

class HomeViewModel(
    private val firestoreRepository: FirestoreRepository = FirestoreRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var qrRefreshJob: Job? = null
    private var countdownJob: Job? = null
    private val QR_REFRESH_INTERVAL = 30_000L // 30 seconds

    init {
        loadUserData()
        generateNewQRCode()
        startQRRefreshTimer()
        loadActiveParkingSession()
    }

    /**
     * Load user data from Firestore using Firebase Auth
     */
    private fun loadUserData() {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val user = firestoreRepository.getUserById(currentUser.uid)
                    if (user != null) {
                        _uiState.value = _uiState.value.copy(user = user)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            error = "User profile not found. Please complete your profile."
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Please log in to generate QR code"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load user data: ${e.message}"
                )
            }
        }
    }

    /**
     * Load active parking session from database
     */
    private fun loadActiveParkingSession() {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val activeSession = firestoreRepository.getActiveSessionForDriver(currentUser.uid)
                    if (activeSession != null) {
                        // Format the entry time
                        val parkedSince = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                            .format(java.util.Date(activeSession.entryTime))
                        
                        val parkingStatus = ParkingStatus(
                            isParked = true,
                            parkedSince = parkedSince,
                            location = activeSession.gateLocation
                        )
                        _uiState.value = _uiState.value.copy(parkingStatus = parkingStatus)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            parkingStatus = ParkingStatus(isParked = false)
                        )
                    }
                }
            } catch (e: Exception) {
                // Silent fail for parking status check
            }
        }
    }

    /**
     * Generate new QR code with secure hash
     */
    private fun generateNewQRCode() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingQR = true)
            
            try {
                val user = _uiState.value.user
                val userId = user?.id ?: "USER_UNKNOWN"
                val vehicleNumber = user?.vehicleNumber ?: "UNKNOWN"
                val now = System.currentTimeMillis()
                val expiresAt = now + QR_REFRESH_INTERVAL
                
                // Generate QR code string
                val qrString = QRCodeUtils.generateQRCodeString(userId, vehicleNumber, now)
                
                // Create bitmap
                val bitmap = QRCodeUtils.generateQRCodeBitmap(qrString, size = 512)
                
                val qrCodeData = QRCodeData(
                    code = qrString,
                    generatedAt = now,
                    expiresAt = expiresAt,
                    userId = userId,
                    securityHash = qrString.split("|")[4]
                )
                
                _uiState.value = _uiState.value.copy(
                    qrCodeData = qrCodeData,
                    qrCodeBitmap = bitmap,
                    isLoadingQR = false,
                    secondsUntilRefresh = 30
                )
                
                // Start countdown timer
                startCountdownTimer()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingQR = false,
                    error = "Failed to generate QR code: ${e.message}"
                )
            }
        }
    }

    /**
     * Start countdown timer for QR refresh
     */
    private fun startCountdownTimer() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            var seconds = 30
            while (seconds > 0) {
                delay(1000)
                seconds--
                _uiState.value = _uiState.value.copy(secondsUntilRefresh = seconds)
            }
        }
    }

    /**
     * Start automatic QR code refresh timer (every 30 seconds)
     */
    private fun startQRRefreshTimer() {
        qrRefreshJob?.cancel()
        qrRefreshJob = viewModelScope.launch {
            while (true) {
                delay(QR_REFRESH_INTERVAL)
                generateNewQRCode()
            }
        }
    }

    /**
     * Manually refresh QR code
     */
    fun refreshQRCode() {
        countdownJob?.cancel()
        generateNewQRCode()
        loadActiveParkingSession() // Also check for active parking session
    }

    /**
     * Update parking status
     */
    fun updateParkingStatus(isParked: Boolean, parkedSince: String? = null) {
        val newStatus = ParkingStatus(
            isParked = isParked,
            parkedSince = parkedSince,
            location = if (isParked) "Zone A - Level 3" else null
        )
        _uiState.value = _uiState.value.copy(parkingStatus = newStatus)
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    override fun onCleared() {
        super.onCleared()
        qrRefreshJob?.cancel()
        countdownJob?.cancel()
    }
}
