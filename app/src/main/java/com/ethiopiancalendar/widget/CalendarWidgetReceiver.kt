package com.ethiopiancalendar.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

/**
 * CalendarWidgetReceiver - BroadcastReceiver for the Calendar Glance Widget
 *
 * This receiver handles widget lifecycle events:
 * - Widget added to home screen
 * - Widget updated
 * - Widget removed from home screen
 * - Widget configuration changed
 */
class CalendarWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = CalendarGlanceWidget()

    /**
     * Trigger immediate widget update when widget is enabled or updated
     */
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        // Trigger immediate update when widget is first added
        triggerImmediateUpdate(context)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: android.appwidget.AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        // Trigger immediate update when widget is updated
        triggerImmediateUpdate(context)
    }

    private fun triggerImmediateUpdate(context: Context) {
        // Queue an immediate one-time update
        val updateRequest = OneTimeWorkRequestBuilder<CalendarWidgetWorker>().build()
        WorkManager.getInstance(context).enqueue(updateRequest)
    }
}
