package com.ethiopiancalendar.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ethiopiancalendar.alarm.AlarmScheduler
import com.ethiopiancalendar.data.local.CalendarDatabase
import com.ethiopiancalendar.data.repository.EventRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Receives boot completed broadcast to reschedule reminders.
 *
 * When a device reboots, all alarms are cleared by the system.
 * This receiver listens for ACTION_BOOT_COMPLETED and reschedules
 * all active event reminders.
 *
 * Requirements:
 * - RECEIVE_BOOT_COMPLETED permission in AndroidManifest
 * - Receiver must be registered in AndroidManifest
 *
 * Implementation notes:
 * - Uses Hilt for dependency injection
 * - Uses goAsync() to allow async database operations
 * - Reschedules all events with reminders enabled
 * - Logs the number of alarms rescheduled for debugging
 */
@AndroidEntryPoint
class BootCompleteReceiver : BroadcastReceiver() {

    @Inject
    lateinit var eventRepository: EventRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }

        Timber.d("Boot completed - rescheduling event reminders")

        // Use goAsync() to allow async operations
        val pendingResult = goAsync()

        scope.launch {
            try {
                // Get all events from database
                val events = eventRepository.getAllEvents().firstOrNull() ?: emptyList()

                // Filter events that have reminders enabled
                val eventsWithReminders = events.filter { it.reminderMinutesBefore != null }

                if (eventsWithReminders.isEmpty()) {
                    Timber.d("No events with reminders to reschedule")
                    return@launch
                }

                // Create alarm scheduler
                val alarmScheduler = AlarmScheduler(context)

                // Reschedule each event
                var successCount = 0
                var failCount = 0

                for (event in eventsWithReminders) {
                    try {
                        val scheduled = alarmScheduler.scheduleAlarm(event)
                        if (scheduled) {
                            successCount++
                        } else {
                            failCount++
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to reschedule alarm for event: ${event.summary}")
                        failCount++
                    }
                }

                Timber.d("Rescheduled $successCount alarms successfully, $failCount failed")

            } catch (e: Exception) {
                Timber.e(e, "Error rescheduling alarms after boot")
            } finally {
                pendingResult.finish()
            }
        }
    }
}
