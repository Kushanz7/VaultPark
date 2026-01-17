package com.kushan.vaultpark.util

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Extension functions for common date/time operations
 */

fun Timestamp?.toFormattedString(format: String = "MMM dd, yyyy"): String {
    return if (this != null) {
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        sdf.format(this.toDate())
    } else {
        ""
    }
}

fun Timestamp?.toTimeString(): String {
    return if (this != null) {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        sdf.format(this.toDate())
    } else {
        ""
    }
}

fun Long.toFormattedString(format: String = "MMM dd, yyyy"): String {
    val sdf = SimpleDateFormat(format, Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.toTimeString(): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(this))
}

fun getCurrentMonthAndYear(): Pair<Int, Int> {
    val calendar = java.util.Calendar.getInstance()
    val month = calendar.get(java.util.Calendar.MONTH) + 1
    val year = calendar.get(java.util.Calendar.YEAR)
    return Pair(month, year)
}
