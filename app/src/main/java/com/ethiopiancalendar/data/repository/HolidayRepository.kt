package com.ethiopiancalendar.data.repository

import com.ethiopiancalendar.domain.calculator.MuslimHolidayCalculator
import com.ethiopiancalendar.domain.calculator.OrthodoxHolidayCalculator
import com.ethiopiancalendar.domain.calculator.PublicHolidayCalculator
import com.ethiopiancalendar.domain.model.HolidayOccurrence
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.threeten.extra.chrono.EthiopicDate
import java.time.temporal.ChronoField
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HolidayRepository @Inject constructor(
    private val publicHolidayCalculator: PublicHolidayCalculator,
    private val orthodoxHolidayCalculator: OrthodoxHolidayCalculator,
    private val muslimHolidayCalculator: MuslimHolidayCalculator
) {

    fun getHolidaysForYear(
        ethiopianYear: Int,
        includeOrthodox: Boolean = true,
        includeMuslim: Boolean = true,
        includeMuslimWorkingDays: Boolean = false
    ): Flow<List<HolidayOccurrence>> {
        return flow {
            val holidays = mutableListOf<HolidayOccurrence>()

            // Public holidays
            val publicHolidays = publicHolidayCalculator.getPublicHolidaysForYear(ethiopianYear)
            holidays.addAll(publicHolidays.map { holiday ->
                HolidayOccurrence(
                    holiday = holiday,
                    ethiopicDate = EthiopicDate.of(
                        ethiopianYear,
                        holiday.ethiopianMonth,
                        holiday.ethiopianDay
                    ),
                    adjustment = 0
                )
            })

            // Orthodox holidays
            if (includeOrthodox) {
                val orthodoxHolidays = orthodoxHolidayCalculator.getOrthodoxHolidaysForYear(ethiopianYear)
                holidays.addAll(orthodoxHolidays.map { holiday ->
                    HolidayOccurrence(
                        holiday = holiday,
                        ethiopicDate = EthiopicDate.of(
                            ethiopianYear,
                            holiday.ethiopianMonth,
                            holiday.ethiopianDay
                        ),
                        adjustment = 0
                    )
                })
            }

            // Muslim holidays
            if (includeMuslim) {
                val muslimHolidays = muslimHolidayCalculator.getMuslimHolidaysForEthiopianYear(
                    ethiopianYear = ethiopianYear,
                    includePublicHolidays = true,
                    includeWorkingHolidays = includeMuslimWorkingDays
                )
                holidays.addAll(muslimHolidays.map { holiday ->
                    HolidayOccurrence(
                        holiday = holiday,
                        ethiopicDate = EthiopicDate.of(
                            ethiopianYear,
                            holiday.ethiopianMonth,
                            holiday.ethiopianDay
                        ),
                        adjustment = 0
                    )
                })
            }

            emit(holidays)
        }
    }

    fun getHolidaysForMonth(
        ethiopianYear: Int,
        ethiopianMonth: Int,
        includeOrthodox: Boolean = true,
        includeMuslim: Boolean = true
    ): Flow<List<HolidayOccurrence>> {
        return flow {
            val allHolidays = mutableListOf<HolidayOccurrence>()
            getHolidaysForYear(
                ethiopianYear = ethiopianYear,
                includeOrthodox = includeOrthodox,
                includeMuslim = includeMuslim
            ).collect { yearHolidays ->
                allHolidays.addAll(yearHolidays)
            }
            val monthHolidays = allHolidays.filter { it.ethiopicDate.get(ChronoField.MONTH_OF_YEAR) == ethiopianMonth }
            emit(monthHolidays)
        }
    }

    fun getHolidaysForDate(ethiopicDate: EthiopicDate): Flow<List<HolidayOccurrence>> {
        return flow {
            val allHolidays = mutableListOf<HolidayOccurrence>()
            getHolidaysForMonth(ethiopicDate.get(ChronoField.YEAR_OF_ERA), ethiopicDate.get(ChronoField.MONTH_OF_YEAR)).collect { monthHolidays ->
                allHolidays.addAll(monthHolidays)
            }

            val dateHolidays = allHolidays.filter { it.ethiopicDate.get(ChronoField.DAY_OF_MONTH) == ethiopicDate.get(ChronoField.DAY_OF_MONTH) }
            emit(dateHolidays)
        }
    }
}
