package com.ethiopiancalendar.widget

import android.content.Context
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.ethiopiancalendar.MainActivity
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import  com.ethiopiancalendar.R;
/**
 * CalendarGlanceWidget - Home screen widget for Ethiopian Calendar
 *
 * Displays:
 * - Current date (large numeric and formatted)
 * - Dual time zones (Nairobi & Local)
 * - Upcoming reminders/events list
 */
class CalendarGlanceWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<CalendarWidgetState> = CalendarWidgetStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                //TextClock(pattern = "HH:mm:ss", timeZoneId = "Africa/Nairobi")

                CalendarWidgetContent()
            }
        }
    }
}



@Composable
fun CalendarWidgetContent() {
    val context = LocalContext.current
    val widgetState = currentState<CalendarWidgetState>()

    // Get current widget data
    val widgetData = getWidgetData(context, widgetState)

    Box(modifier = GlanceModifier.fillMaxSize().background(GlanceTheme.colors.background).padding(16.dp).clickable(actionStartActivity<MainActivity>()), contentAlignment = Alignment.TopCenter) {
        Column(modifier = GlanceModifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) { // Top Section: Date Display
            val packageName = LocalContext.current.packageName

            AndroidRemoteViews(RemoteViews(packageName, R.layout.clock_text_layout))

            Spacer(modifier = GlanceModifier.height(16.dp))
            TimeZonesSection(nairobiTime = widgetData.nairobiTime, nairobiDate = widgetData.nairobiTime, localTime = widgetData.localTime)

            Spacer(modifier = GlanceModifier.height(16.dp))

            // Bottom Section: Upcoming Events/Reminders
            RemindersSection(events = widgetData.upcomingEvents)
        }
    }
}

@Composable
fun TimeZonesSection(nairobiTime: String, nairobiDate: String, localTime: String) {
    Column(modifier = GlanceModifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalAlignment = Alignment.CenterVertically) { // Nairobi Time
        //TextClock(pattern = "HH:mm:ss", timeZoneId = "Africa/Nairobi")

        TimeZoneItem(label = "Nairobi", date = nairobiDate, time = nairobiTime)
        Spacer(modifier = GlanceModifier.width(4.dp))
        TimeZoneItem(label = "Local", date = nairobiDate, time = localTime)
    }
}

@Composable
fun TimeZoneItem(label: String, date: String, time: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalAlignment = Alignment.CenterHorizontally) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(text = label, style = TextStyle(fontSize = 16.sp, color = GlanceTheme.colors.onSurfaceVariant))
            //Spacer(modifier = GlanceModifier.height(1.dp))
            Text(text = date, style = TextStyle(fontSize = 16.sp, color = GlanceTheme.colors.onSurfaceVariant))
        }
        Spacer(modifier = GlanceModifier.width(12.dp))
        Row (horizontalAlignment = Alignment.End) {
            Text(text = time, style = TextStyle(fontSize = 40.sp, fontWeight = FontWeight.Medium, color = GlanceTheme.colors.onBackground))
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(text = "AM", style = TextStyle(fontSize = 16.sp, color = GlanceTheme.colors.onSurfaceVariant))
        }
    }
}

@Composable
fun RemindersSection(events: List<WidgetEvent>) {
    Column(modifier = GlanceModifier.fillMaxWidth()) { // Section header
        Text(text = "Reminders", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GlanceTheme.colors.onBackground))

        Spacer(modifier = GlanceModifier.height(8.dp))

        // Event list or empty state
        if (events.isEmpty()) {
            Text(text = "No reminders yet", style = TextStyle(fontSize = 14.sp, color = GlanceTheme.colors.onSurfaceVariant), modifier = GlanceModifier.padding(start = 8.dp))
        } else { // Show up to 4 upcoming events
            events.take(4).forEach { event ->
                EventItem(event = event)
                Spacer(modifier = GlanceModifier.height(8.dp))
            }
        }
    }
}

@Composable
fun EventItem(event: WidgetEvent) {
    Row(modifier = GlanceModifier.fillMaxWidth().padding(start = 8.dp), verticalAlignment = Alignment.CenterVertically) { // Color indicator dot
        //        Box(
        //            modifier = GlanceModifier
        //                .size(8.dp)
        //                .background(colorResource(event.color))
        //        )

        Spacer(modifier = GlanceModifier.width(8.dp))

        Column { // Event title
            Text(text = event.title, style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = GlanceTheme.colors.onBackground))

            Spacer(modifier = GlanceModifier.height(2.dp))

            // Event time
            Text(text = formatEventTime(event), style = TextStyle(fontSize = 12.sp, color = GlanceTheme.colors.onSurfaceVariant))
        }
    }
}

// Helper function to convert Int color to Glance ColorProvider
//@Composable
//private fun colorResource(colorInt: Int): androidx.glance.unit.ColorProvider {
//    val color = Color(colorInt)
//    return androidx.glance.unit.ColorProvider(color)
//}

// Data class to hold widget display data
data class WidgetData(
    val currentDate: ZonedDateTime, val formattedDate: String, val nairobiTime: String, val localTime: String, val upcomingEvents: List<WidgetEvent>
)

// Get widget data from context and state
fun getWidgetData(context: Context, state: CalendarWidgetState): WidgetData {
    val now = ZonedDateTime.now()

    // Format date: "Thu, Oct 30, 2025"
    val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")
    val formattedDate = now.format(dateFormatter)

    // Format time: "07:15 AM"
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm")

    // Get Nairobi time
    val nairobiZone = ZoneId.of("Africa/Nairobi")
    val nairobiTime = now.withZoneSameInstant(nairobiZone).format(timeFormatter)

    // Get local time
    val localTime = now.format(timeFormatter)

    return WidgetData(currentDate = now, formattedDate = formattedDate, nairobiTime = nairobiTime, localTime = localTime, upcomingEvents = state.events)
}

// Format event time for display
fun formatEventTime(event: WidgetEvent): String {
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d")

    val startTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(event.startTime), ZoneId.systemDefault())

    return if (event.isAllDay) {
        startTime.format(dateFormatter)
    } else {
        "${startTime.format(timeFormatter)} – ${startTime.format(dateFormatter)}"
    }
}
