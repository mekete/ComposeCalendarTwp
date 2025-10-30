package com.ethiopiancalendar.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.ethiopiancalendar.data.local.entity.EventEntity
import timber.log.Timber
import java.time.DayOfWeek
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

/**
 * Scheduler for managing event reminder alarms using Android AlarmManager.
 *
 * Key responsibilities:
 * - Schedule one-time alarms for non-repeating events
 * - Schedule the next occurrence for repeating events
 * - Cancel existing alarms when events are deleted or reminders are disabled
 * - Use appropriate AlarmManager methods based on Android version and precision requirements
 *
 * Design decisions:
 * - Uses RTC_WAKEUP for calendar events (real-time clock based, wakes device)
 * - Uses setExactAndAllowWhileIdle for precise timing even in Doze mode
 * - For repeating events, schedules only the next occurrence (not all future occurrences)
 * - Reschedules repeating events when the alarm fires (via AlarmReceiver)
 * - Uses unique request codes based on event ID to enable cancellation
 *
 * Best practices:
 * - Always create matching PendingIntent when canceling alarms
 * - Use FLAG_UPDATE_CURRENT when updating existing alarms
 * - Be mindful of battery impact - only use exact alarms when necessary
 * - Handle Android 12+ SCHEDULE_EXACT_ALARM permission
 */
