package com.ethiopiancalendar.ui.month

import com.ethiopiancalendar.data.local.entity.EventInstance
import com.ethiopiancalendar.data.preferences.CalendarType
import com.ethiopiancalendar.domain.model.HolidayOccurrence
import org.threeten.extra.chrono.EthiopicDate

/**
 * UI state for month calendar screen
 */
sealed class MonthCalendarUiState {
    object Loading : MonthCalendarUiState()

    data class Success(
        val currentMonth: EthiopicDate,
        val dateList: List<EthiopicDate>,
        val holidays: List<HolidayOccurrence>,
        val events: List<EventInstance>,
        val selectedDate: EthiopicDate?,
        val primaryCalendar: CalendarType,
        val displayDualCalendar: Boolean,
        val secondaryCalendar: CalendarType,
        val currentGregorianYear: Int?,
        val currentGregorianMonth: Int?
    ) : MonthCalendarUiState()

    data class Error(val message: String) : MonthCalendarUiState()
}