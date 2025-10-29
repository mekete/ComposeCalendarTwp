package com.ethiopiancalendar.domain.calculator

import com.ethiopiancalendar.domain.model.Holiday
import com.ethiopiancalendar.domain.model.HolidayType
import org.threeten.extra.chrono.EthiopicDate
import java.time.DayOfWeek
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Calculates Ethiopian Orthodox moveable holidays
 * Uses the Alexandrian computus algorithm for Easter calculation
 */
@Singleton
class OrthodoxHolidayCalculator @Inject constructor() {

    /**
     * Get all Orthodox holidays for a specific Ethiopian year
     */
    fun getOrthodoxHolidaysForYear(ethiopianYear: Int): List<Holiday> {
        val easter = calculateEaster(ethiopianYear)

        // compute derived dates using ChronoUnit days (works with EthiopicDate)
        val goodFriday = easter.minus(2, ChronoUnit.DAYS)
        val ascension = easter.plus(40, ChronoUnit.DAYS)
        val pentecost = easter.plus(50, ChronoUnit.DAYS)

        return listOf(
            // Fasika - Ethiopian Easter (moveable)
            Holiday(
                id = "orthodox_fasika_$ethiopianYear",
                name = "Fasika (Easter)",
                nameAmharic = "ፋሲካ",
                type = HolidayType.ORTHODOX_CHRISTIAN,
                ethiopianMonth = easter.get(ChronoField.MONTH_OF_YEAR),
                ethiopianDay = easter.get(ChronoField.DAY_OF_MONTH),
                isDayOff = true,
                description = "Ethiopian Orthodox Easter"
            ),

            // Siklet - Good Friday (2 days before Easter)
            Holiday(
                id = "orthodox_siklet_$ethiopianYear",
                name = "Siklet (Good Friday)",
                nameAmharic = "ስቅለት",
                type = HolidayType.ORTHODOX_CHRISTIAN,
                ethiopianMonth = goodFriday.get(ChronoField.MONTH_OF_YEAR),
                ethiopianDay = goodFriday.get(ChronoField.DAY_OF_MONTH),
                isDayOff = true,
                description = "Crucifixion of Jesus Christ"
            ),

            // Tensae - Resurrection (same as Easter)
            Holiday(
                id = "orthodox_tensae_$ethiopianYear",
                name = "Tensae (Resurrection)",
                nameAmharic = "ትንሣኤ",
                type = HolidayType.ORTHODOX_CHRISTIAN,
                ethiopianMonth = easter.get(ChronoField.MONTH_OF_YEAR),
                ethiopianDay = easter.get(ChronoField.DAY_OF_MONTH),
                isDayOff = false,
                description = "Resurrection of Jesus Christ"
            ),

            // Erget - Ascension (40 days after Easter)
            Holiday(
                id = "orthodox_erget_$ethiopianYear",
                name = "Erget (Ascension)",
                nameAmharic = "እርገት",
                type = HolidayType.ORTHODOX_CHRISTIAN,
                ethiopianMonth = ascension.get(ChronoField.MONTH_OF_YEAR),
                ethiopianDay = ascension.get(ChronoField.DAY_OF_MONTH),
                isDayOff = false,
                description = "Ascension of Jesus into Heaven"
            ),

            // Peraklitos - Pentecost (50 days after Easter)
            Holiday(
                id = "orthodox_peraklitos_$ethiopianYear",
                name = "Peraklitos (Pentecost)",
                nameAmharic = "ጴራቅሊጦስ",
                type = HolidayType.ORTHODOX_CHRISTIAN,
                ethiopianMonth = pentecost.get(ChronoField.MONTH_OF_YEAR),
                ethiopianDay = pentecost.get(ChronoField.DAY_OF_MONTH),
                isDayOff = false,
                description = "Descent of the Holy Spirit"
            )
        )
    }

    /**
     * Calculate Ethiopian Orthodox Easter using the Alexandrian computus algorithm.
     * Returns a proper EthiopicDate from ThreeTen-Extra.
     */
    fun calculateEaster(ethiopianYear: Int): EthiopicDate {
        val a = ethiopianYear % 4
        val b = ethiopianYear % 7
        val c = ethiopianYear % 19
        val d = (19 * c + 15) % 30
        val e = (2 * a + 4 * b - d + 34) % 7
        val month = (d + e + 114) / 31
        val day = ((d + e + 114) % 31) + 1

        // ThreeTen-Extra EthiopicDate factory
        return EthiopicDate.of(ethiopianYear, month, day)
    }
}
