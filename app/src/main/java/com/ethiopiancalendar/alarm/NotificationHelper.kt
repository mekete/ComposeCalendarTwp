package com.ethiopiancalendar.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.ethiopiancalendar.MainActivity
import com.ethiopiancalendar.R

/**
 * Helper class for managing notification channels and building notifications.
 *
 * Responsibilities:
 * - Create notification channels (required for Android 8.0+)
 * - Build event reminder notifications
 * - Handle notification actions
 *
 * Best practices:
 * - Create channels on app startup or first use
 * - Use unique channel IDs for different notification types
 * - Allow users to customize notification settings via system UI
 */
object NotificationHelper {

    /**
     * Notification channel ID for event reminders.
     * This should match the notificationChannelId field in EventEntity.
     */
    const val CHANNEL_ID_EVENT_REMINDERS = "event_reminders"

    /**
     * Notification channel name displayed to users.
     */
    private const val CHANNEL_NAME = "Event Reminders"

    /**
     * Notification channel description.
     */
    private const val CHANNEL_DESCRIPTION = "Notifications for upcoming calendar events"

    /**
     * Create notification channels.
     * Should be called when the app starts (e.g., in Application.onCreate()).
     *
     * On Android 8.0 (API 26) and higher, you must create notification channels
     * before posting any notifications.
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_EVENT_REMINDERS,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH // High importance for time-sensitive reminders
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
                // Allow users to customize sound, vibration, etc. via system settings
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Build a notification for an event reminder.
     *
     * @param context Application context
     * @param eventId Unique event ID
     * @param title Event title/summary
     * @param description Event description (optional)
     * @param eventTimeMillis Event start time in milliseconds
     * @return NotificationCompat.Builder configured for the event reminder
     */
    fun buildEventReminderNotification(
        context: Context,
        eventId: String,
        title: String,
        description: String?,
        eventTimeMillis: Long
    ): NotificationCompat.Builder {
        // Create intent to open the app when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // You can add extras here to navigate to the specific event
            putExtra("event_id", eventId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            eventId.hashCode(), // Use event ID hash as unique request code
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        return NotificationCompat.Builder(context, CHANNEL_ID_EVENT_REMINDERS)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use your app's icon
            .setContentTitle(title)
            .setContentText(description ?: "Event reminder")
            .setPriority(NotificationCompat.PRIORITY_HIGH) // High priority for heads-up notification
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true) // Dismiss when tapped
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Sound, vibration, lights
    }

    /**
     * Show an event reminder notification.
     *
     * @param context Application context
     * @param eventId Unique event ID
     * @param title Event title
     * @param description Event description (optional)
     * @param eventTimeMillis Event start time in milliseconds
     */
    fun showEventReminderNotification(
        context: Context,
        eventId: String,
        title: String,
        description: String?,
        eventTimeMillis: Long
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = buildEventReminderNotification(
            context,
            eventId,
            title,
            description,
            eventTimeMillis
        ).build()

        // Use event ID hash as unique notification ID so each event has its own notification
        notificationManager.notify(eventId.hashCode(), notification)
    }
}
