package com.ethiopiancalendar.domain.calculator

import com.ethiopiancalendar.domain.model.Holiday
import com.ethiopiancalendar.domain.model.HolidayType
import org.threeten.extra.chrono.EthiopicDate
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Calculates Ethiopian Orthodox moveable holidays using the traditional Ethiopian method
 * based on Metqi, Tewusak, and Nineveh calculations (matching OrthodoxHolidayManager.java)
 */
@Singleton
class OrthodoxHolidayCalculator @Inject constructor() {

    companion object {
        private const val BEFORE_COMMON_ERA = 5500 // God created the world 5500 years before the birth of Our Lord
        private const val MAX_DAYS_IN_LUNAR_MONTH = 30
        private const val NIOUS_QEMER = 19
        private const val MONTH_MESKEREM = 1
        private const val MONTH_TIKIMIT = 2
        private const val MONTH_TIR = 5
        private const val MONTH_YEKATIT = 6
    }

    /**
     * Get all Orthodox holidays for a specific Ethiopian year
     */
    fun getOrthodoxHolidaysForYear(ethiopianYear: Int): List<Holiday> {
        // Calculate Nineveh first, as it's the reference point for all movable holidays
        val nineveh = calculateNineveh(ethiopianYear)

        // Calculate derived dates using ChronoUnit days
        val goodFriday = nineveh.plus(67, ChronoUnit.DAYS) as EthiopicDate
        val easter = nineveh.plus(69, ChronoUnit.DAYS) as EthiopicDate
        val ascension = nineveh.plus(108, ChronoUnit.DAYS) as EthiopicDate
        val pentecost = nineveh.plus(118, ChronoUnit.DAYS) as EthiopicDate

        return listOf(
            // Siklet - Good Friday (Nineveh + 67 days)
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

            // Fasika - Ethiopian Easter (Nineveh + 69 days)
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

            // Erget - Ascension (Nineveh + 108 days)
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

            // Peraklitos - Pentecost (Nineveh + 118 days)
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
     * Calculate Nineveh (Fast of Nineveh) - the reference point for all movable Ethiopian Orthodox holidays
     * This uses the traditional Ethiopian calculation based on Metqi and Tewusak
     */
    private fun calculateNineveh(ethiopianYear: Int): EthiopicDate {
        val metqi = getMetqiForEthiopianYear(ethiopianYear)

        // Metqi can be 8 or less (occurs in Tikimit/October) or greater than 8 (occurs in Meskerem/September)
        val metiqMonth = if (metqi <= 8) MONTH_TIKIMIT else MONTH_MESKEREM
        val metqiDate = metqi

        // Calculate Mebaja Hamer (Metqi date + Tewusak)
        val tewusak = getTewusakFromMetiq(ethiopianYear, metiqMonth, metqiDate)
        val mebajaHamer = metqiDate + tewusak

        // Determine Nineveh month and date
        // If mebajaHamer > 30 or metiqMonth is Tikimit, Nineveh is in Yekatit
        // Otherwise it's in Tir
        val ninevehMonth = if (mebajaHamer > 30 || metiqMonth == MONTH_TIKIMIT) {
            MONTH_YEKATIT
        } else {
            MONTH_TIR
        }

        val ninevehDate = (mebajaHamer % 30).let { if (it == 0) 30 else it }

        return EthiopicDate.of(ethiopianYear, ninevehMonth, ninevehDate)
    }

    /**
     * Calculate Metqi for a given Ethiopian year
     *
     * Metqi (መጥቅ) is the number of days it takes for Lunar month to complete
     * when it is overlapped with the beginning month of new year.
     *
     * Abekitee (አበቅቴ) refers to the difference or overlapping days between
     * Lunar and Solar calendar. Lunar calendar year is shorter than Solar by 11
     * days and this number increases every year.
     */
    private fun getMetqiForEthiopianYear(ethiopianYear: Int): Int {
        val yearsSinceCreation = BEFORE_COMMON_ERA + ethiopianYear

        // Medeb (መደብ) is the remainder of yearsSinceCreation divided by 19 (Nuskemer)
        val medeb = yearsSinceCreation % NIOUS_QEMER

        // Wenber (ወንበር) is obtained by subtracting 1 from Medeb
        // If remainder is 0, Medeb of the year is 0 and Wenber is 18
        val wember = (medeb - 1 + NIOUS_QEMER) % NIOUS_QEMER

        // Abikete (አበቅቴ) the difference or overlapping days between Lunar and Solar calendar
        val abikete = (wember * 11) % MAX_DAYS_IN_LUNAR_MONTH

        // Metqi: the number of days it takes for Lunar month to complete
        val metqi = MAX_DAYS_IN_LUNAR_MONTH - abikete

        // Note: Metqi cannot be 1, 3, 6, 9, 11, 14, 17, 20, 22, 25 and 28
        // Therefore it can be any of the other 19 numbers
        return metqi
    }

    /**
     * Calculate Tewusak from Metiq
     *
     * Tewsak is the number of days from end of the Nineveh Fast to the starting date of the Lent Fast.
     * If the number of days is greater than 30, it's divided by 30 and the remainder will be Tewsak.
     *
     * Example: If Beale Metqi is on Saturday, the number of days between the next day of
     * Beale Metqi and Nineveh is 128. So 128 divided by 30 gives a remainder of 8.
     * This remainder of 8 is called Tewsak which always fall on Tuesday.
     */
    private fun getTewusakFromMetiq(ethiopianYear: Int, bealeMetiqMonth: Int, bealeMetiqDate: Int): Int {
        val metiqDate = EthiopicDate.of(ethiopianYear, bealeMetiqMonth, bealeMetiqDate)

        // Get day of week (1 = Monday, 7 = Sunday in ISO standard)
        val dayOfWeekValue = metiqDate.get(ChronoField.DAY_OF_WEEK)

        // Calculate Tewusak based on the day of week
        // Formula: ((128 - dayOfWeek - 2) % 7) with adjustment if result <= 1
        val tewusak = ((128 - dayOfWeekValue - 2) % 7).let {
            if (it <= 1) it + 7 else it
        }

        return tewusak
    }

    /**
     * Calculate Ethiopian Orthodox Easter using the traditional method
     * Returns Easter date (Nineveh + 69 days)
     */
    fun calculateEaster(ethiopianYear: Int): EthiopicDate {
        val nineveh = calculateNineveh(ethiopianYear)
        return nineveh.plus(69, ChronoUnit.DAYS) as EthiopicDate
    }
}