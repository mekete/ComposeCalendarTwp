package com.ethiopiancalendar.data.local.converter

import androidx.room.TypeConverter
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Room TypeConverter for ZonedDateTime.
 *
 * Stores ZonedDateTime as ISO-8601 string with timezone information.
 * Example: "2025-10-30T14:30:00+03:00[Africa/Addis_Ababa]"
 *
 * This preserves timezone information which is crucial for:
 * - Accurate event scheduling across timezones
 * - Google Calendar API integration (which requires timezone info)
 * - Handling daylight saving time changes
 */
class DateConverter {

    private val formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME

    @TypeConverter
    fun fromZonedDateTime(value: ZonedDateTime?): String? {
        return value?.format(formatter)
    }

    @TypeConverter
    fun toZonedDateTime(value: String?): ZonedDateTime? {
        return value?.let {
            try {
                ZonedDateTime.parse(it, formatter)
            } catch (e: Exception) {
                null
            }
        }
    }
}