class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Schedule an alarm for an event.
     *
     * This method:
     * 1. Calculates the alarm trigger time (event time - reminder offset)
     * 2. Determines if the event is repeating
     * 3. Schedules the appropriate alarm type
     * 4. Handles edge cases (past events, invalid offsets, etc.)
     *
     * @param event The event to schedule an alarm for
     * @return true if alarm was scheduled successfully, false otherwise
     */
    fun scheduleAlarm(event: EventEntity): Boolean {
        // Check if reminder is enabled
        val reminderMinutes = event.reminderMinutesBefore ?: return false

        // Calculate alarm trigger time
        val alarmTime = event.startTime.minus(reminderMinutes.toLong(), ChronoUnit.MINUTES)

        // Don't schedule alarms for past times
        if (alarmTime.isBefore(ZonedDateTime.now())) {
            // For repeating events, try to find the next occurrence
            if (event.recurrenceRule != null) {
                return scheduleNextRecurringAlarm(event)
            }
            Timber.d("Not scheduling alarm for past event: ${event.summary}")
            return false
        }

        // Check if we have permission for exact alarms on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Timber.w("Cannot schedule exact alarms. Permission not granted.")
                // You could show a dialog to the user here to request permission
                return false
            }
        }

        // Create pending intent for the alarm
        val pendingIntent = createAlarmPendingIntent(event)

        try {
            // Use setExactAndAllowWhileIdle for precise timing
            // This is appropriate for calendar apps where precise timing is core functionality
            // RTC_WAKEUP uses real-time clock and wakes the device
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarmTime.toInstant().toEpochMilli(),
                pendingIntent
            )

            Timber.d("Scheduled alarm for event '${event.summary}' at $alarmTime")
            return true

        } catch (e: Exception) {
            Timber.e(e, "Failed to schedule alarm for event: ${event.summary}")
            return false
        }
    }

    /**
     * Schedule the next occurrence of a repeating event.
     *
     * This method parses the recurrence rule and finds the next occurrence
     * after the current time, then schedules an alarm for that occurrence.
     *
     * For weekly repeating events, this finds the next matching weekday.
     * After the alarm fires, AlarmReceiver will call this method again to
     * schedule the subsequent occurrence.
     *
     * @param event The repeating event
     * @return true if next alarm was scheduled, false otherwise
     */
    private fun scheduleNextRecurringAlarm(event: EventEntity): Boolean {
        val rrule = event.recurrenceRule ?: return false
        val reminderMinutes = event.reminderMinutesBefore ?: return false

        // Check if recurrence has ended
        val recurrenceEnd = event.recurrenceEndDate
        if (recurrenceEnd != null && recurrenceEnd.isBefore(ZonedDateTime.now())) {
            Timber.d("Recurrence has ended for event: ${event.summary}")
            return false
        }

        // Parse RRULE for weekly recurrence
        if (!rrule.contains("FREQ=WEEKLY")) {
            // For now, only support WEEKLY recurrence
            // You can extend this to support DAILY, MONTHLY, YEARLY
            Timber.w("Only WEEKLY recurrence is supported. Event: ${event.summary}")
            return false
        }

        // Extract weekdays from BYDAY parameter
        val weekDays = extractWeekDaysFromRRule(rrule)
        if (weekDays.isEmpty()) {
            Timber.w("No weekdays found in RRULE. Event: ${event.summary}")
            return false
        }

        // Find the next occurrence
        var nextOccurrence = findNextOccurrence(event.startTime, weekDays, recurrenceEnd)

        if (nextOccurrence == null) {
            Timber.d("No future occurrences for event: ${event.summary}")
            return false
        }

        // Calculate alarm time (next occurrence - reminder offset)
        val alarmTime = nextOccurrence.minus(reminderMinutes.toLong(), ChronoUnit.MINUTES)

        // Don't schedule if alarm time is in the past
        if (alarmTime.isBefore(ZonedDateTime.now())) {
            Timber.d("Alarm time is in the past for next occurrence. Event: ${event.summary}")
            return false
        }

        // Create pending intent
        val pendingIntent = createAlarmPendingIntent(event)

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarmTime.toInstant().toEpochMilli(),
                pendingIntent
            )

            Timber.d("Scheduled recurring alarm for event '${event.summary}' at $alarmTime (occurrence: $nextOccurrence)")
            return true

        } catch (e: Exception) {
            Timber.e(e, "Failed to schedule recurring alarm for event: ${event.summary}")
            return false
        }
    }

    /**
     * Find the next occurrence of a repeating event.
     *
     * Starting from the event's start time (or now if that's later),
     * find the next date that matches one of the specified weekdays.
     *
     * @param startTime The event's original start time
     * @param weekDays The days of the week when the event repeats
     * @param endDate The end date of the recurrence (null if never ends)
     * @return The next occurrence, or null if there are no more occurrences
     */
    private fun findNextOccurrence(
        startTime: ZonedDateTime,
        weekDays: Set<DayOfWeek>,
        endDate: ZonedDateTime?
    ): ZonedDateTime? {
        val now = ZonedDateTime.now()

        // Start searching from now or the original start time, whichever is later
        var current = if (startTime.isAfter(now)) startTime else now

        // Search for up to 365 days to avoid infinite loops
        val maxSearchDays = 365
        var daysSearched = 0

        while (daysSearched < maxSearchDays) {
            // Check if we've exceeded the recurrence end date
            if (endDate != null && current.isAfter(endDate)) {
                return null
            }

            // Check if current day matches one of the weekdays
            if (weekDays.contains(current.dayOfWeek)) {
                // Create occurrence with same time as original event
                val occurrence = current
                    .withHour(startTime.hour)
                    .withMinute(startTime.minute)
                    .withSecond(startTime.second)
                    .withNano(0)

                // Make sure the occurrence is in the future
                if (occurrence.isAfter(now)) {
                    return occurrence
                }
            }

            // Move to next day
            current = current.plusDays(1)
            daysSearched++
        }

        return null
    }

    /**
     * Extract weekdays from RRULE BYDAY parameter.
     * Example: "RRULE:FREQ=WEEKLY;BYDAY=TU,TH" -> [TUESDAY, THURSDAY]
     */
    private fun extractWeekDaysFromRRule(rrule: String): Set<DayOfWeek> {
        val byDayMatch = Regex("BYDAY=([A-Z,]+)").find(rrule) ?: return emptySet()
        val days = byDayMatch.groupValues[1].split(",")

        return days.mapNotNull { day ->
            when (day) {
                "MO" -> DayOfWeek.MONDAY
                "TU" -> DayOfWeek.TUESDAY
                "WE" -> DayOfWeek.WEDNESDAY
                "TH" -> DayOfWeek.THURSDAY
                "FR" -> DayOfWeek.FRIDAY
                "SA" -> DayOfWeek.SATURDAY
                "SU" -> DayOfWeek.SUNDAY
                else -> null
            }
        }.toSet()
    }

    /**
     * Cancel an alarm for an event.
     *
     * To cancel an alarm, you must create an identical PendingIntent
     * to the one used when scheduling the alarm.
     *
     * @param eventId The ID of the event
     */
    fun cancelAlarm(eventId: String) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_EVENT_ALARM
            putExtra(AlarmReceiver.EXTRA_EVENT_ID, eventId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId.hashCode(), // Must use same request code as when scheduling
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel() // Also cancel the PendingIntent itself

        Timber.d("Cancelled alarm for event ID: $eventId")
    }

    /**
     * Create a PendingIntent for an event alarm.
     *
     * This PendingIntent will be delivered to AlarmReceiver when the alarm fires.
     * The event details are passed as extras.
     *
     * @param event The event to create the PendingIntent for
     * @return PendingIntent for the alarm
     */
    private fun createAlarmPendingIntent(event: EventEntity): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_EVENT_ALARM
            putExtra(AlarmReceiver.EXTRA_EVENT_ID, event.id)
            putExtra(AlarmReceiver.EXTRA_EVENT_TITLE, event.summary)
            putExtra(AlarmReceiver.EXTRA_EVENT_DESCRIPTION, event.description)
            putExtra(AlarmReceiver.EXTRA_EVENT_TIME, event.startTime.toInstant().toEpochMilli())
            putExtra(AlarmReceiver.EXTRA_IS_RECURRING, event.recurrenceRule != null)
        }

        // Use event ID hash as request code for uniqueness
        // This allows us to update/cancel specific alarms
        return PendingIntent.getBroadcast(
            context,
            event.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Check if the app can schedule exact alarms on Android 12+.
     * On Android 12+, apps need SCHEDULE_EXACT_ALARM permission.
     *
     * @return true if exact alarms can be scheduled
     */
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true // Always allowed on older Android versions
        }
    }
}
