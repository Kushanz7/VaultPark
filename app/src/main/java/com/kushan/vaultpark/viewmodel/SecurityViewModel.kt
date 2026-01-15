package com.kushan.vaultpark.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushan.vaultpark.data.repository.FirestoreRepository
import com.kushan.vaultpark.model.ParkingSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class SecurityViewModel(
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {
    
    private val _activeSessions = MutableStateFlow<List<ParkingSession>>(emptyList())
    val activeSessions: StateFlow<List<ParkingSession>> = _activeSessions.asStateFlow()
    
    private val _recentSessions = MutableStateFlow<List<ParkingSession>>(emptyList())
    val recentSessions: StateFlow<List<ParkingSession>> = _recentSessions.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _selectedGate = MutableStateFlow("Main Entrance")
    val selectedGate: StateFlow<String> = _selectedGate.asStateFlow()
    
    init {
        loadActiveSessions()
        loadRecentSessions()
    }
    
    fun loadActiveSessions() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val result = firestoreRepository.getRecentSessions(limit = 10)
                if (result.isSuccess) {
                    val sessions = result.getOrNull()?.filter { 
                        it.status == "ACTIVE" 
                    } ?: emptyList()
                    _activeSessions.value = sessions
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
    
    fun loadRecentSessions() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val result = firestoreRepository.getRecentSessions(limit = 5)
                if (result.isSuccess) {
                    _recentSessions.value = result.getOrNull() ?: emptyList()
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
    
    fun processQRScan(
        qrData: String,
        guardId: String,
        guardName: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                // In a real app, you would:
                // 1. Parse the QR data
                // 2. Find the corresponding parking session
                // 3. Update it with guard info
                // 4. Refresh the sessions list
                
                loadActiveSessions()
                loadRecentSessions()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun setSelectedGate(gate: String) {
        _selectedGate.value = gate
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    companion object {
        val GATE_LOCATIONS = listOf(
            "Main Entrance",
            "Exit Gate A",
            "Exit Gate B",
            "Visitor Parking",
            "Compact Parking"
        )
    }
}
