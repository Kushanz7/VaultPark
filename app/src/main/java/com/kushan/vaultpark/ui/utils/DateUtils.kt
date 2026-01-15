package com.kushan.vaultpark.ui.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

/**
 * Utility functions for date and time formatting
 */

fun formatDate(timeInMillis: Long): String {
    val date = Date(timeInMillis)
    val format = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return format.format(date)
}

fun formatTime(timeInMillis: Long): String {
    val date = Date(timeInMillis)
    val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return format.format(date)
}

fun formatDateTime(timeInMillis: Long): String {
    val date = Date(timeInMillis)
    val format = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
    return format.format(date)
}

fun formatDateTimeWithSeconds(timeInMillis: Long): String {
    val date = Date(timeInMillis)
    val format = SimpleDateFormat("MMM dd, yyyy hh:mm:ss a", Locale.getDefault())
    return format.format(date)
}

fun formatDuration(durationMs: Long): String {
    val hours = durationMs / (1000 * 60 * 60)
    val minutes = (durationMs % (1000 * 60 * 60)) / (1000 * 60)
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        else -> "${minutes}m"
    }
}

fun formatDurationBreakdown(durationMs: Long): Triple<Long, Long, Long> {
    val hours = durationMs / (1000 * 60 * 60)
    val minutes = (durationMs % (1000 * 60 * 60)) / (1000 * 60)
    val seconds = (durationMs % (1000 * 60)) / 1000
    return Triple(hours, minutes, seconds)
}

fun getDateLabel(timeInMillis: Long): String {
    val now = System.currentTimeMillis()
    val calendar = Calendar.getInstance()

    // Get today's start time
    calendar.apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val todayStart = calendar.timeInMillis

    // Get yesterday's start time
    calendar.add(Calendar.DAY_OF_MONTH, -1)
    val yesterdayStart = calendar.timeInMillis

    return when {
        timeInMillis >= todayStart -> "Today"
        timeInMillis >= yesterdayStart -> "Yesterday"
        else -> formatDate(timeInMillis)
    }
}

/**
 * Group sessions by date label for display
 */
fun <T> groupByDateLabel(
    items: List<T>,
    timeSelector: (T) -> Long
): Map<String, List<T>> {
    return items.groupBy { item ->
        getDateLabel(timeSelector(item))
    }
}

/**
 * Calculate total minutes between two timestamps
 */
fun calculateMinutesDifference(startTime: Long, endTime: Long): Long {
    return (endTime - startTime) / 60000
}

/**
 * Calculate total hours between two timestamps
 */
fun calculateHoursDifference(startTime: Long, endTime: Long): Double {
    return calculateMinutesDifference(startTime, endTime) / 60.0
}

/**
 * Format billing amount
 */
fun formatAmount(amount: Double): String {
    return String.format("$%.2f", amount)
}

/**
 * Calculate billing amount based on duration
 * Placeholder: $5 per hour
 */
fun calculateBillingAmount(durationMs: Long): Double {
    val hourlyRate = 5.0
    val hours = durationMs / (1000.0 * 60 * 60)
    return hours * hourlyRate
}
