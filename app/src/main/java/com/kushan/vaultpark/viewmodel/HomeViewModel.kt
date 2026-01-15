package com.kushan.vaultpark.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var qrRefreshJob: Job? = null
    private var countdownJob: Job? = null
    private val QR_REFRESH_INTERVAL = 30_000L // 30 seconds

    init {
        loadUserData()
        generateNewQRCode()
        startQRRefreshTimer()
    }

    /**
     * Load mock user data
     */
    private fun loadUserData() {
        val mockUser = User(
            id = "USER_001",
            name = "Kushan Sharma",
            email = "kushan@vaultpark.com",
            phone = "+1-555-0123",
            vehicleNumber = "KA-01-AB-1234",
            membershipType = "Premium",
            createdDate = "2025-01-01"
        )
        _uiState.value = _uiState.value.copy(user = mockUser)
    }

    /**
     * Generate new QR code with secure hash
     */
    private fun generateNewQRCode() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingQR = true)
            
            try {
                val userId = _uiState.value.user?.id ?: "USER_UNKNOWN"
                val now = System.currentTimeMillis()
                val expiresAt = now + QR_REFRESH_INTERVAL
                
                // Generate QR code string
                val qrString = QRCodeUtils.generateQRCodeString(userId, now)
                
                // Create bitmap
                val bitmap = QRCodeUtils.generateQRCodeBitmap(qrString, size = 512)
                
                val qrCodeData = QRCodeData(
                    code = qrString,
                    generatedAt = now,
                    expiresAt = expiresAt,
                    userId = userId,
                    securityHash = qrString.split("|")[3]
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
