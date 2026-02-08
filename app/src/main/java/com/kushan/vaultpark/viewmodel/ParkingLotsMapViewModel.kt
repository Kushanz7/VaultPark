package com.kushan.vaultpark.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kushan.vaultpark.model.ParkingLot
import com.kushan.vaultpark.utils.MapUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.osmdroid.util.GeoPoint

class ParkingLotsMapViewModel : ViewModel() {
    private val firestore = Firebase.firestore
    
    // No Retrofit needed for OSM/External Nav

    private val _uiState = MutableStateFlow(ParkingLotsMapUiState())
    val uiState: StateFlow<ParkingLotsMapUiState> = _uiState.asStateFlow()

    init {
        fetchParkingLots()
    }

    fun onEvent(event: ParkingLotsMapEvent) {
        when (event) {
            is ParkingLotsMapEvent.ParkingLotSelected -> {
                _uiState.value = _uiState.value.copy(selectedParkingLot = event.parkingLot)
                calculateDistance(event.parkingLot)
            }
            ParkingLotsMapEvent.ClearSelection -> {
                _uiState.value = _uiState.value.copy(
                    selectedParkingLot = null,
                    distance = null,
                    duration = null
                )
            }
            is ParkingLotsMapEvent.LocationUpdated -> {
                _uiState.value = _uiState.value.copy(userLocation = event.location)
            }
            is ParkingLotsMapEvent.GetDirections -> {
                 // Trigger external intent
            }
        }
    }
    
    private fun fetchParkingLots() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val snapshot = firestore.collection("parkingLots")
                    .whereEqualTo("status", "ACTIVE")
                    .get()
                    .await()
                
                val lots = snapshot.toObjects(ParkingLot::class.java)
                _uiState.value = _uiState.value.copy(
                    parkingLots = lots,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun requestCurrentLocation(context: Context) {
        viewModelScope.launch {
            val location = MapUtils.getCurrentLocation(context)
            if (location != null) {
                _uiState.value = _uiState.value.copy(userLocation = location)
            }
        }
    }

    private fun calculateDistance(destinationLot: ParkingLot) {
        val userLoc = _uiState.value.userLocation ?: return
        
        // Simple straight line distance calculation for now
        // or usage of Location.distanceTo if we convert GeoPoint to Location
        val results = FloatArray(1)
        Location.distanceBetween(
            userLoc.latitude, userLoc.longitude,
            destinationLot.latitude, destinationLot.longitude,
            results
        )
        
        val distanceInMeters = results[0]
        val distanceText = if (distanceInMeters > 1000) {
            String.format("%.1f km", distanceInMeters / 1000)
        } else {
            "${distanceInMeters.toInt()} m"
        }
        
        // Estimate duration (assuming 40km/h avg speed in city)
        // 40 km/h = 666.67 m/min
        val durationInMins = (distanceInMeters / 666.67).toInt()
        val durationText = "$durationInMins mins"

        _uiState.value = _uiState.value.copy(
            distance = distanceText,
            duration = durationText
        )
    }
}

data class ParkingLotsMapUiState(
    val parkingLots: List<ParkingLot> = emptyList(),
    val userLocation: GeoPoint? = null,
    val selectedParkingLot: ParkingLot? = null,
    val distance: String? = null,
    val duration: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class ParkingLotsMapEvent {
    data class ParkingLotSelected(val parkingLot: ParkingLot) : ParkingLotsMapEvent()
    object ClearSelection : ParkingLotsMapEvent()
    data class LocationUpdated(val location: GeoPoint) : ParkingLotsMapEvent()
    object GetDirections : ParkingLotsMapEvent()
}
