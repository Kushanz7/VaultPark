package com.kushan.vaultpark.viewmodel

import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kushan.vaultpark.model.ParkingLot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import java.util.Date

class AddParkingLotViewModel : ViewModel() {
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    private val _uiState = MutableStateFlow(AddParkingLotUiState())
    val uiState: StateFlow<AddParkingLotUiState> = _uiState.asStateFlow()

    fun onEvent(event: AddParkingLotEvent) {
        when (event) {
            is AddParkingLotEvent.NameChanged -> {
                _uiState.value = _uiState.value.copy(name = event.name)
            }
            is AddParkingLotEvent.SpacesChanged -> {
                _uiState.value = _uiState.value.copy(totalSpaces = event.spaces)
            }
            is AddParkingLotEvent.RateChanged -> {
                _uiState.value = _uiState.value.copy(hourlyRate = event.rate)
            }
            is AddParkingLotEvent.FacilityToggled -> {
                val currentFacilities = _uiState.value.facilities.toMutableList()
                if (currentFacilities.contains(event.facility)) {
                    currentFacilities.remove(event.facility)
                } else {
                    currentFacilities.add(event.facility)
                }
                _uiState.value = _uiState.value.copy(facilities = currentFacilities)
            }
            is AddParkingLotEvent.LocationSelected -> {
                _uiState.value = _uiState.value.copy(
                    selectedLocation = event.location
                )
            }
            is AddParkingLotEvent.AddressChanged -> {
                _uiState.value = _uiState.value.copy(address = event.address)
            }
            AddParkingLotEvent.SaveClicked -> saveParkingLot()
        }
    }

    fun reverseGeocodeLocation(location: GeoPoint, geocoder: Geocoder) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // GeoPoint uses latitude, longitude just like LatLng
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val addressText = address.getAddressLine(0)
                    withContext(Dispatchers.Main) {
                        _uiState.value = _uiState.value.copy(address = addressText)
                    }
                }
            } catch (e: Exception) {
                Log.e("AddParkingLotViewModel", "Geocoding error", e)
            }
        }
    }

    private fun saveParkingLot() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val user = auth.currentUser
                if (user == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "User not authenticated"
                    )
                    return@launch
                }

                val state = _uiState.value
                val newLot = ParkingLot(
                    id = firestore.collection("parkingLots").document().id,
                    securityGuardId = user.uid,
                    securityGuardName = user.displayName ?: "Admin",
                    name = state.name,
                    location = state.address,
                    latitude = state.selectedLocation?.latitude ?: 0.0,
                    longitude = state.selectedLocation?.longitude ?: 0.0,
                    totalSpaces = state.totalSpaces.toIntOrNull() ?: 0,
                    availableSpaces = state.totalSpaces.toIntOrNull() ?: 0,
                    hourlyRate = state.hourlyRate.toDoubleOrNull() ?: 0.0,
                    facilities = state.facilities,
                    status = "ACTIVE",
                    createdAt = Date(),
                    updatedAt = Date()
                )

                firestore.collection("parkingLots")
                    .document(newLot.id)
                    .set(newLot)
                    .await()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to save parking lot"
                )
            }
        }
    }
}

data class AddParkingLotUiState(
    val name: String = "",
    val totalSpaces: String = "",
    val hourlyRate: String = "",
    val facilities: List<String> = emptyList(),
    val address: String = "",
    val selectedLocation: GeoPoint? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

sealed class AddParkingLotEvent {
    data class NameChanged(val name: String) : AddParkingLotEvent()
    data class SpacesChanged(val spaces: String) : AddParkingLotEvent()
    data class RateChanged(val rate: String) : AddParkingLotEvent()
    data class FacilityToggled(val facility: String) : AddParkingLotEvent()
    data class LocationSelected(val location: GeoPoint) : AddParkingLotEvent()
    data class AddressChanged(val address: String) : AddParkingLotEvent()
    object SaveClicked : AddParkingLotEvent()
}
