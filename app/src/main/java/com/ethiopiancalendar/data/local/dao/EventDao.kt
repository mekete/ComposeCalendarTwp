package com.ethiopiancalendar.data.local.dao

import androidx.room.*
import com.ethiopiancalendar.data.local.entity.EventEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Event operations.
 *
 * Uses Flow for reactive database queries - UI will automatically update when data changes.
 * This is the same pattern used in HolidayRepository.
 */
@Dao
interface EventDao {

    // ========== CREATE ==========

    /**
     * Insert a new event.
     * @return The row ID of the inserted event
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity): Long

    /**
     * Insert multiple events.
     * @return Array of row IDs
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>): List<Long>

    // ========== READ ==========

    /**
     * Get all events as Flow (reactive).
     * UI will automatically update when events change.
     */
    @Query("SELECT * FROM events ORDER BY startTime ASC")
    fun getAllEventsFlow(): Flow<List<EventEntity>>

    /**
     * Get all events (non-reactive, for one-time fetch).
     */
    @Query("SELECT * FROM events ORDER BY startTime ASC")
    suspend fun getAllEvents(): List<EventEntity>

    /**
     * Get event by ID.
     */
    @Query("SELECT * FROM events WHERE id = :eventId")
    suspend fun getEventById(eventId: String): EventEntity?

    /**
     * Get event by ID as Flow (reactive).
     */
    @Query("SELECT * FROM events WHERE id = :eventId")
    fun getEventByIdFlow(eventId: String): Flow<EventEntity?>

    /**
     * Get events for a specific Ethiopian date.
     * Useful for showing events on a calendar day.
     */
    @Query("""
        SELECT * FROM events
        WHERE ethiopianYear = :year
        AND ethiopianMonth = :month
        AND ethiopianDay = :day
        ORDER BY startTime ASC
    """)
    fun getEventsForDate(year: Int, month: Int, day: Int): Flow<List<EventEntity>>

    /**
     * Get events for a specific Ethiopian month.
     * Useful for monthly calendar view.
     */
    @Query("""
        SELECT * FROM events
        WHERE ethiopianYear = :year
        AND ethiopianMonth = :month
        ORDER BY ethiopianDay ASC, startTime ASC
    """)
    fun getEventsForMonth(year: Int, month: Int): Flow<List<EventEntity>>

    /**
     * Get events for a specific Ethiopian year.
     */
    @Query("""
        SELECT * FROM events
        WHERE ethiopianYear = :year
        ORDER BY ethiopianMonth ASC, ethiopianDay ASC, startTime ASC
    """)
    fun getEventsForYear(year: Int): Flow<List<EventEntity>>

    /**
     * Get events within a Gregorian date range.
     * Useful for week view or custom date ranges.
     *
     * @param startTimeMillis Start timestamp (inclusive)
     * @param endTimeMillis End timestamp (exclusive)
     */
    @Query("""
        SELECT * FROM events
        WHERE startTime >= :startTimeMillis
        AND startTime < :endTimeMillis
        ORDER BY startTime ASC
    """)
    fun getEventsInRange(startTimeMillis: Long, endTimeMillis: Long): Flow<List<EventEntity>>

    /**
     * Get all recurring events (events with recurrence rules).
     */
    @Query("SELECT * FROM events WHERE recurrenceRule IS NOT NULL")
    suspend fun getRecurringEvents(): List<EventEntity>

    /**
     * Get events by category.
     */
    @Query("SELECT * FROM events WHERE category = :category ORDER BY startTime ASC")
    fun getEventsByCategory(category: String): Flow<List<EventEntity>>

    /**
     * Search events by title or description.
     */
    @Query("""
        SELECT * FROM events
        WHERE summary LIKE '%' || :query || '%'
        OR description LIKE '%' || :query || '%'
        ORDER BY startTime DESC
    """)
    fun searchEvents(query: String): Flow<List<EventEntity>>

    /**
     * Get upcoming events (from start of today onwards).
     * Shows all events for today and future events.
     */
    @Query("""
        SELECT * FROM events
        WHERE startTime >= :startOfDayMillis
        ORDER BY startTime ASC
        LIMIT :limit
    """)
    fun getUpcomingEvents(startOfDayMillis: Long, limit: Int = 10): Flow<List<EventEntity>>

    // ========== UPDATE ==========

    /**
     * Update an existing event.
     * @return Number of rows updated (should be 1)
     */
    @Update
    suspend fun updateEvent(event: EventEntity): Int

    /**
     * Update multiple events.
     * @return Number of rows updated
     */
    @Update
    suspend fun updateEvents(events: List<EventEntity>): Int

    /**
     * Mark event as synced with Google Calendar.
     */
    @Query("""
        UPDATE events
        SET isSynced = :isSynced,
            syncedAt = :syncedAt,
            googleCalendarEventId = :googleEventId
        WHERE id = :eventId
    """)
    suspend fun updateSyncStatus(
        eventId: String,
        isSynced: Boolean,
        syncedAt: Long,
        googleEventId: String?
    ): Int

    // ========== DELETE ==========

    /**
     * Delete an event.
     * @return Number of rows deleted (should be 1)
     */
    @Delete
    suspend fun deleteEvent(event: EventEntity): Int

    /**
     * Delete event by ID.
     * @return Number of rows deleted
     */
    @Query("DELETE FROM events WHERE id = :eventId")
    suspend fun deleteEventById(eventId: String): Int

    /**
     * Delete all events.
     * @return Number of rows deleted
     */
    @Query("DELETE FROM events")
    suspend fun deleteAllEvents(): Int

    /**
     * Delete events by category.
     * @return Number of rows deleted
     */
    @Query("DELETE FROM events WHERE category = :category")
    suspend fun deleteEventsByCategory(category: String): Int

    /**
     * Delete past events (older than specified date).
     * Useful for cleanup.
     */
    @Query("DELETE FROM events WHERE startTime < :beforeTimeMillis AND recurrenceRule IS NULL")
    suspend fun deletePastEvents(beforeTimeMillis: Long): Int

    // ========== COUNT ==========

    /**
     * Get total event count.
     */
    @Query("SELECT COUNT(*) FROM events")
    suspend fun getEventCount(): Int

    /**
     * Get event count for a specific date.
     */
    @Query("""
        SELECT COUNT(*) FROM events
        WHERE ethiopianYear = :year
        AND ethiopianMonth = :month
        AND ethiopianDay = :day
    """)
    suspend fun getEventCountForDate(year: Int, month: Int, day: Int): Int
}
