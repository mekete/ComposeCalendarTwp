package com.ethiopiancalendar.domain.calculator

import com.ethiopiancalendar.domain.model.EthiopianDate
import com.ethiopiancalendar.domain.model.HijriDate
import com.ethiopiancalendar.domain.model.Holiday
import com.ethiopiancalendar.domain.model.HolidayType
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Calculates Islamic holidays using the Hijri calendar
 * All dates are calculated astronomically and may vary ±1-2 days based on moon sighting
 */
@Singleton
class MuslimHolidayCalculator @Inject constructor() {
    
    companion object {
        const val UID_EID_AL_FITR = 101
        const val UID_EID_AL_ADHA = 102
        const val UID_MAWLID = 103
        const val UID_RAMADAN = 108
    }
    
    /**
     * Get Muslim holidays for a specific Ethiopian year
     * Since Islamic calendar is lunar, multiple Hijri years may overlap with one Ethiopian year
     */
    fun getMuslimHolidaysForEthiopianYear(
        ethiopianYear: Int,
        includePublicHolidays: Boolean = true,
        includeWorkingHolidays: Boolean = false
    ): List<Holiday> {
        val holidays = mutableListOf<Holiday>()
        
        // Get start and end dates for Ethiopian year
        val startEthiopian = EthiopianDate(ethiopianYear, 1, 1)
        val endEthiopian = EthiopianDate(ethiopianYear + 1, 1, 1)
        
        val startGregorian = startEthiopian.toGregorianDate()
        val endGregorian = endEthiopian.toGregorianDate()
        
        // Get Hijri years that overlap with this Ethiopian year
        val startHijri = HijriDate.from(startGregorian)
        val endHijri = HijriDate.from(endGregorian)
        
        // Generate holidays for all overlapping Hijri years
        for (hijriYear in startHijri.year..endHijri.year) {
            if (includePublicHolidays) {
                holidays.addAll(getPublicMuslimHolidays(hijriYear, ethiopianYear))
            }
            
            if (includeWorkingHolidays) {
                holidays.addAll(getWorkingMuslimHolidays(hijriYear, ethiopianYear))
            }
        }
        
        // Filter to only holidays within the Ethiopian year
        return holidays.filter { holiday ->
            val ethiopianDate = EthiopianDate(
                ethiopianYear,
                holiday.ethiopianMonth,
                holiday.ethiopianDay
            )
            val gregorianDate = ethiopianDate.toGregorianDate()
            
            !gregorianDate.isBefore(startGregorian) && gregorianDate.isBefore(endGregorian)
        }
    }
    
    private fun getPublicMuslimHolidays(hijriYear: Int, ethiopianYear: Int): List<Holiday> {
        val holidays = mutableListOf<Holiday>()
        
        // Eid al-Fitr (Shawwal 1)
        val eidFitrDate = createHijriDate(hijriYear, 10, 1)
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
        
        // Eid al-Adha (Dhul Hijjah 10)
        val eidAdhaDate = createHijriDate(hijriYear, 12, 10)
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
        
        // Mawlid al-Nabi (Rabi' al-Awwal 12)
        val mawlidDate = createHijriDate(hijriYear, 3, 12)
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
    
    private fun getWorkingMuslimHolidays(hijriYear: Int, ethiopianYear: Int): List<Holiday> {
        val holidays = mutableListOf<Holiday>()
        
        // Islamic New Year (Muharram 1)
        val newYearDate = createHijriDate(hijriYear, 1, 1)
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
        val ashuraDate = createHijriDate(hijriYear, 1, 10)
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
        
        // Start of Ramadan (Ramadan 1)
        val ramadanDate = createHijriDate(hijriYear, 9, 1)
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
        
        // Mid-Sha'ban (Sha'ban 15)
        val midShabanDate = createHijriDate(hijriYear, 8, 15)
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
    
    private fun createHijriDate(year: Int, month: Int, day: Int): HijriDate {
        // Create a Hijri date and convert to check validity
        val gregorian = HijriDate(
            year = year,
            month = month,
            day = day,
            dayOfWeek = org.threeten.bp.DayOfWeek.MONDAY,
            isLeapYear = false
        ).toGregorian()
        
        return HijriDate.from(gregorian)
    }
    
    private fun createMuslimHoliday(
        id: String,
        name: String,
        nameAmharic: String,
        hijriDate: HijriDate,
        isDayOff: Boolean,
        description: String
    ): Holiday {
        // Convert Hijri date to Ethiopian
        val ethiopianDate = hijriDate.toEthiopian()
        
        return Holiday(
            id = id,
            name = name,
            nameAmharic = nameAmharic,
            type = HolidayType.MUSLIM,
            ethiopianMonth = ethiopianDate.month,
            ethiopianDay = ethiopianDate.day,
            isDayOff = isDayOff,
            description = description
        )
    }
}
