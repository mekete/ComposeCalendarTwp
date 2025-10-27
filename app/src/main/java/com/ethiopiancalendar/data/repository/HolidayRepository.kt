package com.ethiopiancalendar.data.repository

import com.ethiopiancalendar.domain.calculator.MuslimHolidayCalculator
import com.ethiopiancalendar.domain.calculator.OrthodoxHolidayCalculator
import com.ethiopiancalendar.domain.calculator.PublicHolidayCalculator
import com.ethiopiancalendar.domain.model.EthiopicDate
import com.ethiopiancalendar.domain.model.HolidayOccurrence
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central repository for all holiday management
 * Combines public holidays, Orthodox holidays, and Muslim holidays
 */
@Singleton
class HolidayRepository @Inject constructor(
    private val publicHolidayCalculator: PublicHolidayCalculator,
    private val orthodoxHolidayCalculator: OrthodoxHolidayCalculator,
    private val muslimHolidayCalculator: MuslimHolidayCalculator
) {
    
    /**
     * Get all holidays for a specific Ethiopian year
     * Includes public holidays, Orthodox holidays, and Muslim holidays
     */
    fun getHolidaysForYear(
        ethiopianYear: Int,
        includeOrthodox: Boolean = true,
        includeMuslim: Boolean = true,
        includeMuslimWorkingDays: Boolean = false
    ): Flow<List<HolidayOccurrence>> {
        return flow {
            val holidays = mutableListOf<HolidayOccurrence>()
            
            // Always include public holidays
            val publicHolidays = publicHolidayCalculator.getPublicHolidaysForYear(ethiopianYear)
            holidays.addAll(publicHolidays.map { holiday ->
                HolidayOccurrence(
                    holiday = holiday,
                    ethiopicDate = EthiopicDate(
                        year = ethiopianYear,
                        month = holiday.ethiopianMonth,
                        day = holiday.ethiopianDay
                    ),
                    adjustment = 0
                )
            })
            
            // Add Orthodox holidays if enabled
            if (includeOrthodox) {
                val orthodoxHolidays = orthodoxHolidayCalculator.getOrthodoxHolidaysForYear(ethiopianYear)
                holidays.addAll(orthodoxHolidays.map { holiday ->
                    HolidayOccurrence(
                        holiday = holiday,
                        ethiopicDate = EthiopicDate(
                            year = ethiopianYear,
                            month = holiday.ethiopianMonth,
                            day = holiday.ethiopianDay
                        ),
                        adjustment = 0
                    )
                })
            }
            
            // Add Muslim holidays if enabled
            if (includeMuslim) {
                val muslimHolidays = muslimHolidayCalculator.getMuslimHolidaysForEthiopianYear(
                    ethiopianYear = ethiopianYear,
                    includePublicHolidays = true,
                    includeWorkingHolidays = includeMuslimWorkingDays
                )
                holidays.addAll(muslimHolidays.map { holiday ->
                    HolidayOccurrence(
                        holiday = holiday,
                        ethiopicDate = EthiopicDate(
                            year = ethiopianYear,
                            month = holiday.ethiopianMonth,
                            day = holiday.ethiopianDay
                        ),
                        adjustment = 0
                    )
                })
            }
            
            emit(holidays )
        }
    }
    
    /**
     * Get all holidays for a specific Ethiopian month
     */
    fun getHolidaysForMonth(
        ethiopianYear: Int,
        ethiopianMonth: Int,
        includeOrthodox: Boolean = true,
        includeMuslim: Boolean = true
    ): Flow<List<HolidayOccurrence>> {
        return flow {
            val allHolidays = mutableListOf<HolidayOccurrence>()
            
            // Get holidays for the year
            getHolidaysForYear(
                ethiopianYear = ethiopianYear,
                includeOrthodox = includeOrthodox,
                includeMuslim = includeMuslim
            ).collect { yearHolidays ->
                allHolidays.addAll(yearHolidays)
            }
            
            // Filter to only holidays in this month
            val monthHolidays = allHolidays.filter { it.ethiopicDate.month == ethiopianMonth }
            
            emit(monthHolidays)
        }
    }
    
    /**
     * Get holidays for a specific date
     */
    fun getHolidaysForDate(ethiopicDate: EthiopicDate): Flow<List<HolidayOccurrence>> {
        return flow {
            val allHolidays = mutableListOf<HolidayOccurrence>()
            
            getHolidaysForMonth(ethiopicDate.year, ethiopicDate.month).collect { monthHolidays ->
                allHolidays.addAll(monthHolidays)
            }
            
            val dateHolidays = allHolidays.filter { it.ethiopicDate.day == ethiopicDate.day }
            
            emit(dateHolidays)
        }
    }
}
