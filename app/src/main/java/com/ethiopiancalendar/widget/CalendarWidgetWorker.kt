package com.ethiopiancalendar.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ethiopiancalendar.data.repository.EventRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

/**
 * CalendarWidgetWorker - Background worker to update the Calendar Widget
 *
 * This worker:
 * - Runs periodically (every 15 minutes)
 * - Fetches upcoming events from EventRepository
 * - Updates all widget instances with fresh data
 */
@HiltWorker
class CalendarWidgetWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val eventRepository: EventRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Fetch upcoming events from repository
            val upcomingEvents = eventRepository.getUpcomingEvents(limit = 4).first()

            // Convert to WidgetEvent
            val widgetEvents = upcomingEvents.map { event ->
                WidgetEvent(
                    id = event.eventId,
                    title = event.summary,
                    startTime = event.instanceStart.toInstant().toEpochMilli(),
                    endTime = event.instanceEnd?.toInstant()?.toEpochMilli(),
                    isAllDay = event.isAllDay,
                    color = event.color,
                    category = event.category
                )
            }

            // Create new widget state
            val widgetState = CalendarWidgetState(events = widgetEvents)

            // Get all widget instances
            val glanceAppWidgetManager = GlanceAppWidgetManager(context)
            val glanceIds = glanceAppWidgetManager.getGlanceIds(CalendarGlanceWidget::class.java)

            // Update each widget instance with new state
            glanceIds.forEach { glanceId ->
                updateAppWidgetState(
                    context = context,
                    definition = CalendarWidgetStateDefinition,
                    glanceId = glanceId,
                    updateState = { widgetState }
                )
            }

            // Trigger widget update to refresh UI
            CalendarGlanceWidget().updateAll(context)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "CalendarWidgetUpdateWork"

        /**
         * Schedule periodic widget updates
         * Updates every 15 minutes to keep time and events fresh
         */
        fun schedule(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<CalendarWidgetWorker>(
                repeatInterval = 15,
                repeatIntervalTimeUnit = TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }

        /**
         * Cancel scheduled widget updates
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
