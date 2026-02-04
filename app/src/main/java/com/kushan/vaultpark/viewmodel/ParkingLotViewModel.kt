package com.kushan.vaultpark.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushan.vaultpark.util.ParkingLotFirestoreQueries
import com.kushan.vaultpark.model.ParkingLot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

data class ParkingLotUIState(
    val isLoading: Boolean = false,
    val myParkingLot: ParkingLot? = null,
    val availableLots: List<ParkingLot> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class ParkingLotViewModel : ViewModel() {
    companion object {
        private const val TAG = "ParkingLotViewModel"
    }

    private val _uiState = MutableStateFlow(ParkingLotUIState())
    val uiState: StateFlow<ParkingLotUIState> = _uiState.asStateFlow()

    /**
     * Load the parking lot for a security guard
     */
    fun loadParkingLot(securityGuardId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val lot = ParkingLotFirestoreQueries.getParkingLotByGuardId(securityGuardId)
                _uiState.value = _uiState.value.copy(
                    myParkingLot = lot,
                    isLoading = false
                )
                Log.d(TAG, "Loaded parking lot for guard: ${lot?.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading parking lot", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to load parking lot: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    /**
     * Create a new parking lot for a security guard
     */
    fun createParkingLot(
        securityGuardId: String,
        securityGuardName: String,
        name: String,
        location: String,
        totalSpaces: Int,
        hourlyRate: Double,
        dailyCap: Double
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val result = ParkingLotFirestoreQueries.createParkingLot(
                    securityGuardId = securityGuardId,
                    securityGuardName = securityGuardName,
                    name = name,
                    location = location,
                    totalSpaces = totalSpaces,
                    hourlyRate = hourlyRate,
                    dailyCap = dailyCap
                )

                if (result.isSuccess) {
                    val createdLot = result.getOrNull()
                    _uiState.value = _uiState.value.copy(
                        myParkingLot = createdLot,
                        successMessage = "Parking lot created successfully",
                        isLoading = false
                    )
                    Log.d(TAG, "Parking lot created: ${createdLot?.id}")
                } else {
                    throw result.exceptionOrNull() ?: Exception("Unknown error")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating parking lot", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to create parking lot: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    /**
     * Update parking lot details
     */
    fun updateParkingLot(
        lotId: String,
        name: String,
        location: String,
        totalSpaces: Int,
        hourlyRate: Double,
        dailyCap: Double
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val result = ParkingLotFirestoreQueries.updateParkingLot(
                    parkingLotId = lotId,
                    name = name,
                    location = location,
                    totalSpaces = totalSpaces,
                    hourlyRate = hourlyRate,
                    dailyCap = dailyCap
                )

                if (result.isSuccess) {
                    // Reload the parking lot
                    val lot = ParkingLotFirestoreQueries.getParkingLotById(lotId)
                    _uiState.value = _uiState.value.copy(
                        myParkingLot = lot,
                        successMessage = "Parking lot updated successfully",
                        isLoading = false
                    )
                    Log.d(TAG, "Parking lot updated: $lotId")
                } else {
                    throw result.exceptionOrNull() ?: Exception("Unknown error")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating parking lot", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to update parking lot: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    /**
     * Load all active parking lots (for drivers to select from)
     */
    fun loadAvailableLots() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val lots = ParkingLotFirestoreQueries.getAllActiveParkingLots()
                _uiState.value = _uiState.value.copy(
                    availableLots = lots,
                    isLoading = false
                )
                Log.d(TAG, "Loaded ${lots.size} available parking lots")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading available lots", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to load parking lots: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    /**
     * Toggle parking lot status
     */
    fun toggleParkingLotStatus(lotId: String, newStatus: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val result = ParkingLotFirestoreQueries.updateParkingLot(
                    parkingLotId = lotId,
                    status = newStatus
                )

                if (result.isSuccess) {
                    // Reload the parking lot
                    val lot = ParkingLotFirestoreQueries.getParkingLotById(lotId)
                    _uiState.value = _uiState.value.copy(
                        myParkingLot = lot,
                        successMessage = "Status updated successfully",
                        isLoading = false
                    )
                    Log.d(TAG, "Parking lot status updated: $lotId -> $newStatus")
                } else {
                    throw result.exceptionOrNull() ?: Exception("Unknown error")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling parking lot status", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to update status: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    /**
     * Clear messages
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
}
