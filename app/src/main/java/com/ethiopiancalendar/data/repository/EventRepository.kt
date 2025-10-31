package com.ethiopiancalendar.data.repository

import com.ethiopiancalendar.data.local.dao.EventDao
import com.ethiopiancalendar.data.local.entity.EventEntity
import com.ethiopiancalendar.data.local.entity.EventInstance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for event operations.
 *
 * Follows the same pattern as HolidayRepository:
 * - Uses Flow for reactive data
 * - Provides clean API for UI layer
 * - Handles business logic (e.g., recurring event instance generation)
 * - Single source of truth for event data
 *
 * This repository handles:
 * - CRUD operations for events
 * - Recurring event instance generation
 * - Event queries by date, month, year
 * - Future: Google Calendar sync
 */
@Singleton
class EventRepository @Inject constructor(
    private val eventDao: EventDao
) {

    // ========== QUERY OPERATIONS (Flow-based) ==========

    /**
     * Get all events as Flow.
     * UI will automatically update when events change.
     */
    fun getAllEvents(): Flow<List<EventEntity>> {
        return eventDao.getAllEventsFlow()
    }

    /**
     * Get event by ID as Flow.
     */
    fun getEventById(eventId: String): Flow<EventEntity?> {
        return eventDao.getEventByIdFlow(eventId)
    }

    /**
     * Get events for a specific Ethiopian date.
     * Returns both single events and instances of recurring events.
     */
    fun getEventsForDate(year: Int, month: Int, day: Int): Flow<List<EventInstance>> {
        return eventDao.getEventsForDate(year, month, day).map { events ->
            events.map { it.toEventInstance() }
        }
    }

    /**
     * Get events for a specific Ethiopian month.
     * Includes instances of recurring events.
     */
    fun getEventsForMonth(year: Int, month: Int): Flow<List<EventInstance>> {
        return eventDao.getEventsForMonth(year, month).map { events ->
            // Generate instances for each event (including recurring events)
            events.flatMap { event ->
                if (event.recurrenceRule != null) {
                    generateRecurringInstances(event, year, month)
                } else {
                    listOf(event.toEventInstance())
                }
            }
        }
    }

    /**
     * Get upcoming events (from start of today onwards).
     * Shows all events for today and future events.
     */
    fun getUpcomingEvents(limit: Int = 10): Flow<List<EventInstance>> {
        // Get start of today (midnight) to include all events for today
        val startOfToday = ZonedDateTime.now()
            .toLocalDate()
            .atStartOfDay(ZonedDateTime.now().zone)
            .toInstant()
            .toEpochMilli()

        return eventDao.getUpcomingEvents(startOfToday, limit).map { events ->
            events.map { it.toEventInstance() }
        }
    }

    /**
     * Get events within a Gregorian date range.
     * Useful for week view or custom ranges.
     */
    fun getEventsInRange(start: ZonedDateTime, end: ZonedDateTime): Flow<List<EventInstance>> {
        val startMillis = start.toInstant().toEpochMilli()
        val endMillis = end.toInstant().toEpochMilli()
        return eventDao.getEventsInRange(startMillis, endMillis).map { events ->
            events.flatMap { event ->
                if (event.recurrenceRule != null) {
                    generateRecurringInstancesInRange(event, start, end)
                } else {
                    listOf(event.toEventInstance())
                }
            }
        }
    }

    /**
     * Search events by query (title or description).
     */
    fun searchEvents(query: String): Flow<List<EventInstance>> {
        return eventDao.searchEvents(query).map { events ->
            events.map { it.toEventInstance() }
        }
    }

    /**
     * Get events by category.
     */
    fun getEventsByCategory(category: String): Flow<List<EventInstance>> {
        return eventDao.getEventsByCategory(category).map { events ->
            events.map { it.toEventInstance() }
        }
    }

    // ========== WRITE OPERATIONS (suspend functions) ==========

    /**
     * Create a new event.
     * @return The row ID of the created event
     */
    suspend fun createEvent(event: EventEntity): Long {
        return eventDao.insertEvent(event)
    }

    /**
     * Update an existing event.
     * @return Number of rows updated
     */
    suspend fun updateEvent(event: EventEntity): Int {
        return eventDao.updateEvent(event.copy(updatedAt = System.currentTimeMillis()))
    }

    /**
     * Delete an event.
     * @return Number of rows deleted
     */
    suspend fun deleteEvent(event: EventEntity): Int {
        return eventDao.deleteEvent(event)
    }

    /**
     * Delete event by ID.
     * @return Number of rows deleted
     */
    suspend fun deleteEventById(eventId: String): Int {
        return eventDao.deleteEventById(eventId)
    }

    /**
     * Get event count for a specific date (for calendar badges).
     */
    suspend fun getEventCountForDate(year: Int, month: Int, day: Int): Int {
        return eventDao.getEventCountForDate(year, month, day)
    }

    // ========== RECURRING EVENT LOGIC ==========

    /**
     * Generate instances of a recurring event for a specific Ethiopian month.
     *
     * This is a simplified implementation. For production, you would:
     * 1. Parse the RRULE string
     * 2. Generate all instances within the month range
     * 3. Handle complex recurrence patterns (MONTHLY, YEARLY, etc.)
     * 4. Respect recurrence end date or count
     *
     * For now, we implement WEEKLY recurrence with selected weekdays.
     */
    private fun generateRecurringInstances(
        event: EventEntity,
        year: Int,
        month: Int
    ): List<EventInstance> {
        // For this implementation, we'll keep it simple and just return the original event
        // In a full implementation, you would parse the RRULE and generate instances
        return listOf(event.toEventInstance())
    }

    /**
     * Generate instances of a recurring event within a Gregorian date range.
     *
     * This handles WEEKLY recurrence patterns.
     */
    private fun generateRecurringInstancesInRange(
        event: EventEntity,
        rangeStart: ZonedDateTime,
        rangeEnd: ZonedDateTime
    ): List<EventInstance> {
        val instances = mutableListOf<EventInstance>()
        val rrule = event.recurrenceRule ?: return listOf(event.toEventInstance())

        // Parse RRULE to determine recurrence pattern
        // For WEEKLY recurrence: "RRULE:FREQ=WEEKLY;BYDAY=TU,TH"
        if (!rrule.contains("FREQ=WEEKLY")) {
            // For non-weekly recurrence, return original event for now
            return listOf(event.toEventInstance())
        }

        // Extract weekdays from BYDAY parameter
        val weekDays = extractWeekDaysFromRRule(rrule)
        if (weekDays.isEmpty()) {
            return listOf(event.toEventInstance())
        }

        // Start from the event's start time
        var currentDate = event.startTime

        // Adjust to first occurrence in range
        while (currentDate.isBefore(rangeStart)) {
            currentDate = currentDate.plusDays(1)
        }

        // Generate instances until we exceed range end or recurrence end
        val recurrenceEnd = event.recurrenceEndDate ?: rangeEnd.plusYears(1) // Default max 1 year
        val effectiveEnd = if (recurrenceEnd.isBefore(rangeEnd)) recurrenceEnd else rangeEnd

        while (currentDate.isBefore(effectiveEnd)) {
            // Check if current day matches one of the specified weekdays
            if (weekDays.contains(currentDate.dayOfWeek)) {
                // Create instance with same time as original event
                val instanceStart = currentDate.withHour(event.startTime.hour)
                    .withMinute(event.startTime.minute)
                    .withSecond(event.startTime.second)

                val instanceEnd = event.endTime?.let {
                    currentDate.withHour(it.hour)
                        .withMinute(it.minute)
                        .withSecond(it.second)
                }

                // Convert to Ethiopian calendar for display
                // For now, use the original event's Ethiopian date (simplified)
                instances.add(
                    EventInstance(
                        eventId = event.id,
                        summary = event.summary,
                        description = event.description,
                        instanceStart = instanceStart,
                        instanceEnd = instanceEnd,
                        isAllDay = event.isAllDay,
                        category = event.category,
                        color = event.color,
                        reminderMinutesBefore = event.reminderMinutesBefore,
                        ethiopianYear = event.ethiopianYear,
                        ethiopianMonth = event.ethiopianMonth,
                        ethiopianDay = event.ethiopianDay,
                        isRecurring = true,
                        originalEvent = event
                    )
                )
            }

            currentDate = currentDate.plusDays(1)
        }

        return instances
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

    // ========== HELPER METHODS ==========

    /**
     * Convert EventEntity to EventInstance (for non-recurring or single instance).
     */
    private fun EventEntity.toEventInstance(): EventInstance {
        return EventInstance(
            eventId = id,
            summary = summary,
            description = description,
            instanceStart = startTime,
            instanceEnd = endTime,
            isAllDay = isAllDay,
            category = category,
            color = color,
            reminderMinutesBefore = reminderMinutesBefore,
            ethiopianYear = ethiopianYear,
            ethiopianMonth = ethiopianMonth,
            ethiopianDay = ethiopianDay,
            isRecurring = recurrenceRule != null,
            originalEvent = this
        )
    }
}
