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
import com.kushan.vaultpark.data.repository.RoutingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.osmdroid.util.GeoPoint

class ParkingLotsMapViewModel : ViewModel() {
    private val firestore = Firebase.firestore
    private val routingRepository = RoutingRepository()
    
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
                calculateRoute(event.parkingLot)
            }
            ParkingLotsMapEvent.ClearSelection -> {
                // Only clear the selection (hides bottom sheet) but keep the route visible on map
                // as per user request: "keep it until we click on another parking lot pin"
                _uiState.value = _uiState.value.copy(
                    selectedParkingLot = null
                )
            }
            is ParkingLotsMapEvent.LocationUpdated -> {
                _uiState.value = _uiState.value.copy(userLocation = event.location)
            }
            is ParkingLotsMapEvent.SearchQueryChanged -> {
                _uiState.value = _uiState.value.copy(searchQuery = event.query)
                filterParkingLots(event.query)
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
                    filteredParkingLots = lots,
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
    
    private fun filterParkingLots(query: String) {
        val filtered = if (query.isBlank()) {
            _uiState.value.parkingLots
        } else {
            _uiState.value.parkingLots.filter { lot ->
                lot.name.contains(query, ignoreCase = true) ||
                lot.location.contains(query, ignoreCase = true) ||
                lot.facilities.any { it.contains(query, ignoreCase = true) }
            }
        }
        _uiState.value = _uiState.value.copy(filteredParkingLots = filtered)
    }
    
    private fun calculateRoute(destinationLot: ParkingLot) {
        val userLoc = _uiState.value.userLocation ?: return
        
        viewModelScope.launch {
            // Set loading state
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Get real route from OSRM
                val routeResult = routingRepository.getRoute(
                    start = userLoc,
                    end = GeoPoint(destinationLot.latitude, destinationLot.longitude)
                )
                
                if (routeResult != null) {
                    // Use real route points
                    _uiState.value = _uiState.value.copy(
                        routePoints = routeResult.points,
                        isLoading = false
                    )
                    
                    // Update distance and duration with accurate values from routing
                    val distanceText = if (routeResult.distanceMeters > 1000) {
                        String.format("%.1f km", routeResult.distanceMeters / 1000)
                    } else {
                        "${routeResult.distanceMeters.toInt()} m"
                    }
                    
                    val durationInMins = (routeResult.durationSeconds / 60).toInt()
                    val durationText = "$durationInMins mins"
                    
                    _uiState.value = _uiState.value.copy(
                        distance = distanceText,
                        duration = durationText
                    )
                } else {
                    // Fallback to straight line if routing fails
                    val fallbackRoute = listOf(
                        userLoc,
                        GeoPoint(destinationLot.latitude, destinationLot.longitude)
                    )
                    _uiState.value = _uiState.value.copy(
                        routePoints = fallbackRoute,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback to straight line on error
                val fallbackRoute = listOf(
                    userLoc,
                    GeoPoint(destinationLot.latitude, destinationLot.longitude)
                )
                _uiState.value = _uiState.value.copy(
                    routePoints = fallbackRoute,
                    isLoading = false,
                    error = "Could not calculate route"
                )
            }
        }
    }
}

data class ParkingLotsMapUiState(
    val parkingLots: List<ParkingLot> = emptyList(),
    val filteredParkingLots: List<ParkingLot> = emptyList(),
    val searchQuery: String = "",
    val userLocation: GeoPoint? = null,
    val selectedParkingLot: ParkingLot? = null,
    val routePoints: List<GeoPoint>? = null,
    val distance: String? = null,
    val duration: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class ParkingLotsMapEvent {
    data class ParkingLotSelected(val parkingLot: ParkingLot) : ParkingLotsMapEvent()
    object ClearSelection : ParkingLotsMapEvent()
    data class LocationUpdated(val location: GeoPoint) : ParkingLotsMapEvent()
    data class SearchQueryChanged(val query: String) : ParkingLotsMapEvent()
    object GetDirections : ParkingLotsMapEvent()
}
