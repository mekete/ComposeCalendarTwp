package com.ethiopiancalendar.ui.month

import com.ethiopiancalendar.domain.model.EthiopianDate
import com.ethiopiancalendar.domain.model.HolidayOccurrence

/**
 * UI state for month calendar screen
 */
sealed class MonthCalendarUiState {
    object Loading : MonthCalendarUiState()
    
    data class Success(
        val currentMonth: EthiopianDate,
        val dateList: List<EthiopianDate>,
        val holidays: List<HolidayOccurrence>,
        val selectedDate: EthiopianDate?
    ) : MonthCalendarUiState()
    
    data class Error(val message: String) : MonthCalendarUiState()
}
