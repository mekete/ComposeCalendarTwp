package com.ethiopiancalendar.ui.holidaylist

import com.ethiopiancalendar.domain.model.HolidayOccurrence
import com.ethiopiancalendar.domain.model.HolidayType

sealed class HolidayListUiState {
    data object Loading : HolidayListUiState()

    data class Success(
        val currentYear: Int,
        val allHolidays: List<HolidayOccurrence>,
        val filteredHolidays: List<HolidayOccurrence>,
        val selectedFilters: Set<HolidayType>
    ) : HolidayListUiState()

    data class Error(val message: String) : HolidayListUiState()
}
