package com.kushan.vaultpark.notifications

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.kushan.vaultpark.model.NotificationData
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID

/**
 * Notification helper functions for VaultPark
 */
object NotificationHelper {

    private const val TAG = "NotificationHelper"
    private val firestore = FirebaseFirestore.getInstance()
    private val messaging = FirebaseMessaging.getInstance()

    /**
     * Register FCM token to Firestore
     */
    suspend fun registerFCMToken(userId: String): Result<String> = try {
        val token = messaging.token.await()

        firestore.collection("users")
            .document(userId)
            .update("fcmToken", token)
            .await()

        Log.d(TAG, "FCM token registered for user: $userId")
        Result.success(token)
    } catch (e: Exception) {
        Log.e(TAG, "Error registering FCM token", e)
        Result.failure(e)
    }

    /**
     * Save notification to Firestore
     */
    suspend fun saveNotification(
        userId: String,
        type: String,
        title: String,
        message: String,
        data: Map<String, String> = emptyMap()
    ): Result<String> = try {
        val notificationData = NotificationData(
            id = UUID.randomUUID().toString(),
            userId = userId,
            type = type,
            title = title,
            message = message,
            timestamp = Date(),
            isRead = false,
            data = data
        )

        val docRef = firestore.collection("notifications").add(notificationData).await()

        Log.d(TAG, "Notification saved: ${notificationData.id}")
        Result.success(docRef.id)
    } catch (e: Exception) {
        Log.e(TAG, "Error saving notification", e)
        Result.failure(e)
    }

    /**
     * Send entry notification
     */
    suspend fun sendEntryNotification(
        userId: String,
        driverName: String,
        gateLocation: String,
        sessionId: String
    ): Result<Unit> = try {
        val data = mapOf(
            "type" to "ENTRY",
            "sessionId" to sessionId,
            "gateLocation" to gateLocation,
            "timestamp" to Date().time.toString()
        )

        saveNotification(
            userId = userId,
            type = "ENTRY",
            title = "Entry Recorded ✓",
            message = "You entered at $gateLocation",
            data = data
        )

        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Error sending entry notification", e)
        Result.failure(e)
    }

    /**
     * Send exit notification
     */
    suspend fun sendExitNotification(
        userId: String,
        driverName: String,
        gateLocation: String,
        sessionId: String,
        duration: Long
    ): Result<Unit> = try {
        val hours = duration / 60
        val minutes = duration % 60

        val data = mapOf(
            "type" to "EXIT",
            "sessionId" to sessionId,
            "gateLocation" to gateLocation,
            "duration" to "$hours h $minutes m",
            "timestamp" to Date().time.toString()
        )

        saveNotification(
            userId = userId,
            type = "EXIT",
            title = "Exit Recorded ✓",
            message = "You exited at $gateLocation after ${hours}h ${minutes}m",
            data = data
        )

        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Error sending exit notification", e)
        Result.failure(e)
    }

    /**
     * Send billing reminder notification
     */
    suspend fun sendBillingReminder(
        userId: String,
        amount: Double,
        month: String,
        invoiceId: String
    ): Result<Unit> = try {
        val data = mapOf(
            "type" to "BILLING",
            "invoiceId" to invoiceId,
            "amount" to amount.toString(),
            "month" to month,
            "timestamp" to Date().time.toString()
        )

        saveNotification(
            userId = userId,
            type = "BILLING",
            title = "Billing Reminder",
            message = "Your $month parking bill of $$amount is due",
            data = data
        )

        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Error sending billing reminder", e)
        Result.failure(e)
    }

    /**
     * Send system notification
     */
    suspend fun sendSystemNotification(
        userId: String,
        title: String,
        message: String,
        data: Map<String, String> = emptyMap()
    ): Result<Unit> = try {
        saveNotification(
            userId = userId,
            type = "SYSTEM",
            title = title,
            message = message,
            data = data
        )

        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Error sending system notification", e)
        Result.failure(e)
    }

    /**
     * Mark notification as read
     */
    suspend fun markAsRead(notificationId: String): Result<Unit> = try {
        firestore.collection("notifications")
            .document(notificationId)
            .update("isRead", true)
            .await()

        Log.d(TAG, "Notification marked as read: $notificationId")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Error marking notification as read", e)
        Result.failure(e)
    }

    /**
     * Get recent notifications for user
     */
    suspend fun getRecentNotifications(
        userId: String,
        limit: Long = 30,
        daysBack: Long = 30
    ): Result<List<NotificationData>> = try {
        val thirtyDaysAgo = Date(System.currentTimeMillis() - (daysBack * 24 * 60 * 60 * 1000))

        val snapshot = firestore.collection("notifications")
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("timestamp", thirtyDaysAgo)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .await()

        val notifications = snapshot.toObjects(NotificationData::class.java)
        Log.d(TAG, "Retrieved ${notifications.size} recent notifications")
        Result.success(notifications)
    } catch (e: Exception) {
        Log.e(TAG, "Error getting recent notifications", e)
        Result.failure(e)
    }

    /**
     * Delete notification
     */
    suspend fun deleteNotification(notificationId: String): Result<Unit> = try {
        firestore.collection("notifications")
            .document(notificationId)
            .delete()
            .await()

        Log.d(TAG, "Notification deleted: $notificationId")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Error deleting notification", e)
        Result.failure(e)
    }

    /**
     * Trigger device vibration
     */
    fun vibrate(context: Context, duration: Long = 200) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(duration)
            }
        }
    }

    /**
     * Trigger success vibration pattern
     */
    fun vibrateSuccess(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 100, 100, 100),
                        -1
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(longArrayOf(0, 100, 100, 100), -1)
            }
        }
    }

    /**
     * Trigger error vibration pattern
     */
    fun vibrateError(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 200, 100, 200),
                        -1
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(longArrayOf(0, 200, 100, 200), -1)
            }
        }
    }
}
