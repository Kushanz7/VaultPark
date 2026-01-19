package com.kushan.vaultpark.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * User Preferences for profile settings
 * Stored in Firestore under /userPreferences/{userId}
 */
@IgnoreExtraProperties
data class UserPreferences(
    val userId: String = "",
    val notificationsEnabled: Boolean = true,
    val entryAlerts: Boolean = true,
    val exitAlerts: Boolean = true,
    val billingReminders: Boolean = true,
    val darkMode: Boolean = true, // Always true for VaultPark
    val language: String = "English",
    val scanSuccessSound: Boolean = true, // For security guards
    val vibrationFeedback: Boolean = true, // For security guards
    @ServerTimestamp
    val updatedAt: Date? = null
) {
    @Exclude
    fun toMap(): Map<String, Any?> = mapOf(
        "userId" to userId,
        "notificationsEnabled" to notificationsEnabled,
        "entryAlerts" to entryAlerts,
        "exitAlerts" to exitAlerts,
        "billingReminders" to billingReminders,
        "darkMode" to darkMode,
        "language" to language,
        "scanSuccessSound" to scanSuccessSound,
        "vibrationFeedback" to vibrationFeedback
    )
}

/**
 * Push Notification data model
 * Stored in Firestore under /notifications/{notificationId}
 */
@IgnoreExtraProperties
data class NotificationData(
    val id: String = "",
    val userId: String = "",
    val type: String = "", // ENTRY, EXIT, BILLING, SYSTEM
    val title: String = "",
    val message: String = "",
    @ServerTimestamp
    val timestamp: Date? = null,
    val isRead: Boolean = false,
    val data: Map<String, String> = emptyMap()
) {
    @Exclude
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "userId" to userId,
        "type" to type,
        "title" to title,
        "message" to message,
        "timestamp" to timestamp,
        "isRead" to isRead,
        "data" to data
    )
}

/**
 * Driver profile statistics
 */
data class DriverStats(
    val totalVisits: Int = 0,
    val totalHours: Double = 0.0,
    val thisMonthSessions: Int = 0,
    val memberSince: Date? = null
)

/**
 * Security profile statistics
 */
data class SecurityStats(
    val totalScans: Int = 0,
    val totalEntries: Int = 0,
    val totalExits: Int = 0,
    val guardSince: Date? = null
)
