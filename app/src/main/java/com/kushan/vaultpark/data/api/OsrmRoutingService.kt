package com.kushan.vaultpark.data.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * OSRM Routing API Service
 * Uses the public OSRM server for routing calculations
 * Note: For production, consider hosting your own OSRM instance
 */
interface OsrmRoutingService {
    
    /**
     * Get route between two points
     * @param coordinates Format: "lon1,lat1;lon2,lat2"
     * @param overview Full geometry overview
     * @param geometries Geometry format (geojson or polyline)
     */
    @GET("route/v1/driving/{coordinates}")
    suspend fun getRoute(
        @Path("coordinates") coordinates: String,
        @Query("overview") overview: String = "full",
        @Query("geometries") geometries: String = "geojson",
        @Query("steps") steps: Boolean = true
    ): OsrmRouteResponse
}

/**
 * OSRM API Response Models
 */
data class OsrmRouteResponse(
    val code: String,
    val routes: List<OsrmRoute>?,
    val waypoints: List<OsrmWaypoint>?
)

data class OsrmRoute(
    val geometry: OsrmGeometry,
    val legs: List<OsrmLeg>,
    val distance: Double, // meters
    val duration: Double  // seconds
)

data class OsrmGeometry(
    val coordinates: List<List<Double>>, // [longitude, latitude] pairs
    val type: String = "LineString"
)

data class OsrmLeg(
    val steps: List<OsrmStep>,
    val distance: Double,
    val duration: Double
)

data class OsrmStep(
    val geometry: OsrmGeometry,
    val maneuver: OsrmManeuver,
    val name: String,
    val distance: Double,
    val duration: Double
)

data class OsrmManeuver(
    val type: String,
    val location: List<Double>
)

data class OsrmWaypoint(
    val location: List<Double>,
    val name: String
)
