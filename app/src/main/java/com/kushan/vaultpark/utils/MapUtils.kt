package com.kushan.vaultpark.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import org.osmdroid.util.GeoPoint
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object MapUtils {
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): GeoPoint? {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        
        return suspendCancellableCoroutine { continuation ->
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        continuation.resume(GeoPoint(location.latitude, location.longitude))
                    } else {
                        continuation.resume(null)
                    }
                }
                .addOnFailureListener {
                    continuation.resume(null)
                }
        }
    }
}
