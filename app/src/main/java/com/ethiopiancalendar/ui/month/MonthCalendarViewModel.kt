package com.ethiopiancalendar.ui.month

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ethiopiancalendar.data.preferences.CalendarType
import com.ethiopiancalendar.data.preferences.SettingsPreferences
import com.ethiopiancalendar.data.repository.EventRepository
import com.ethiopiancalendar.data.repository.HolidayRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.threeten.extra.chrono.EthiopicDate
import timber.log.Timber
import java.time.LocalDate
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class MonthCalendarViewModel @Inject constructor(
    private val holidayRepository: HolidayRepository,
    private val eventRepository: EventRepository,
    private val settingsPreferences: SettingsPreferences
) : ViewModel() {

    companion object {
        // Paging range: ±60 months (5 years)
        const val MONTHS_BEFORE = 60
        const val MONTHS_AFTER = 60
        const val TOTAL_PAGES = MONTHS_BEFORE + 1 + MONTHS_AFTER // 121 pages
    }

    // Reference date: current Ethiopian date
    private val referenceDate = EthiopicDate.now() as EthiopicDate
    private val referenceYear = referenceDate.get(ChronoField.YEAR_OF_ERA)
    private val referenceMonth = referenceDate.get(ChronoField.MONTH_OF_YEAR)

    // Calculate initial page (center of range)
    val initialPage = MONTHS_BEFORE

    // Calendar display preferences
    val primaryCalendar: StateFlow<CalendarType> = settingsPreferences.primaryCalendar
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CalendarType.ETHIOPIAN
        )

    val displayDualCalendar: StateFlow<Boolean> = settingsPreferences.displayDualCalendar
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val secondaryCalendar: StateFlow<CalendarType> = settingsPreferences.secondaryCalendar
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CalendarType.GREGOREAN
        )

    private val _selectedDate = MutableStateFlow<EthiopicDate?>(null)
    val selectedDate: StateFlow<EthiopicDate?> = _selectedDate.asStateFlow()

    /**
     * Get Ethiopian date for a specific page index
     */
    fun getEthiopicDateForPage(page: Int): EthiopicDate {
        val monthOffset = page - MONTHS_BEFORE
        var targetYear = referenceYear
        var targetMonth = referenceMonth + monthOffset

        // Handle year wrapping
        while (targetMonth > 13) {
            targetMonth -= 13
            targetYear++
        }
        while (targetMonth < 1) {
            targetMonth += 13
            targetYear--
        }

        return EthiopicDate.of(targetYear, targetMonth, 1)
    }

    /**
     * Get page index for a specific Ethiopian date
     */
    fun getPageForEthiopicDate(date: EthiopicDate): Int {
        val year = date.get(ChronoField.YEAR_OF_ERA)
        val month = date.get(ChronoField.MONTH_OF_YEAR)

        val yearDiff = year - referenceYear
        val monthDiff = month - referenceMonth
        val totalMonthDiff = yearDiff * 13 + monthDiff

        return MONTHS_BEFORE + totalMonthDiff
    }

    /**
     * Load month data for a specific page
     */
    fun loadMonthDataForPage(page: Int): Flow<MonthCalendarUiState> {
        return flow {
            emit(MonthCalendarUiState.Loading)

            try {
                val currentMonth = getEthiopicDateForPage(page)
                val year = currentMonth.get(ChronoField.YEAR_OF_ERA)
                val month = currentMonth.get(ChronoField.MONTH_OF_YEAR)

                // Combine preferences with holiday and event data
                // This will automatically react to preference changes and date selection
                combine(
                    holidayRepository.getHolidaysForMonth(year, month),
                    eventRepository.getEventsForMonth(year, month),
                    primaryCalendar,
                    displayDualCalendar,
                    secondaryCalendar,
                    _selectedDate
                ) { holidays, events, primary, displayDual, secondary, selected ->
                    val dateList = generateDateListForMonth(currentMonth, primary)

                    // Calculate Gregorian month/year when Gregorian is primary
                    val (gregorianYear, gregorianMonth) = if (primary == CalendarType.GREGOREAN) {
                        calculateGregorianMonthForDisplay(currentMonth)
                    } else {
                        Pair(null, null)
                    }

                    MonthCalendarUiState.Success(
                        currentMonth = currentMonth,
                        dateList = dateList,
                        holidays = holidays,
                        events = events,
                        selectedDate = selected,
                        primaryCalendar = primary,
                        displayDualCalendar = displayDual,
                        secondaryCalendar = secondary,
                        currentGregorianYear = gregorianYear,
                        currentGregorianMonth = gregorianMonth
                    )
                }.collect { state ->
                    emit(state)
                    Timber.d("Loaded page $page: ${state.holidays.size} holidays, ${state.events.size} events for $currentMonth")
                }
            } catch (e: CancellationException) {
                // Don't log cancellation exceptions - they're expected when composition is left
                throw e
            } catch (e: Exception) {
                Timber.e(e, "Error loading month data for page $page")
                val errorState = MonthCalendarUiState.Error(e.message ?: "Unknown error")
                emit(errorState)
            }
        }
    }

    /**
     * Generate 42 date cells for calendar grid (6 weeks × 7 days)
     * Based on the primary calendar type (Ethiopian or Gregorian)
     */
    private fun generateDateListForMonth(month: EthiopicDate, primaryCalendar: CalendarType): List<EthiopicDate> {
        return when (primaryCalendar) {
            CalendarType.ETHIOPIAN -> generateEthiopianMonthGrid(month)
            CalendarType.GREGOREAN -> generateGregorianMonthGrid(month)
            CalendarType.HIRJI -> generateEthiopianMonthGrid(month) // Fallback to Ethiopian for now
        }
    }

    /**
     * Generate calendar grid based on Ethiopian month
     */
    private fun generateEthiopianMonthGrid(month: EthiopicDate): List<EthiopicDate> {
        val year = month.get(ChronoField.YEAR_OF_ERA)
        val monthValue = month.get(ChronoField.MONTH_OF_YEAR)

        val firstDayOfMonth = EthiopicDate.of(year, monthValue, 1)
        val daysInMonth = getDaysInMonth(year, monthValue)

        val dateList = mutableListOf<EthiopicDate>()

        // Add days from previous month to fill first week
        val gregorianFirstDay = LocalDate.from(firstDayOfMonth)
        val firstDayWeekday = gregorianFirstDay.dayOfWeek.value
        val dayOffset = (firstDayWeekday - 1) % 7

        if (dayOffset > 0) {
            // Use plus with negative value to go backwards
            var prevDate = firstDayOfMonth.plus(-1, ChronoUnit.DAYS) as EthiopicDate
            val prevDatesToAdd = mutableListOf<EthiopicDate>()

            // Collect previous dates in reverse
            for (i in 0 until dayOffset) {
                prevDatesToAdd.add(prevDate)
                if (i < dayOffset - 1) {
                    prevDate = prevDate.plus(-1, ChronoUnit.DAYS) as EthiopicDate
                }
            }

            // Add them in correct order (oldest to newest)
            dateList.addAll(prevDatesToAdd.reversed())
        }

        // Add days of current month
        for (day in 1..daysInMonth) {
            dateList.add(EthiopicDate.of(year, monthValue, day))
        }

        // Add days from next month to complete the grid
        val remainingCells = 42 - dateList.size
        if (remainingCells > 0) {
            // Use plus with positive value to go forwards
            var nextDate = EthiopicDate.of(year, monthValue, daysInMonth).plus(1, ChronoUnit.DAYS) as EthiopicDate

            for (i in 0 until remainingCells) {
                dateList.add(nextDate)
                if (i < remainingCells - 1) {
                    nextDate = nextDate.plus(1, ChronoUnit.DAYS) as EthiopicDate
                }
            }
        }

        return dateList
    }

    /**
     * Generate calendar grid based on Gregorian month
     * The input month is Ethiopian, but we'll find the corresponding Gregorian month
     */
    private fun generateGregorianMonthGrid(ethiopianMonth: EthiopicDate): List<EthiopicDate> {
        // Use the last day of the Ethiopian month as reference to find the Gregorian month.
        // This ensures when an Ethiopian month spans two Gregorian months,
        // we show the Gregorian month that contains the end of the Ethiopian month,
        // which is more intuitive when the primary calendar is Gregorian.
        val ethiopianYear = ethiopianMonth.get(ChronoField.YEAR_OF_ERA)
        val ethiopianMonthValue = ethiopianMonth.get(ChronoField.MONTH_OF_YEAR)
        val daysInEthiopianMonth = getDaysInMonth(ethiopianYear, ethiopianMonthValue)

        // Use the last day of the Ethiopian month
        val referenceDay = daysInEthiopianMonth
        val referenceEthiopianDate = EthiopicDate.of(ethiopianYear, ethiopianMonthValue, referenceDay)

        // Convert to Gregorian to find the Gregorian month to display
        val gregorianDate = LocalDate.from(referenceEthiopianDate)
        val year = gregorianDate.year
        val month = gregorianDate.monthValue

        // Get first day of the Gregorian month
        val firstDayOfMonth = LocalDate.of(year, month, 1)
        val daysInMonth = firstDayOfMonth.lengthOfMonth()

        val dateList = mutableListOf<EthiopicDate>()

        // Add days from previous month to fill first week
        val firstDayWeekday = firstDayOfMonth.dayOfWeek.value
        val dayOffset = (firstDayWeekday - 1) % 7

        if (dayOffset > 0) {
            var prevDate = firstDayOfMonth.minusDays(1)
            val prevDatesToAdd = mutableListOf<LocalDate>()

            for (i in 0 until dayOffset) {
                prevDatesToAdd.add(prevDate)
                if (i < dayOffset - 1) {
                    prevDate = prevDate.minusDays(1)
                }
            }

            // Convert to EthiopicDate and add in correct order
            dateList.addAll(prevDatesToAdd.reversed().map { EthiopicDate.from(it) })
        }

        // Add days of current Gregorian month
        for (day in 1..daysInMonth) {
            val gregorianDay = LocalDate.of(year, month, day)
            dateList.add(EthiopicDate.from(gregorianDay))
        }

        // Add days from next month to complete the grid
        val remainingCells = 42 - dateList.size
        if (remainingCells > 0) {
            var nextDate = LocalDate.of(year, month, daysInMonth).plusDays(1)

            for (i in 0 until remainingCells) {
                dateList.add(EthiopicDate.from(nextDate))
                if (i < remainingCells - 1) {
                    nextDate = nextDate.plusDays(1)
                }
            }
        }

        return dateList
    }

    private fun getDaysInMonth(year: Int, month: Int): Int {
        return when (month) {
            13 -> if (year % 4 == 3) 6 else 5  // Pagume
            else -> 30
        }
    }

    /**
     * Calculate which Gregorian month to display for a given Ethiopian month
     * Uses the same logic as generateGregorianMonthGrid
     */
    private fun calculateGregorianMonthForDisplay(ethiopianMonth: EthiopicDate): Pair<Int, Int> {
        val ethiopianYear = ethiopianMonth.get(ChronoField.YEAR_OF_ERA)
        val ethiopianMonthValue = ethiopianMonth.get(ChronoField.MONTH_OF_YEAR)
        val daysInEthiopianMonth = getDaysInMonth(ethiopianYear, ethiopianMonthValue)

        // Use the last day of the Ethiopian month
        val referenceDay = daysInEthiopianMonth
        val referenceEthiopianDate = EthiopicDate.of(ethiopianYear, ethiopianMonthValue, referenceDay)

        // Convert to Gregorian to find the Gregorian month to display
        val gregorianDate = LocalDate.from(referenceEthiopianDate)
        val year = gregorianDate.year
        val month = gregorianDate.monthValue

        return Pair(year, month)
    }

    // User actions
    fun selectDate(date: EthiopicDate) {
        _selectedDate.value = date
        Timber.d("Selected date: $date")
    }

    fun getTodayPage(): Int {
        val today = EthiopicDate.now() as EthiopicDate
        return getPageForEthiopicDate(today)
    }
}