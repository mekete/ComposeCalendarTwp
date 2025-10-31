package com.ethiopiancalendar.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

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
}
