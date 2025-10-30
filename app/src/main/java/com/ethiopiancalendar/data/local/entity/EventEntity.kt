package com.ethiopiancalendar.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.ethiopiancalendar.data.local.converter.DateConverter
import java.time.ZonedDateTime
import java.util.UUID

/**
 * Room entity for storing events and reminders.
 *
 * This structure is designed to be compatible with Google Calendar API's Event resource:
 * - id: Unique identifier (maps to Google Calendar's event id)
 * - summary: Event title (maps to Google Calendar's summary)
 * - description: Event details (maps to Google Calendar's description)
 * - startTime: Start date/time with timezone (maps to Google Calendar's start.dateTime)
 * - endTime: End date/time with timezone (maps to Google Calendar's end.dateTime)
 * - isAllDay: Whether the event is all-day (maps to Google Calendar's start.date vs start.dateTime)
 * - timeZone: IANA timezone identifier (maps to Google Calendar's start.timeZone)
 * - recurrenceRule: RRULE string for recurring events (maps to Google Calendar's recurrence array)
 * - googleCalendarEventId: For bidirectional sync with Google Calendar
 *
 * Ethiopian calendar fields are stored separately for local display purposes.
 */
@Entity(tableName = "events")
@TypeConverters(DateConverter::class)
data class EventEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    // Core event fields (Google Calendar compatible)
    val summary: String,                              // Event title
    val description: String? = null,                   // Event description
    val startTime: ZonedDateTime,                      // Start date/time with timezone
    val endTime: ZonedDateTime? = null,                // End date/time with timezone (null for reminders)
    val isAllDay: Boolean = false,                     // All-day event flag
    val timeZone: String = "Africa/Addis_Ababa",      // IANA timezone identifier

    // Recurrence fields (Google Calendar compatible)
    val recurrenceRule: String? = null,                // RRULE string (e.g., "RRULE:FREQ=WEEKLY;BYDAY=TU,TH")
    val recurrenceEndDate: ZonedDateTime? = null,      // When recurrence ends (null = never ends)

    // Category and visual
    val category: String = "PERSONAL",                 // PERSONAL, WORK, RELIGIOUS, etc.
    val color: Int = 0xFF1976D2.toInt(),              // Event color

    // Reminder/Notification settings
    val reminderMinutesBefore: Int? = null,            // Minutes before event to remind (null = no reminder)
    val notificationChannelId: String = "event_reminders", // For Android notification channels

    // Ethiopian calendar fields (for local display)
    val ethiopianYear: Int,                            // Ethiopian calendar year
    val ethiopianMonth: Int,                           // Ethiopian calendar month (1-13)
    val ethiopianDay: Int,                             // Ethiopian calendar day

    // Google Calendar sync fields
    val googleCalendarEventId: String? = null,         // Google Calendar event ID
    val googleCalendarId: String? = null,              // Google Calendar ID (e.g., "primary")
    val isSynced: Boolean = false,                     // Whether synced with Google Calendar
    val syncedAt: Long? = null,                        // Last sync timestamp

    // Metadata
    val createdAt: Long = System.currentTimeMillis(),  // Creation timestamp
    val updatedAt: Long = System.currentTimeMillis()   // Last update timestamp
)

/**
 * Represents a single instance of an event (useful for recurring events).
 * This is a computed model, not stored in database directly.
 */
data class EventInstance(
    val eventId: String,
    val summary: String,
    val description: String?,
    val instanceStart: ZonedDateTime,
    val instanceEnd: ZonedDateTime?,
    val isAllDay: Boolean,
    val category: String,
    val color: Int,
    val reminderMinutesBefore: Int?,
    val ethiopianYear: Int,
    val ethiopianMonth: Int,
    val ethiopianDay: Int,
    val isRecurring: Boolean,
    val originalEvent: EventEntity? = null
)
