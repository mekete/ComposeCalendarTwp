package com.ethiopiancalendar.domain.model

/**
 * Represents a holiday in the Ethiopian calendar
 */
data class Holiday(
    val id: String,
    val name: String,
    val nameAmharic: String = "",
    val type: HolidayType,
    val ethiopianMonth: Int,
    val ethiopianDay: Int,
    val isDayOff: Boolean,
    val description: String = ""
) : Comparable<Holiday> {
    
    override fun compareTo(other: Holiday): Int {
        return when {
            ethiopianMonth != other.ethiopianMonth -> ethiopianMonth.compareTo(other.ethiopianMonth)
            else -> ethiopianDay.compareTo(other.ethiopianDay)
        }
    }
    
    companion object {
        // Public Holiday IDs
        const val UID_HOLIDAY_PUBLIC_NEW_YEAR = 1
        const val UID_HOLIDAY_PUBLIC_MESKEL = 2
        const val UID_HOLIDAY_PUBLIC_CHRISTMAS = 3
        const val UID_HOLIDAY_PUBLIC_EPIPHANY = 4
        const val UID_HOLIDAY_PUBLIC_ADOWA = 5
        const val UID_HOLIDAY_PUBLIC_MAYDAY = 6
        const val UID_HOLIDAY_PUBLIC_PATRIOT_DAY = 7
        const val UID_HOLIDAY_PUBLIC_GINBOT_20 = 8
    }
}

/**
 * Holiday occurrence for a specific year with actual date
 */
data class HolidayOccurrence(
    val holiday: Holiday,
    val ethiopianDate: EthiopianDate,
    val adjustment: Int = 0  // Days adjusted via Firebase
) {
    val actualEthiopianDate: EthiopianDate
        get() = ethiopianDate.plusDays(adjustment.toLong())
}
