package com.kushan.vaultpark.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.kushan.vaultpark.MainActivity
import com.kushan.vaultpark.R
import com.kushan.vaultpark.model.NotificationData
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID

/**
 * Firebase Cloud Messaging Service for VaultPark
 * Handles incoming push notifications and displays them to the user
 */
class VaultParkMessagingService : FirebaseMessagingService() {

    private val firestore = FirebaseFirestore.getInstance()
    private val TAG = "VaultParkMessaging"

    /**
     * Called when a new token is generated
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        
        // In a real app, send this token to your backend or save it
        // It's automatically saved to Firestore by ProfileViewModel
    }

    /**
     * Called when a message is received
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "Message received")

        // Handle notification
        val notification = remoteMessage.notification
        val data = remoteMessage.data

        when (data["type"]) {
            "ENTRY" -> handleEntryNotification(notification, data)
            "EXIT" -> handleExitNotification(notification, data)
            "BILLING" -> handleBillingNotification(notification, data)
            "SYSTEM" -> handleSystemNotification(notification, data)
            else -> handleDefaultNotification(notification, data)
        }

        // Save notification to Firestore for in-app display
        saveNotificationToFirestore(data)
    }

    /**
     * Handle entry notification
     */
    private fun handleEntryNotification(notification: RemoteMessage.Notification?, data: Map<String, String>) {
        val title = notification?.title ?: data["title"] ?: "Entry Recorded ✓"
        val body = notification?.body ?: data["message"] ?: "You've entered the parking"
        
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("navigate_to", "history")
            putExtra("notificationType", "entry")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        showNotification(
            channelId = "entry_exit_alerts",
            title = title,
            message = body,
            intent = intent,
            smallIcon = R.drawable.logo,
            color = Color.parseColor("#7F00FF"), // PrimaryPurple
            data = data
        )
    }

    /**
     * Handle exit notification
     */
    private fun handleExitNotification(notification: RemoteMessage.Notification?, data: Map<String, String>) {
        val title = notification?.title ?: data["title"] ?: "Exit Recorded ✓"
        val body = notification?.body ?: data["message"] ?: "You've exited the parking"
        
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("navigate_to", "history")
            putExtra("notificationType", "exit")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        showNotification(
            channelId = "entry_exit_alerts",
            title = title,
            message = body,
            intent = intent,
            smallIcon = R.drawable.logo,
            color = Color.parseColor("#7F00FF"),
            data = data
        )
    }

    /**
     * Handle billing notification
     */
    private fun handleBillingNotification(notification: RemoteMessage.Notification?, data: Map<String, String>) {
        val title = notification?.title ?: data["title"] ?: "Billing Reminder"
        val body = notification?.body ?: data["message"] ?: "Your monthly parking bill is due"
        
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("navigate_to", "billing")
            putExtra("notificationType", "billing")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        showNotification(
            channelId = "billing_reminders",
            title = title,
            message = body,
            intent = intent,
            smallIcon = R.drawable.logo,
            color = Color.parseColor("#FCD34D"), // SecondaryGold
            data = data
        )
    }

    /**
     * Handle system notification
     */
    private fun handleSystemNotification(notification: RemoteMessage.Notification?, data: Map<String, String>) {
        val title = notification?.title ?: data["title"] ?: "System Alert"
        val body = notification?.body ?: data["message"] ?: "Important system update"
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        showNotification(
            channelId = "system_alerts",
            title = title,
            message = body,
            intent = intent,
            smallIcon = R.drawable.logo,
            color = Color.parseColor("#4DA6FF"), // StatusInfo
            data = data
        )
    }

    /**
     * Handle default notification
     */
    private fun handleDefaultNotification(notification: RemoteMessage.Notification?, data: Map<String, String>) {
        val title = notification?.title ?: "VaultPark Notification"
        val body = notification?.body ?: "You have a new notification"
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        showNotification(
            channelId = "system_alerts",
            title = title,
            message = body,
            intent = intent,
            smallIcon = R.drawable.logo,
            color = android.graphics.Color.parseColor("#7C3AED"),
            data = data
        )
    }

    /**
     * Show notification to user
     */
    private fun showNotification(
        channelId: String,
        title: String,
        message: String,
        intent: Intent,
        smallIcon: Int,
        color: Int,
        data: Map<String, String>
    ) {
        val notificationId = System.currentTimeMillis().toInt()

        // Create pending intent
        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(smallIcon)
            .setContentTitle(title)
            .setContentText(message)
            .setColor(color)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        // Add sound for entry/exit alerts
        if (channelId == "entry_exit_alerts") {
            notificationBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            notificationBuilder.setVibrate(longArrayOf(0, 500, 250, 500))
        }

        // Add action buttons
        if (data["type"] == "BILLING") {
            val paymentIntent = Intent(this, MainActivity::class.java).apply {
                putExtra("navigate_to", "billing")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val paymentPendingIntent = PendingIntent.getActivity(
                this,
                notificationId + 1,
                paymentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            notificationBuilder.addAction(
                android.R.drawable.ic_menu_info_details,
                "Pay Now",
                paymentPendingIntent
            )
        }

        // Show notification
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notificationBuilder.build())

        Log.d(TAG, "Notification shown: $title")
    }

    /**
     * Save notification to Firestore for in-app display
     */
    private fun saveNotificationToFirestore(data: Map<String, String>) {
        runBlocking {
            try {
                val userId = data["userId"] ?: return@runBlocking
                
                val notificationData = NotificationData(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    type = data["type"] ?: "SYSTEM",
                    title = data["title"] ?: "",
                    message = data["message"] ?: "",
                    timestamp = Date(),
                    isRead = false,
                    data = data
                )

                firestore.collection("notifications")
                    .add(notificationData)
                    .await()

                Log.d(TAG, "Notification saved to Firestore")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving notification to Firestore", e)
            }
        }
    }

    companion object {
        /**
         * Create notification channels (call from MainActivity)
         */
        fun createNotificationChannels(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                // Entry/Exit channel
                val entryExitChannel = NotificationChannel(
                    "entry_exit_alerts",
                    "Entry & Exit Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications when you enter or exit parking"
                    enableVibration(true)
                    enableLights(true)
                    lightColor = Color.parseColor("#7F00FF")
                    setSound(
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                        android.media.AudioAttributes.Builder()
                            .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                            .build()
                    )
                }

                // Billing channel
                val billingChannel = NotificationChannel(
                    "billing_reminders",
                    "Billing Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Monthly billing notifications"
                }

                // System channel
                val systemChannel = NotificationChannel(
                    "system_alerts",
                    "System Alerts",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "App updates and announcements"
                }

                notificationManager.createNotificationChannel(entryExitChannel)
                notificationManager.createNotificationChannel(billingChannel)
                notificationManager.createNotificationChannel(systemChannel)

                Log.d("VaultParkMessaging", "Notification channels created")
            }
        }
    }
}
