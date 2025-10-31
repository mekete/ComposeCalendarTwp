package com.ethiopiancalendar.widget

import android.content.Context
import android.util.Log
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

    companion object {
        private const val TAG = "CalendarWidgetReceiver"
    }

    override val glanceAppWidget: GlanceAppWidget = CalendarGlanceWidget()

    /**
     * Trigger immediate widget update when widget is enabled or updated
     */
    override fun onEnabled(context: Context) {
        Log.d(TAG, "onEnabled: Widget added to home screen")
        super.onEnabled(context)
        // Trigger immediate update when widget is first added
        triggerImmediateUpdate(context)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: android.appwidget.AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(TAG, "onUpdate: Updating ${appWidgetIds.size} widget instances: ${appWidgetIds.contentToString()}")
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        // Trigger immediate update when widget is updated
        triggerImmediateUpdate(context)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        Log.d(TAG, "onDeleted: Removing ${appWidgetIds.size} widget instances")
        super.onDeleted(context, appWidgetIds)
    }

    override fun onDisabled(context: Context) {
        Log.d(TAG, "onDisabled: Last widget instance removed")
        super.onDisabled(context)
    }

    private fun triggerImmediateUpdate(context: Context) {
        Log.d(TAG, "triggerImmediateUpdate: Enqueuing immediate worker update")
        // Queue an immediate one-time update
        val updateRequest = OneTimeWorkRequestBuilder<CalendarWidgetWorker>().build()
        WorkManager.getInstance(context).enqueue(updateRequest)
        Log.d(TAG, "triggerImmediateUpdate: Work request enqueued with id ${updateRequest.id}")
    }
}
