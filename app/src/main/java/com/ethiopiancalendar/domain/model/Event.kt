package com.ethiopiancalendar.domain.model

import java.time.LocalDate
import java.util.UUID

/**
 * Represents a user event in the Ethiopian calendar
 */
data class Event(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String? = null,
    val ethiopianYear: Int,
    val ethiopianMonth: Int,
    val ethiopianDay: Int,
    val startTime: String? = null,  // HH:mm format
    val endTime: String? = null,
    val isAllDay: Boolean = true,
    val category: EventCategory = EventCategory.PERSONAL,
    val color: Int = 0xFF1976D2.toInt(),
    val googleCalendarEventId: String? = null,
    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    
    /**
     * Get the Ethiopian date for this event
     */
    fun getEthiopianDate(): EthiopicDate {
        return EthiopicDate(
            year = ethiopianYear,
            month = ethiopianMonth,
            day = ethiopianDay
        )
    }
    
    /**
     * Get the Gregorian date for this event
     */
    fun getGregorianDate(): LocalDate {
        return getEthiopianDate().toGregorianDate()
    }
}

/**
 * Categories for events
 */
enum class EventCategory {
    PERSONAL,
    WORK,
    RELIGIOUS,
    NATIONAL,
    BIRTHDAY,
    ANNIVERSARY,
    CUSTOM
}
