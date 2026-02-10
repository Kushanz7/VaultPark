package com.kushan.vaultpark.data.repository

import com.kushan.vaultpark.data.api.OsrmRoutingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Repository for handling routing operations
 */
class RoutingRepository {
    
    private val osrmService: OsrmRoutingService by lazy {
        Retrofit.Builder()
            .baseUrl("https://routing.openstreetmap.de/routed-car/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OsrmRoutingService::class.java)
    }
    
    /**
     * Get route between two points using OSRM
     * @param start Starting point
     * @param end Destination point
     * @return List of GeoPoints representing the route, or null if failed
     */
    suspend fun getRoute(start: GeoPoint, end: GeoPoint): RouteResult? {
        return withContext(Dispatchers.IO) {
            try {
                // OSRM expects coordinates as "lon,lat;lon,lat"
                val coordinates = "${start.longitude},${start.latitude};${end.longitude},${end.latitude}"
                
                val response = osrmService.getRoute(coordinates)
                
                if (response.code == "Ok" && response.routes?.isNotEmpty() == true) {
                    val route = response.routes.first()
                    
                    // Convert coordinates from [lon, lat] to GeoPoint
                    val routePoints = route.geometry.coordinates.map { coord ->
                        GeoPoint(coord[1], coord[0]) // lat, lon
                    }
                    
                    RouteResult(
                        points = routePoints,
                        distanceMeters = route.distance,
                        durationSeconds = route.duration
                    )
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}

/**
 * Result of a routing calculation
 */
data class RouteResult(
    val points: List<GeoPoint>,
    val distanceMeters: Double,
    val durationSeconds: Double
)
