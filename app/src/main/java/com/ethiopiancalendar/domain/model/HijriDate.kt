package com.ethiopiancalendar.domain.model

import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate

/**
 * Represents a date in the Hijri (Islamic) calendar
 * Based on the Kuwaiti algorithm for astronomical calculations
 */
data class HijriDate(
    val year: Int,
    val month: Int,  // 1-12
    val day: Int,    // 1-29 or 1-30
    val dayOfWeek: DayOfWeek,
    val isLeapYear: Boolean = false
) {
    
    companion object {
        private const val HIJRI_EPOCH = 1948440  // Julian day of Hijra (July 16, 622 CE)
        
        fun now(): HijriDate {
            return from(LocalDate.now())
        }
        
        /**
         * Convert Gregorian date to Hijri date
         * Uses Kuwaiti algorithm for astronomical calculations
         */
        fun from(gregorianDate: LocalDate): HijriDate {
            val julianDay = gregorianToJulianDay(gregorianDate)
            return julianDayToHijri(julianDay)
        }
        
        private fun gregorianToJulianDay(date: LocalDate): Int {
            val year = date.year
            val month = date.monthValue
            val day = date.dayOfMonth
            
            val a = (14 - month) / 12
            val y = year + 4800 - a
            val m = month + (12 * a) - 3
            
            return day + ((153 * m + 2) / 5) + (365 * y) + (y / 4) - (y / 100) + (y / 400) - 32045
        }
        
        private fun julianDayToHijri(julianDay: Int): HijriDate {
            val l = julianDay - HIJRI_EPOCH + 10632
            val n = (l - 1) / 10631
            val l2 = l - 10631 * n + 354
            val j = ((10985 - l2) / 5316) * ((50 * l2) / 17719) + (l2 / 5670) * ((43 * l2) / 15238)
            val l3 = l2 - ((30 - j) / 15) * ((17719 * j) / 50) - (j / 16) * ((15238 * j) / 43) + 29
            
            val month = (24 * l3) / 709
            val day = l3 - ((709 * month) / 24)
            val year = 30 * n + j - 30
            
            val dayOfWeek = DayOfWeek.of(((julianDay + 1) % 7) + 1)
            val isLeapYear = isHijriLeapYear(year)
            
            return HijriDate(
                year = year,
                month = month,
                day = day,
                dayOfWeek = dayOfWeek,
                isLeapYear = isLeapYear
            )
        }
        
        private fun isHijriLeapYear(year: Int): Boolean {
            val remainder = year % 30
            return remainder in listOf(2, 5, 7, 10, 13, 16, 18, 21, 24, 26, 29)
        }
    }
    
    /**
     * Convert Hijri date to Gregorian date
     */
    fun toGregorian(): LocalDate {
        val julianDay = hijriToJulianDay(this)
        return julianDayToGregorian(julianDay)
    }
    
    private fun hijriToJulianDay(hijriDate: HijriDate): Int {
        return ((11 * hijriDate.year + 3) / 30) + 
               354 * hijriDate.year + 
               30 * hijriDate.month - 
               ((hijriDate.month - 1) / 2) + 
               hijriDate.day + 
               HIJRI_EPOCH - 385
    }
    
    private fun julianDayToGregorian(julianDay: Int): LocalDate {
        val a = julianDay + 32044
        val b = (4 * a + 3) / 146097
        val c = a - ((146097 * b) / 4)
        val d = (4 * c + 3) / 1461
        val e = c - ((1461 * d) / 4)
        val m = (5 * e + 2) / 153
        
        val day = e - ((153 * m + 2) / 5) + 1
        val month = m + 3 - (12 * (m / 10))
        val year = (100 * b) + d - 4800 + (m / 10)
        
        return LocalDate.of(year, month, day)
    }
    
    fun toEthiopian(): EthiopianDate = EthiopianDate.from(toGregorian())
    
    fun format(): String {
        val monthName = getMonthName()
        return "$monthName $day, $year AH"
    }
    
    fun getMonthName(): String {
        return when (month) {
            1 -> "Muharram"
            2 -> "Safar"
            3 -> "Rabi' al-Awwal"
            4 -> "Rabi' al-Thani"
            5 -> "Jumada al-Awwal"
            6 -> "Jumada al-Thani"
            7 -> "Rajab"
            8 -> "Sha'ban"
            9 -> "Ramadan"
            10 -> "Shawwal"
            11 -> "Dhu al-Qi'dah"
            12 -> "Dhu al-Hijjah"
            else -> "Unknown"
        }
    }
}
