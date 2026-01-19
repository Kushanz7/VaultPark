package com.kushan.vaultpark.data.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.kushan.vaultpark.model.NotificationData
import com.kushan.vaultpark.model.UserPreferences
import kotlinx.coroutines.tasks.await
import java.util.Date

/**
 * Firestore queries for profile and preferences
 */
object ProfileFirestoreQueries {

    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Get user preferences
     */
    suspend fun getUserPreferences(userId: String): UserPreferences? = try {
        val doc = firestore.collection("userPreferences")
            .document(userId)
            .get()
            .await()

        if (doc.exists()) {
            doc.toObject(UserPreferences::class.java)
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }

    /**
     * Save user preferences
     */
    suspend fun saveUserPreferences(
        userId: String,
        preferences: UserPreferences
    ): Result<Unit> = try {
        firestore.collection("userPreferences")
            .document(userId)
            .set(preferences)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Update specific user preference fields
     */
    suspend fun updateUserPreferences(
        userId: String,
        updates: Map<String, Any>
    ): Result<Unit> = try {
        firestore.collection("userPreferences")
            .document(userId)
            .update(updates)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Get recent notifications
     */
    suspend fun getRecentNotifications(
        userId: String,
        limit: Long = 30
    ): Result<List<NotificationData>> = try {
        val snapshot = firestore.collection("notifications")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .await()

        val notifications = snapshot.toObjects(NotificationData::class.java)
        Result.success(notifications)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Get unread notification count
     */
    suspend fun getUnreadNotificationCount(userId: String): Result<Int> = try {
        val snapshot = firestore.collection("notifications")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isRead", false)
            .get()
            .await()

        Result.success(snapshot.size())
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Mark notification as read
     */
    suspend fun markNotificationAsRead(notificationId: String): Result<Unit> = try {
        firestore.collection("notifications")
            .document(notificationId)
            .update("isRead", true)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
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
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Clear all read notifications older than X days
     */
    suspend fun clearOldReadNotifications(
        userId: String,
        daysOld: Long = 30
    ): Result<Unit> = try {
        val cutoffDate = Date(System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000))

        val snapshot = firestore.collection("notifications")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isRead", true)
            .whereLessThan("timestamp", cutoffDate)
            .get()
            .await()

        val batch = firestore.batch()
        snapshot.documents.forEach { doc ->
            batch.delete(doc.reference)
        }
        batch.commit().await()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Get notifications by type
     */
    suspend fun getNotificationsByType(
        userId: String,
        type: String,
        limit: Long = 10
    ): Result<List<NotificationData>> = try {
        val snapshot = firestore.collection("notifications")
            .whereEqualTo("userId", userId)
            .whereEqualTo("type", type)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .await()

        val notifications = snapshot.toObjects(NotificationData::class.java)
        Result.success(notifications)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Update FCM token
     */
    suspend fun updateFCMToken(
        userId: String,
        token: String
    ): Result<Unit> = try {
        firestore.collection("users")
            .document(userId)
            .update("fcmToken", token)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Update profile image URL
     */
    suspend fun updateProfileImageUrl(
        userId: String,
        imageUrl: String
    ): Result<Unit> = try {
        firestore.collection("users")
            .document(userId)
            .update("profileImageUrl", imageUrl)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Delete profile image
     */
    suspend fun deleteProfileImage(userId: String): Result<Unit> = try {
        firestore.collection("users")
            .document(userId)
            .update("profileImageUrl", null)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
