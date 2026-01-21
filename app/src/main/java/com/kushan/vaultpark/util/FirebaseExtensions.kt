package com.kushan.vaultpark.util

import com.google.firebase.firestore.QuerySnapshot
import com.kushan.vaultpark.model.ParkingSession
import kotlinx.coroutines.tasks.await

/**
 * Firebase Firestore extension functions
 */

/**
 * Await extension for Firestore Task
 */
suspend inline fun <reified T> QuerySnapshot.toObjectsAsync(): List<T> {
    return this.toObjects(T::class.java)
}

/**
 * Format duration in minutes to readable string
 */
fun formatDurationMinutes(minutes: Long): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return when {
        hours > 0 -> "${hours}h ${mins}m"
        else -> "${mins}m"
    }
}

/**
 * Format timestamp to relative time
 */
fun formatRelativeTime(timeMillis: Long): String {
    val now = System.currentTimeMillis()
    val diff = (now - timeMillis) / 1000

    return when {
        diff < 60 -> "now"
        diff < 3600 -> "${diff / 60}m ago"
        diff < 86400 -> "${diff / 3600}h ago"
        else -> "${diff / 86400}d ago"
    }
}

/**
 * Calculate parking session duration
 */
fun calculateSessionDuration(entryTime: Long, exitTime: Long?): Long {
    val endTime = exitTime ?: System.currentTimeMillis()
    return (endTime - entryTime) / (1000 * 60) // Return in minutes
}

/**
 * Calculate parking charges
 */
fun calculateParkingCharge(durationMinutes: Long, ratePerHour: Double = 50.0): Double {
    val hours = durationMinutes / 60.0
    return hours * ratePerHour
}

/**
 * Parse QR code data
 */
fun parseQRCodeData(qrData: String): Map<String, String> {
    val parts = qrData.split("|")
    return if (parts.size >= 5) {
        mapOf(
            "source" to parts.getOrNull(0).orEmpty(),
            "userId" to parts.getOrNull(1).orEmpty(),
            "timestamp" to parts.getOrNull(2).orEmpty(),
            "vehicleNumber" to parts.getOrNull(3).orEmpty(),
            "hash" to parts.getOrNull(4).orEmpty()
        )
    } else {
        emptyMap()
    }
}

/**
 * Validate QR code format
 */
fun isValidQRCode(qrData: String): Boolean {
    return qrData.startsWith("VAULTPARK|") && qrData.split("|").size == 5
}

/**
 * Session status helper
 */
fun isSessionActive(session: ParkingSession): Boolean {
    return session.status == "ACTIVE" && session.exitTime == null
}

/**
 * Get session gate display
 */
fun getGateDisplay(gateLocation: String): String {
    return gateLocation.replace("_", " ")
        .replaceFirstChar { it.uppercase() }
}
