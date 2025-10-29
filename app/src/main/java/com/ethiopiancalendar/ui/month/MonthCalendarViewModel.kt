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

    private val _currentMonth = MutableStateFlow(EthiopicDate.now() as EthiopicDate)
    val currentMonth: StateFlow<EthiopicDate> = _currentMonth.asStateFlow()

    private val _selectedDate = MutableStateFlow<EthiopicDate?>(null)
    val selectedDate: StateFlow<EthiopicDate?> = _selectedDate.asStateFlow()

    private val _uiState = MutableStateFlow<MonthCalendarUiState>(MonthCalendarUiState.Loading)
    val uiState: StateFlow<MonthCalendarUiState> = _uiState.asStateFlow()

    init {
        loadMonthData()
    }

    private fun loadMonthData() {
        viewModelScope.launch {
            try {
                val year = _currentMonth.value.get(ChronoField.YEAR_OF_ERA)
                val month = _currentMonth.value.get(ChronoField.MONTH_OF_YEAR)

                holidayRepository.getHolidaysForMonth(
                    year,
                    month
                ).collect { holidays ->
                    val dateList = generateDateListForMonth(_currentMonth.value)

                    _uiState.value = MonthCalendarUiState.Success(
                        currentMonth = _currentMonth.value,
                        dateList = dateList,
                        holidays = holidays,
                        selectedDate = _selectedDate.value
                    )

                    Timber.d("Loaded ${holidays.size} holidays for ${_currentMonth.value}")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading month data")
                _uiState.value = MonthCalendarUiState.Error(e.message ?: "Unknown error")
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
            val prevMonth = firstDayOfMonth.plus(-1, ChronoUnit.DAYS)  as EthiopicDate
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
            val nextMonth = firstDayOfMonth.plus(1, ChronoUnit.DAYS) as EthiopicDate
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
        loadMonthData()
        Timber.d("Selected date: $date")
    }

    fun nextMonth() {
        _currentMonth.value = _currentMonth.value.plus(1, ChronoUnit.DAYS) as EthiopicDate
        loadMonthData()
        Timber.d("Navigated to next month: ${_currentMonth.value}")
    }

    fun previousMonth() {
        _currentMonth.value = _currentMonth.value.plus(-1, ChronoUnit.DAYS) as EthiopicDate
        loadMonthData()
        Timber.d("Navigated to previous month: ${_currentMonth.value}")
    }

    fun goToToday() {
        val today = EthiopicDate.now() as EthiopicDate
        _currentMonth.value = today
        _selectedDate.value = today
        loadMonthData()
        Timber.d("Navigated to today: ${_currentMonth.value}")
    }
}