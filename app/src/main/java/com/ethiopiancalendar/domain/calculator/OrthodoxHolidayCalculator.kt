package com.ethiopiancalendar.domain.calculator

import com.ethiopiancalendar.domain.model.EthiopicDate
import com.ethiopiancalendar.domain.model.Holiday
import com.ethiopiancalendar.domain.model.HolidayType
import java.time.DayOfWeek
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
        
        return listOf(
            // Fasika - Ethiopian Easter (moveable)
            Holiday(
                id = "orthodox_fasika_$ethiopianYear",
                name = "Fasika (Easter)",
                nameAmharic = "ፋሲካ",
                type = HolidayType.ORTHODOX_CHRISTIAN,
                ethiopianMonth = easter.month,
                ethiopianDay = easter.day,
                isDayOff = true,
                description = "Ethiopian Orthodox Easter"
            ),
            
            // Siklet - Good Friday (2 days before Easter)
            Holiday(
                id = "orthodox_siklet_$ethiopianYear",
                name = "Siklet (Good Friday)",
                nameAmharic = "ስቅለት",
                type = HolidayType.ORTHODOX_CHRISTIAN,
                ethiopianMonth = easter.plusDays(-2).month,
                ethiopianDay = easter.plusDays(-2).day,
                isDayOff = true,
                description = "Crucifixion of Jesus Christ"
            ),
            
            // Tensae - Resurrection (same as Easter)
            Holiday(
                id = "orthodox_tensae_$ethiopianYear",
                name = "Tensae (Resurrection)",
                nameAmharic = "ትንሣኤ",
                type = HolidayType.ORTHODOX_CHRISTIAN,
                ethiopianMonth = easter.month,
                ethiopianDay = easter.day,
                isDayOff = false,
                description = "Resurrection of Jesus Christ"
            ),
            
            // Erget - Ascension (40 days after Easter)
            Holiday(
                id = "orthodox_erget_$ethiopianYear",
                name = "Erget (Ascension)",
                nameAmharic = "እርገት",
                type = HolidayType.ORTHODOX_CHRISTIAN,
                ethiopianMonth = easter.plusDays(40).month,
                ethiopianDay = easter.plusDays(40).day,
                isDayOff = false,
                description = "Ascension of Jesus into Heaven"
            ),
            
            // Peraklitos - Pentecost (50 days after Easter)
            Holiday(
                id = "orthodox_peraklitos_$ethiopianYear",
                name = "Peraklitos (Pentecost)",
                nameAmharic = "ጴራቅሊጦስ",
                type = HolidayType.ORTHODOX_CHRISTIAN,
                ethiopianMonth = easter.plusDays(50).month,
                ethiopianDay = easter.plusDays(50).day,
                isDayOff = false,
                description = "Descent of the Holy Spirit"
            )
        )
    }
    
    /**
     * Calculate Ethiopian Orthodox Easter using the Alexandrian computus
     * This algorithm matches the traditional Ethiopian Orthodox calculation
     */
    fun calculateEaster(ethiopianYear: Int): EthiopicDate {
        val a = ethiopianYear % 4
        val b = ethiopianYear % 7
        val c = ethiopianYear % 19
        val d = (19 * c + 15) % 30
        val e = (2 * a + 4 * b - d + 34) % 7
        val month = (d + e + 114) / 31
        val day = ((d + e + 114) % 31) + 1
        
        return EthiopicDate(
            year = ethiopianYear,
            month = month,
            day = day,
            dayOfWeek = DayOfWeek.SUNDAY  // Easter is always Sunday
        )
    }
}
