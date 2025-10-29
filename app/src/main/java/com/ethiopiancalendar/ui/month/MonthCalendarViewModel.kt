package com.ethiopiancalendar.ui.month

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ethiopiancalendar.data.repository.HolidayRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val holidayRepository: HolidayRepository
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

    private val _selectedDate = MutableStateFlow<EthiopicDate?>(null)
    val selectedDate: StateFlow<EthiopicDate?> = _selectedDate.asStateFlow()

    // Cache for month data
    private val monthDataCache = mutableMapOf<Int, MonthCalendarUiState>()

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
        // Check cache first
        monthDataCache[page]?.let {
            return flowOf(it)
        }

        return flow {
            emit(MonthCalendarUiState.Loading)

            try {
                val currentMonth = getEthiopicDateForPage(page)
                val year = currentMonth.get(ChronoField.YEAR_OF_ERA)
                val month = currentMonth.get(ChronoField.MONTH_OF_YEAR)

                holidayRepository.getHolidaysForMonth(year, month).collect { holidays ->
                    val dateList = generateDateListForMonth(currentMonth)

                    val state = MonthCalendarUiState.Success(
                        currentMonth = currentMonth,
                        dateList = dateList,
                        holidays = holidays,
                        selectedDate = _selectedDate.value
                    )

                    // Cache the result
                    monthDataCache[page] = state
                    emit(state)

                    Timber.d("Loaded page $page: ${holidays.size} holidays for $currentMonth")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading month data for page $page")
                val errorState = MonthCalendarUiState.Error(e.message ?: "Unknown error")
                emit(errorState)
            }
        }
    }

    /**
     * Generate 42 date cells for calendar grid (6 weeks × 7 days)
     */
    private fun generateDateListForMonth(month: EthiopicDate): List<EthiopicDate> {
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
            val prevMonth = firstDayOfMonth.plus(1, ChronoUnit.DAYS) as EthiopicDate
            val prevYear = prevMonth.get(ChronoField.YEAR_OF_ERA)
            val prevMonthValue = prevMonth.get(ChronoField.MONTH_OF_YEAR)
            val daysInPrevMonth = getDaysInMonth(prevYear, prevMonthValue)

            for (i in (daysInPrevMonth - dayOffset + 1)..daysInPrevMonth) {
                dateList.add(EthiopicDate.of(prevYear, prevMonthValue, i))
            }
        }

        // Add days of current month
        for (day in 1..daysInMonth) {
            dateList.add(EthiopicDate.of(year, monthValue, day))
        }

        // Add days from next month to complete the grid
        val remainingCells = 42 - dateList.size
        if (remainingCells > 0) {
            val nextMonth = firstDayOfMonth.plus(1, ChronoUnit.DAYS)  as EthiopicDate
            val nextYear = nextMonth.get(ChronoField.YEAR_OF_ERA)
            val nextMonthValue = nextMonth.get(ChronoField.MONTH_OF_YEAR)

            for (day in 1..remainingCells) {
                dateList.add(EthiopicDate.of(nextYear, nextMonthValue, day))
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

    // User actions
    fun selectDate(date: EthiopicDate) {
        _selectedDate.value = date
        // Clear cache to refresh UI with new selection
        monthDataCache.clear()
        Timber.d("Selected date: $date")
    }

    fun clearCache() {
        monthDataCache.clear()
    }

    fun getTodayPage(): Int {
        val today = EthiopicDate.now() as EthiopicDate
        return getPageForEthiopicDate(today)
    }
}