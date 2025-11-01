package com.ethiopiancalendar.domain.calculator

import com.ethiopiancalendar.data.preferences.SettingsPreferences
import com.ethiopiancalendar.domain.model.Holiday
import com.ethiopiancalendar.domain.model.HolidayType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.threeten.extra.chrono.EthiopicDate
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Calculates Islamic holidays using the Hijrah calendar.
 * All dates are calculated astronomically and may vary ±1-2 days based on moon sighting.
 *
 * This implementation:
 *  - Converts the Ethiopian-year interval to Gregorian LocalDate range
 *  - Determines the overlapping Hijrah years (via HijrahDate.from(LocalDate))
 *  - Generates Hijrah dates for standard Muslim holidays and converts them to EthiopicDate
 *  - Filters to only return holidays that fall inside the requested Ethiopian year
 */
@Singleton
class MuslimHolidayCalculator @Inject constructor(
    private val settingsPreferences: SettingsPreferences
) {

    companion object {
        const val UID_EID_AL_FITR = 101
        const val UID_EID_AL_ADHA = 102
        const val UID_MAWLID = 103
        const val UID_RAMADAN = 108
    }

    /**
     * Get Muslim holidays for a specific Ethiopian year
     */
    fun getMuslimHolidaysForEthiopianYear(
        ethiopianYear: Int,
        includePublicHolidays: Boolean = true,
        includeWorkingHolidays: Boolean = false
    ): List<Holiday> {
        val holidays = mutableListOf<Holiday>()

        // Ethiopian start (Meskerem 1) and next-year start (exclusive)
        val startEthiopian = EthiopicDate.of(ethiopianYear, 1, 1)
        val endEthiopian = EthiopicDate.of(ethiopianYear + 1, 1, 1)

        // Convert Ethiopic boundaries to Gregorian LocalDate for comparison
        val startGregorian = LocalDate.from(startEthiopian) // throws if conversion not supported
        val endGregorian = LocalDate.from(endEthiopian)

        // Determine Hijri years overlapping this Gregorian interval
        val startHijrah = HijrahDate.from(startGregorian)
        val endHijrah = HijrahDate.from(endGregorian.minusDays(1)) // make end inclusive for overlap calculation

        for (hijriYear in startHijrah.get(ChronoField.YEAR_OF_ERA)..endHijrah.get(ChronoField.YEAR_OF_ERA)) {
            if (includePublicHolidays) {
                holidays.addAll(getPublicMuslimHolidays(hijriYear, ethiopianYear))
            }

            if (includeWorkingHolidays) {
                holidays.addAll(getWorkingMuslimHolidays(hijriYear))
            }
        }

        // Filter to only holidays whose Gregorian date falls inside the Ethiopian year range
        return holidays.filter { holiday ->
            // Convert holiday's Ethiopic date back to Gregorian for comparison
            val ethiopicDate = EthiopicDate.of(ethiopianYear, holiday.ethiopianMonth, holiday.ethiopianDay)
            val holidayGregorian = LocalDate.from(ethiopicDate)
            !holidayGregorian.isBefore(startGregorian) && holidayGregorian.isBefore(endGregorian)
        }
    }

    // Public (typically day-off) Muslim holidays for a given Hijri year
    private fun getPublicMuslimHolidays(hijriYear: Int, ethiopianYear: Int): List<Holiday> {
        val holidays = mutableListOf<Holiday>()

        // Get offset values from preferences
        val offsets = runBlocking {
            HolidayOffsets(
                eidAlAdha = settingsPreferences.dayOffsetEidAlAdha.first(),
                eidAlFitr = settingsPreferences.dayOffsetEidAlFitr.first(),
                mawlid = settingsPreferences.dayOffsetMawlid.first(),
                configuredYear = settingsPreferences.dayOffsetEthioYear.first()
            )
        }

        // Only apply offsets if the current Ethiopian year matches the configured year
        val shouldApplyOffsets = offsets.configuredYear == ethiopianYear

        // Eid al-Fitr (Shawwal 1) — Hijrah month 10, day 1
        var eidFitrDate = HijrahDate.of(hijriYear, 10, 1)
        if (shouldApplyOffsets && offsets.eidAlFitr != 0) {
            eidFitrDate = eidFitrDate.plus(offsets.eidAlFitr.toLong(), ChronoUnit.DAYS)
        }
        holidays.add(
            createMuslimHoliday(
                id = "muslim_eid_fitr_$hijriYear",
                name = "Eid al-Fitr",
                nameAmharic = "ኢድ አል-ፈጥር",
                hijriDate = eidFitrDate,
                isDayOff = true,
                description = "Festival of Breaking the Fast after Ramadan"
            )
        )

        // Eid al-Adha (Dhu al-Hijjah 10) — Hijrah month 12, day 10
        var eidAdhaDate = HijrahDate.of(hijriYear, 12, 10)
        if (shouldApplyOffsets && offsets.eidAlAdha != 0) {
            eidAdhaDate = eidAdhaDate.plus(offsets.eidAlAdha.toLong(), ChronoUnit.DAYS)
        }
        holidays.add(
            createMuslimHoliday(
                id = "muslim_eid_adha_$hijriYear",
                name = "Eid al-Adha",
                nameAmharic = "ኢድ አል-አድሃ",
                hijriDate = eidAdhaDate,
                isDayOff = true,
                description = "Festival of Sacrifice"
            )
        )

        // Mawlid al-Nabi (Rabi' al-Awwal 12) — Hijrah month 3, day 12
        var mawlidDate = HijrahDate.of(hijriYear, 3, 12)
        if (shouldApplyOffsets && offsets.mawlid != 0) {
            mawlidDate = mawlidDate.plus(offsets.mawlid.toLong(), ChronoUnit.DAYS)
         }
        holidays.add(
            createMuslimHoliday(
                id = "muslim_mawlid_$hijriYear",
                name = "Mawlid al-Nabi",
                nameAmharic = "መውሊድ",
                hijriDate = mawlidDate,
                isDayOff = true,
                description = "Birthday of Prophet Muhammad"
            )
        )

        return holidays
    }

    /**
     * Data class to hold holiday offset values
     */
    private data class HolidayOffsets(
        val eidAlAdha: Int,
        val eidAlFitr: Int,
        val mawlid: Int,
        val configuredYear: Int
    )

    // Working / observance Muslim holidays for a given Hijri year
    private fun getWorkingMuslimHolidays(hijriYear: Int): List<Holiday> {
        val holidays = mutableListOf<Holiday>()

        // Islamic New Year (Muharram 1) — Hijrah month 1, day 1
        val newYearDate = HijrahDate.of(hijriYear, 1, 1)
        holidays.add(
            createMuslimHoliday(
                id = "muslim_new_year_$hijriYear",
                name = "Islamic New Year",
                nameAmharic = "የሙስሊም አዲስ ዓመት",
                hijriDate = newYearDate,
                isDayOff = false,
                description = "First day of Muharram"
            )
        )

        // Ashura (Muharram 10)
        val ashuraDate = HijrahDate.of(hijriYear, 1, 10)
        holidays.add(
            createMuslimHoliday(
                id = "muslim_ashura_$hijriYear",
                name = "Ashura",
                nameAmharic = "አሹራ",
                hijriDate = ashuraDate,
                isDayOff = false,
                description = "Day of Ashura"
            )
        )

        // Start of Ramadan (Ramadan 1) — Hijrah month 9, day 1
        val ramadanDate = HijrahDate.of(hijriYear, 9, 1)
        holidays.add(
            createMuslimHoliday(
                id = "muslim_ramadan_$hijriYear",
                name = "Start of Ramadan",
                nameAmharic = "የረመዳን መጀመሪያ",
                hijriDate = ramadanDate,
                isDayOff = false,
                description = "Beginning of the holy month of fasting"
            )
        )

        // Mid-Sha'ban (Sha'ban 15) — Hijrah month 8, day 15
        val midShabanDate = HijrahDate.of(hijriYear, 8, 15)
        holidays.add(
            createMuslimHoliday(
                id = "muslim_mid_shaban_$hijriYear",
                name = "Mid-Sha'ban",
                nameAmharic = "መካከለኛ ሻዕባን",
                hijriDate = midShabanDate,
                isDayOff = false,
                description = "Night of Mid-Sha'ban"
            )
        )

        return holidays
    }

    /**
     * Create a Holiday from a HijrahDate by converting to Gregorian then to EthiopicDate.
     *
     * The ThreeTen-Extra EthiopicDate is a ChronoLocalDate; conversion path:
     *   HijrahDate -> LocalDate -> EthiopicDate
     *
     * We extract month/day using ChronoField to be robust against chronology implementations.
     */
    private fun createMuslimHoliday(
        id: String,
        name: String,
        nameAmharic: String,
        hijriDate: HijrahDate,
        isDayOff: Boolean,
        description: String
    ): Holiday {
        // HijrahDate -> Gregorian LocalDate
        val gregorian: LocalDate = LocalDate.from(hijriDate)

        // Gregorian LocalDate -> EthiopicDate
        val ethiopic: EthiopicDate = EthiopicDate.from(gregorian)

        // Extract Ethiopic month/day in an implementation-neutral way
        val ethiopianMonth = ethiopic.get(ChronoField.MONTH_OF_YEAR)
        val ethiopianDay = ethiopic.get(ChronoField.DAY_OF_MONTH)

        return Holiday(
            id = id,
            name = name,
            nameAmharic = nameAmharic,
            type = HolidayType.MUSLIM,
            ethiopianMonth = ethiopianMonth,
            ethiopianDay = ethiopianDay,
            isDayOff = isDayOff,
            description = description
        )
    }
}
