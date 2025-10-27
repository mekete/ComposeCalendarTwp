package com.ethiopiancalendar.ui.month

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ethiopiancalendar.data.repository.HolidayRepository
import com.ethiopiancalendar.domain.model.EthiopicDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.DayOfWeek
import javax.inject.Inject

@HiltViewModel
class MonthCalendarViewModel @Inject constructor(
    private val holidayRepository: HolidayRepository
) : ViewModel() {
    
    private val _currentMonth = MutableStateFlow(EthiopicDate.now())
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
                holidayRepository.getHolidaysForMonth(
                    _currentMonth.value.year,
                    _currentMonth.value.month
                ).collect { holidays ->
                    val dateList = generateDateListForMonth(_currentMonth.value)
                    
                    _uiState.value = MonthCalendarUiState.Success(
                        currentMonth = _currentMonth.value,
                        dateList = dateList,
                        holidays = holidays,
                        selectedDate = _selectedDate.value
                    )
                    
                    Timber.d("Loaded ${holidays.size} holidays for ${_currentMonth.value.format()}")
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
        val firstDayOfMonth = EthiopicDate(month.year, month.month, 1, DayOfWeek.MONDAY)
        val daysInMonth = getDaysInMonth(month.year, month.month)
        
        val dateList = mutableListOf<EthiopicDate>()
        
        // Add days from previous month to fill first week
        val dayOffset = (firstDayOfMonth.toGregorianDate().dayOfWeek.value - 1) % 7
        if (dayOffset > 0) {
            val prevMonth = month.plusMonths(-1)
            val daysInPrevMonth = getDaysInMonth(prevMonth.year, prevMonth.month)
            
            for (i in (daysInPrevMonth - dayOffset + 1)..daysInPrevMonth) {
                dateList.add(EthiopicDate(prevMonth.year, prevMonth.month, i, DayOfWeek.MONDAY))
            }
        }
        
        // Add days of current month
        for (day in 1..daysInMonth) {
            dateList.add(EthiopicDate(month.year, month.month, day, DayOfWeek.MONDAY))
        }
        
        // Add days from next month to complete the grid
        val remainingCells = 42 - dateList.size
        if (remainingCells > 0) {
            val nextMonth = month.plusMonths(1)
            for (day in 1..remainingCells) {
                dateList.add(EthiopicDate(nextMonth.year, nextMonth.month, day, DayOfWeek.MONDAY))
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
        Timber.d("Selected date: ${date.format()}")
    }
    
    fun nextMonth() {
        _currentMonth.value = _currentMonth.value.plusMonths(1)
        loadMonthData()
        Timber.d("Navigated to next month: ${_currentMonth.value.format()}")
    }
    
    fun previousMonth() {
        _currentMonth.value = _currentMonth.value.plusMonths(-1)
        loadMonthData()
        Timber.d("Navigated to previous month: ${_currentMonth.value.format()}")
    }
    
    fun goToToday() {
        _currentMonth.value = EthiopicDate.now()
        _selectedDate.value = EthiopicDate.now()
        loadMonthData()
        Timber.d("Navigated to today: ${_currentMonth.value.format()}")
    }
}
