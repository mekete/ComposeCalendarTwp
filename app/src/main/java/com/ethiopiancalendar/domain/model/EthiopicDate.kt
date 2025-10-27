package com.ethiopiancalendar.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Represents a date in the Ethiopian calendar
 * Ethiopian calendar has 13 months: 12 months of 30 days each, plus Pagume with 5-6 days
 */
data class EthiopicDate(
    val year: Int,
    val month: Int,  // 1-13
    val day: Int,    // 1-30 (or 1-5/6 for Pagume)
    val dayOfWeek: DayOfWeek = DayOfWeek.MONDAY
) : Comparable<EthiopicDate> {
    
    companion object {
        private const val ETHIOPIAN_EPOCH_OFFSET = 8 * 365 + 2  // 8 years difference + leap days
        
        fun now(): EthiopicDate {
            return from(LocalDate.now())
        }
        
        /**
         * Convert Gregorian date to Ethiopian date
         */
        fun from(gregorianDate: LocalDate): EthiopicDate {
            // Ethiopian calendar is approximately 7-8 years behind Gregorian
            val gregYear = gregorianDate.year
            val gregMonth = gregorianDate.monthValue
            val gregDay = gregorianDate.dayOfMonth
            
            // Calculate Ethiopian year
            val ethioYear = if (gregMonth <= 9 || (gregMonth == 9 && gregDay < 11)) {
                gregYear - 8
            } else {
                gregYear - 7
            }
            
            // Calculate day of year in Gregorian calendar
            val startOfYear = LocalDate.of(gregYear, 1, 1)
            val dayOfYear = ChronoUnit.DAYS.between(startOfYear, gregorianDate).toInt() + 1
            
            // Ethiopian new year starts around Sept 11 (or Sept 12 in leap years)
            val ethioNewYearDay = if (isEthiopianLeapYear(ethioYear)) 257 else 256
            
            // Calculate Ethiopian month and day
            val ethioDayOfYear = if (dayOfYear >= ethioNewYearDay) {
                dayOfYear - ethioNewYearDay + 1
            } else {
                val prevEthioYear = ethioYear - 1
                val daysInPrevYear = if (isEthiopianLeapYear(prevEthioYear)) 366 else 365
                dayOfYear + daysInPrevYear - ethioNewYearDay + 1
            }
            
            val ethioMonth = ((ethioDayOfYear - 1) / 30) + 1
            val ethioDay = ((ethioDayOfYear - 1) % 30) + 1
            
            return EthiopicDate(
                year = if (dayOfYear >= ethioNewYearDay) ethioYear else ethioYear - 1,
                month = ethioMonth.coerceIn(1, 13),
                day = ethioDay,
                dayOfWeek = gregorianDate.dayOfWeek
            )
        }
        
        private fun isEthiopianLeapYear(year: Int): Boolean {
            return year % 4 == 3
        }
    }
    
    /**
     * Convert Ethiopian date to Gregorian date
     */
    fun toGregorianDate(): LocalDate {
        // Ethiopian new year is Sept 11 (or 12 in leap year)
        val gregYear = year + 7
        val isLeapYear = isEthiopianLeapYear(year)
        val newYearDay = if (isLeapYear) 12 else 11
        
        val dayOfYear = (month - 1) * 30 + day
        
        val startOfEthioYear = LocalDate.of(gregYear, 9, newYearDay)
        return startOfEthioYear.plusDays((dayOfYear - 1).toLong())
    }
    
    fun plusDays(days: Long): EthiopicDate {
        val gregorian = toGregorianDate().plusDays(days)
        return from(gregorian)
    }
    
    fun plusMonths(months: Long): EthiopicDate {
        var newMonth = month + months.toInt()
        var newYear = year
        
        while (newMonth > 13) {
            newMonth -= 13
            newYear++
        }
        while (newMonth < 1) {
            newMonth += 13
            newYear--
        }
        
        return EthiopicDate(newYear, newMonth, day.coerceAtMost(getDaysInMonth(newYear, newMonth)), dayOfWeek)
    }
    
    fun format(): String {
        val monthName = getMonthName()
        return "$monthName $day, $year"
    }
    
    fun getMonthName(): String {
        return when (month) {
            1 -> "Meskerem"
            2 -> "Tikimt"
            3 -> "Hidar"
            4 -> "Tahsas"
            5 -> "Tir"
            6 -> "Yekatit"
            7 -> "Megabit"
            8 -> "Miazia"
            9 -> "Ginbot"
            10 -> "Sene"
            11 -> "Hamle"
            12 -> "Nehasse"
            13 -> "Pagume"
            else -> "Unknown"
        }
    }

    private fun gregToHijiri() {
        // 1. Create a Gregorian date
        val gregorianDate = LocalDate.of(2025, 10, 27)

        // 2. Convert to Hijri date
        val hijriDate = HijrahDate.from(gregorianDate)

        // 3. Format the Hijri date for display
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedHijri = formatter.format(hijriDate)

        println("Gregorian Date: $gregorianDate")
        println("Hijri Date: $formattedHijri")
    }


    private fun gregToEthio() {
        // 1. Create a Gregorian date
        val gregorianDate = LocalDate.of(2025, 10, 27)

        // 2. Convert to Hijri date
        val hijriDate = org.threeten.extra.chrono.EthiopicDate.from(gregorianDate)

        // 3. Format the Hijri date for display
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedHijri = formatter.format(hijriDate)

        println("Gregorian Date: $gregorianDate")
        println("Hijri Date: $formattedHijri")
    }


    private fun getDaysInMonth(year: Int, month: Int): Int {
        return when (month) {
            13 -> if (isEthiopianLeapYear(year)) 6 else 5
            else -> 30
        }
    }
    
    private fun isEthiopianLeapYear(year: Int): Boolean {
        return year % 4 == 3
    }
    
    override fun compareTo(other: EthiopicDate): Int {
        return when {
            year != other.year -> year.compareTo(other.year)
            month != other.month -> month.compareTo(other.month)
            else -> day.compareTo(other.day)
        }
    }
}
