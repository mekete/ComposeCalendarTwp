package com.ethiopiancalendar.domain.calculator

import com.ethiopiancalendar.domain.model.Holiday
import com.ethiopiancalendar.domain.model.HolidayType
import javax.inject.Inject

/**
 * Calculates Ethiopian national and public holidays
 */
class PublicHolidayCalculator @Inject constructor() {
    
    fun getPublicHolidaysForYear(ethiopianYear: Int): List<Holiday> {
        return listOf(
            // Enkutatash - Ethiopian New Year (Meskerem 1)
            Holiday(
                id = "public_new_year_$ethiopianYear",
                name = "Enkutatash (New Year)",
                nameAmharic = "እንቁጣጣሽ",
                type = HolidayType.NATIONAL,
                ethiopianMonth = 1,
                ethiopianDay = 1,
                isDayOff = true,
                description = "Ethiopian New Year"
            ),
            
            // Meskel - Finding of the True Cross (Meskerem 17)
            Holiday(
                id = "public_meskel_$ethiopianYear",
                name = "Meskel",
                nameAmharic = "መስቀል",
                type = HolidayType.ORTHODOX_CHRISTIAN,
                ethiopianMonth = 1,
                ethiopianDay = 17,
                isDayOff = true,
                description = "Finding of the True Cross"
            ),
            
            // Genna - Ethiopian Christmas (Tahsas 28 or 29)
            Holiday(
                id = "public_christmas_$ethiopianYear",
                name = "Genna (Christmas)",
                nameAmharic = "ገና",
                type = HolidayType.ORTHODOX_CHRISTIAN,
                ethiopianMonth = 4,
                ethiopianDay = getChristmasDay(ethiopianYear),
                isDayOff = true,
                description = "Ethiopian Christmas"
            ),
            
            // Timket - Epiphany (Tir 11)
            Holiday(
                id = "public_epiphany_$ethiopianYear",
                name = "Timket (Epiphany)",
                nameAmharic = "ጥምቀት",
                type = HolidayType.ORTHODOX_CHRISTIAN,
                ethiopianMonth = 5,
                ethiopianDay = 11,
                isDayOff = true,
                description = "Epiphany / Baptism of Jesus"
            ),
            
            // Adwa Victory Day (Yekatit 23)
            Holiday(
                id = "public_adwa_$ethiopianYear",
                name = "Adwa Victory Day",
                nameAmharic = "የዓድዋ ድል",
                type = HolidayType.NATIONAL,
                ethiopianMonth = 6,
                ethiopianDay = 23,
                isDayOff = true,
                description = "Victory of Adwa"
            ),
            
            // Labour Day (Ginbot 23)
            Holiday(
                id = "public_mayday_$ethiopianYear",
                name = "Labour Day",
                nameAmharic = "የሰራተኞች ቀን",
                type = HolidayType.NATIONAL,
                ethiopianMonth = 8,
                ethiopianDay = 23,
                isDayOff = true,
                description = "International Workers' Day"
            ),
            
            // Patriots' Day (Miazia 27)
            Holiday(
                id = "public_patriot_day_$ethiopianYear",
                name = "Patriots' Day",
                nameAmharic = "የአርበኞች ቀን",
                type = HolidayType.NATIONAL,
                ethiopianMonth = 8,
                ethiopianDay = 27,
                isDayOff = true,
                description = "Patriots Victory Day"
            ),
            
            // Derg Downfall Day (Ginbot 20)
            Holiday(
                id = "public_ginbot_20_$ethiopianYear",
                name = "Derg Downfall Day",
                nameAmharic = "የደርግ ውድቀት",
                type = HolidayType.NATIONAL,
                ethiopianMonth = 9,
                ethiopianDay = 20,
                isDayOff = true,
                description = "Downfall of the Derg Regime"
            )
        )
    }
    
    /**
     * Christmas is on Tahsas 28 in leap years, 29 otherwise
     */
    private fun getChristmasDay(ethiopianYear: Int): Int {
        return if (ethiopianYear % 4 == 3) 28 else 29
    }
}
