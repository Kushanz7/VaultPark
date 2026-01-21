package com.kushan.vaultpark.config

import com.kushan.vaultpark.model.ParkingSession
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Configuration and constants for VaultPark home screens
 */

object DashboardConfig {
    
    // Timing
    const val QR_REFRESH_INTERVAL = 30_000L // 30 seconds
    const val QR_VALIDITY_DURATION = 2 * 60 * 1000L // 2 minutes
    const val ACTIVE_SESSION_POLL_INTERVAL = 30_000L // 30 seconds
    const val DURATION_UPDATE_INTERVAL = 60_000L // 1 minute
    
    // Rates
    const val PARKING_RATE_PER_HOUR = 50.0
    
    // UI
    const val RECENT_SESSIONS_LIMIT = 3
    const val RECENT_SCANS_LIMIT = 5
    const val RECENT_ACTIVITY_LIMIT = 5
    
    // Gates
    val AVAILABLE_GATES = listOf(
        "Main Entrance",
        "Exit Gate A",
        "Exit Gate B"
    )
    
    // Entry Types
    val ENTRY_TYPES = listOf(
        "ENTRY",
        "EXIT"
    )
}

/**
 * Formatting utilities for dashboard displays
 */
object DashboardFormatting {
    
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    private val hourFormat = SimpleDateFormat("hh a", Locale.getDefault())
    
    fun formatEntryTime(timeMillis: Long): String {
        return timeFormat.format(Date(timeMillis))
    }
    
    fun formatDate(timeMillis: Long): String {
        return dateFormat.format(Date(timeMillis))
    }
    
    fun formatDuration(durationMinutes: Long): String {
        val hours = durationMinutes / 60
        val minutes = durationMinutes % 60
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            else -> "${minutes}m"
        }
    }
    
    fun formatSessionDuration(entryTime: Long, exitTime: Long?): String {
        val endTime = exitTime ?: System.currentTimeMillis()
        val durationMinutes = (endTime - entryTime) / (1000 * 60)
        return formatDuration(durationMinutes)
    }
    
    fun formatBillingAmount(entryTime: Long, exitTime: Long?): String {
        val endTime = exitTime ?: System.currentTimeMillis()
        val durationHours = (endTime - entryTime) / (1000.0 * 60 * 60)
        val amount = durationHours * DashboardConfig.PARKING_RATE_PER_HOUR
        return "$" + String.format("%.2f", amount)
    }
    
    fun formatRelativeTime(timeMillis: Long): String {
        val now = System.currentTimeMillis()
        val diff = (now - timeMillis) / 1000
        
        return when {
            diff < 60 -> "Now"
            diff < 3600 -> "${diff / 60}m ago"
            diff < 86400 -> "${diff / 3600}h ago"
            else -> dateFormat.format(Date(timeMillis))
        }
    }
    
    fun formatHour(hour: Int): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, 0)
        return hourFormat.format(cal.time)
    }
}

/**
 * Session status utilities
 */
object SessionStatusUtils {
    
    fun isSessionActive(session: ParkingSession): Boolean {
        return session.status == "ACTIVE" && session.exitTime == null
    }
    
    fun getSessionStatusDisplay(isActive: Boolean): String {
        return if (isActive) "Currently Parked" else "Not Parked"
    }
    
    fun calculateTimeSinceEntry(entryTime: Long): Long {
        return (System.currentTimeMillis() - entryTime) / (1000 * 60) // in minutes
    }
}

/**
 * Statistics aggregation utilities
 */
object StatisticsUtils {
    
    fun calculateMonthlyStats(sessions: List<ParkingSession>): Triple<Int, Double, Double> {
        val count = sessions.size
        var totalHours = 0.0
        
        sessions.forEach { session ->
            val exitTime = session.exitTime ?: System.currentTimeMillis()
            val durationHours = (exitTime - session.entryTime) / (1000.0 * 60 * 60)
            totalHours += durationHours
        }
        
        val totalAmount = totalHours * DashboardConfig.PARKING_RATE_PER_HOUR
        
        return Triple(count, totalHours, totalAmount)
    }
    
    fun getStartOfMonth(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis
    }
    
    fun getEndOfMonth(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis
    }
    
    fun getStartOfDay(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis
    }
    
    fun getEndOfDay(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis
    }
    
    fun groupByHour(sessions: List<ParkingSession>): Map<Int, Int> {
        val hourlyMap = mutableMapOf<Int, Int>()
        
        // Initialize all hours to 0
        for (hour in 0..23) {
            hourlyMap[hour] = 0
        }
        
        // Count sessions per hour
        sessions.forEach { session ->
            val cal = Calendar.getInstance()
            cal.timeInMillis = session.entryTime
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            hourlyMap[hour] = (hourlyMap[hour] ?: 0) + 1
        }
        
        return hourlyMap
    }
}

/**
 * QR Code generation utilities
 */
object QRCodeUtils {
    
    fun generateQRCodeData(userId: String, vehicleNumber: String): String {
        val timestamp = System.currentTimeMillis()
        val hashInput = "$userId|$timestamp|$vehicleNumber"
        val hash = hashInput.hashCode().toString().take(8)
        
        return "VAULTPARK|$userId|$timestamp|$vehicleNumber|$hash"
    }
    
    fun parseQRCodeData(qrData: String): Map<String, String>? {
        val parts = qrData.split("|")
        return if (parts.size == 5 && parts[0] == "VAULTPARK") {
            mapOf(
                "source" to parts[0],
                "userId" to parts[1],
                "timestamp" to parts[2],
                "vehicleNumber" to parts[3],
                "hash" to parts[4]
            )
        } else {
            null
        }
    }
    
    fun isValidQRCode(qrData: String): Boolean {
        return qrData.startsWith("VAULTPARK|") && qrData.split("|").size == 5
    }
}
