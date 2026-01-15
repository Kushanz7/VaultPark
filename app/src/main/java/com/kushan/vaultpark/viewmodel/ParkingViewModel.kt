package com.kushan.vaultpark.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushan.vaultpark.data.repository.FirestoreRepository
import com.kushan.vaultpark.model.ParkingSession
import com.kushan.vaultpark.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.concurrent.TimeUnit

class ParkingViewModel(
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {
    
    private val _activeSession = MutableStateFlow<ParkingSession?>(null)
    val activeSession: StateFlow<ParkingSession?> = _activeSession.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _sessionDuration = MutableStateFlow("0m")
    val sessionDuration: StateFlow<String> = _sessionDuration.asStateFlow()
    
    fun observeActiveSession(driverId: String) {
        viewModelScope.launch {
            try {
                // First, get the current session
                val result = firestoreRepository.getActiveSessionForDriverLegacy(driverId)
                if (result.isSuccess) {
                    _activeSession.value = result.getOrNull()
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }
    
    fun startParkingSession(
        driverId: String,
        driverName: String,
        vehicleNumber: String,
        gateLocation: String,
        qrCodeData: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val result = firestoreRepository.createParkingSessionLegacy2(
                    driverId = driverId,
                    driverName = driverName,
                    vehicleNumber = vehicleNumber,
                    gateLocation = gateLocation,
                    qrCodeData = qrCodeData
                )
                
                if (result.isSuccess) {
                    // Refresh the active session
                    observeActiveSession(driverId)
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun endParkingSession(
        sessionId: String,
        driverId: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val session = _activeSession.value
                if (session != null && session.entryTime > 0L) {
                    val duration = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(
                        System.currentTimeMillis() - session.entryTime
                    )
                    
                    val result = firestoreRepository.updateParkingSessionLegacy2(
                        sessionId,
                        System.currentTimeMillis(),
                        duration
                    )
                    
                    if (result.isSuccess) {
                        _activeSession.value = null
                        observeActiveSession(driverId)
                    } else {
                        _errorMessage.value = result.exceptionOrNull()?.message
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateSessionDuration() {
        viewModelScope.launch {
            val session = _activeSession.value
            if (session != null && session.entryTime > 0L) {
                val diffMs = System.currentTimeMillis() - session.entryTime
                val minutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(diffMs)
                val hours = minutes / 60
                val mins = minutes % 60
                
                _sessionDuration.value = when {
                    hours > 0 -> "${hours}h ${mins}m"
                    else -> "${mins}m"
                }
            }
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
}
