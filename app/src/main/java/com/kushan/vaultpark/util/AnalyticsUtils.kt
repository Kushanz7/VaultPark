package com.kushan.vaultpark.util

import com.kushan.vaultpark.model.DailyTrendData
import com.kushan.vaultpark.model.HourlyData
import com.kushan.vaultpark.model.ParkingSession
import com.kushan.vaultpark.model.ReportStats
import com.kushan.vaultpark.model.TopDriver
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object AnalyticsUtils {
    
    /**
     * Calculate statistics from parking sessions
     */
    fun calculateStatistics(
        sessions: List<ParkingSession>,
        startDate: Long,
        endDate: Long
    ): ReportStats {
        val entries = sessions.count { it.entryTime != 0L }
        val exits = sessions.count { it.exitTime != null && it.exitTime != 0L }
        val active = sessions.count { it.status == "ACTIVE" }
        
        // Average duration in hours
        val completedSessions = sessions.filter { it.exitTime != null && it.exitTime != 0L }
        val avgDuration = if (completedSessions.isNotEmpty()) {
            completedSessions.map { calculateDurationInHours(it.entryTime, it.exitTime ?: 0) }
                .average()
        } else 0.0
        
        // Busiest hour
        val hourCounts = mutableMapOf<Int, Int>()
        for (hour in 0..23) {
            hourCounts[hour] = 0
        }
        
        sessions.forEach { session ->
            val hour = getHourFromTimestamp(session.entryTime)
            hourCounts[hour] = (hourCounts[hour] ?: 0) + 1
        }
        
        val busiestHour = hourCounts.maxByOrNull { it.value }?.key ?: 0
        
        // Format date range
        val dateRange = formatDateRange(startDate, endDate)
        
        return ReportStats(
            totalScans = entries + exits,
            totalEntries = entries,
            totalExits = exits,
            activeNow = active,
            averageDuration = avgDuration,
            busiestHour = busiestHour,
            dateRange = dateRange
        )
    }
    
    /**
     * Aggregate sessions by hour (0-23)
     */
    fun aggregateByHour(sessions: List<ParkingSession>): List<HourlyData> {
        val hourMap = mutableMapOf<Int, Int>()
        
        // Initialize all hours with 0
        for (hour in 0..23) {
            hourMap[hour] = 0
        }
        
        // Count scans per hour
        sessions.forEach { session ->
            if (session.entryTime != 0L) {
                val hour = getHourFromTimestamp(session.entryTime)
                hourMap[hour] = (hourMap[hour] ?: 0) + 1
            }
            if (session.exitTime != null && session.exitTime != 0L) {
                val exitHour = getHourFromTimestamp(session.exitTime)
                hourMap[exitHour] = (hourMap[exitHour] ?: 0) + 1
            }
        }
        
        return hourMap.map { HourlyData(it.key, it.value) }.sortedBy { it.hour }
    }
    
    /**
     * Aggregate sessions by day for trend analysis
     */
    fun aggregateDailyTrend(sessions: List<ParkingSession>): List<DailyTrendData> {
        val dayMap = mutableMapOf<Long, Int>()
        
        sessions.forEach { session ->
            if (session.entryTime != 0L) {
                val dayTimestamp = getStartOfDayTimestamp(session.entryTime)
                dayMap[dayTimestamp] = (dayMap[dayTimestamp] ?: 0) + 1
            }
        }
        
        return dayMap.map { DailyTrendData(it.key, it.value) }
            .sortedBy { it.date }
    }
    
    /**
     * Calculate top 5 drivers by visit count
     */
    fun calculateTopDrivers(sessions: List<ParkingSession>): List<TopDriver> {
        return sessions
            .filter { it.driverId.isNotEmpty() }
            .groupBy { it.driverId }
            .map { (driverId, driverSessions) ->
                TopDriver(
                    driverId = driverId,
                    driverName = driverSessions.firstOrNull()?.driverName ?: "Unknown",
                    vehicleNumber = driverSessions.firstOrNull()?.vehicleNumber ?: "Unknown",
                    visitCount = driverSessions.size,
                    totalHours = driverSessions
                        .filter { it.exitTime != null && it.exitTime != 0L }
                        .sumOf { calculateDurationInHours(it.entryTime, it.exitTime ?: 0) }
                )
            }
            .sortedByDescending { it.visitCount }
            .take(5)
    }
    
    /**
     * Calculate duration between two timestamps in hours
     */
    fun calculateDurationInHours(entryTime: Long, exitTime: Long): Double {
        if (entryTime == 0L || exitTime == 0L) return 0.0
        val durationMs = exitTime - entryTime
        return durationMs / (1000.0 * 60 * 60)
    }
    
    /**
     * Get hour of day (0-23) from timestamp
     */
    fun getHourFromTimestamp(timestamp: Long): Int {
        if (timestamp == 0L) return 0
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return calendar.get(Calendar.HOUR_OF_DAY)
    }
    
    /**
     * Get start of day timestamp (00:00:00)
     */
    fun getStartOfDayTimestamp(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    /**
     * Format date range as string
     */
    fun formatDateRange(startTime: Long, endTime: Long): String {
        val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
        val startDate = sdf.format(Date(startTime))
        val endDate = sdf.format(Date(endTime))
        return "$startDate - $endDate"
    }
    
    /**
     * Get current date range for TODAY
     */
    fun getTodayDateRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val endTime = calendar.timeInMillis - 1
        
        return Pair(startTime, endTime)
    }
    
    /**
     * Get date range for THIS_WEEK
     */
    fun getThisWeekDateRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        val startTime = calendar.timeInMillis
        
        calendar.add(Calendar.DAY_OF_MONTH, 7)
        val endTime = calendar.timeInMillis - 1
        
        return Pair(startTime, endTime)
    }
    
    /**
     * Get date range for THIS_MONTH
     */
    fun getThisMonthDateRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val startTime = calendar.timeInMillis
        
        calendar.add(Calendar.MONTH, 1)
        val endTime = calendar.timeInMillis - 1
        
        return Pair(startTime, endTime)
    }
    
    /**
     * Format timestamp as short date (MMM d)
     */
    fun formatShortDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
    
    /**
     * Format timestamp as short time (h:mm a)
     */
    fun formatShortTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
    
    /**
     * Format timestamp as hour (0am, 3am, 6am, etc.)
     */
    fun formatHour(hour: Int): String {
        return when (hour) {
            0 -> "12am"
            12 -> "12pm"
            in 1..11 -> "${hour}am"
            else -> "${hour - 12}pm"
        }
    }
}
