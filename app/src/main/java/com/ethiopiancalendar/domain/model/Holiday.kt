package com.ethiopiancalendar.domain.model

import org.threeten.extra.chrono.EthiopicDate
import java.time.LocalDate
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit

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
    val ethiopicDate: EthiopicDate,
    val adjustment: Int = 0  // Days adjusted via Firebase
) {
    /**
     * Get the actual Ethiopic date after applying any adjustment
     * Converts through Gregorian to ensure correct date arithmetic
     */
    val actualEthiopicDate: EthiopicDate
        get() = if (adjustment == 0) {
            ethiopicDate
        } else {
            // Convert to Gregorian, add days, convert back to Ethiopic
            val gregorian = LocalDate.from(ethiopicDate)
            val adjustedGregorian = gregorian.plusDays(adjustment.toLong())
            EthiopicDate.from(adjustedGregorian)
        }

    /**
     * Get the Gregorian date for this holiday occurrence
     */
    fun toGregorian(): LocalDate = LocalDate.from(actualEthiopicDate)

    /**
     * Get Ethiopian month number (1-13)
     */
    fun getEthiopianMonth(): Int = actualEthiopicDate.get(ChronoField.MONTH_OF_YEAR)

    /**
     * Get Ethiopian day of month (1-30)
     */
    fun getEthiopianDay(): Int = actualEthiopicDate.get(ChronoField.DAY_OF_MONTH)

    /**
     * Get Ethiopian year
     */
    fun getEthiopianYear(): Int = actualEthiopicDate.get(ChronoField.YEAR_OF_ERA)
}