package com.ethiopiancalendar.ui.holidaylist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ethiopiancalendar.data.repository.HolidayRepository
import com.ethiopiancalendar.domain.model.HolidayType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.threeten.extra.chrono.EthiopicDate
import javax.inject.Inject

@HiltViewModel
class HolidayListViewModel @Inject constructor(
    private val holidayRepository: HolidayRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HolidayListUiState>(HolidayListUiState.Loading)
    val uiState: StateFlow<HolidayListUiState> = _uiState.asStateFlow()

    private var currentYear: Int = EthiopicDate.now().year
    private var selectedFilters: Set<HolidayType> = setOf(
        HolidayType.NATIONAL,
        HolidayType.ORTHODOX_CHRISTIAN,
        HolidayType.MUSLIM
    )

    init {
        loadHolidaysForYear()
    }

    fun incrementYear() {
        currentYear++
        loadHolidaysForYear()
    }

    fun decrementYear() {
        currentYear--
        loadHolidaysForYear()
    }

    fun toggleFilter(holidayType: HolidayType) {
        selectedFilters = if (selectedFilters.contains(holidayType)) {
            selectedFilters - holidayType
        } else {
            selectedFilters + holidayType
        }
        applyFilters()
    }

    private fun loadHolidaysForYear() {
        viewModelScope.launch {
            _uiState.value = HolidayListUiState.Loading
            holidayRepository.getHolidaysForYear(
                ethiopianYear = currentYear,
                includeNational = true,
                includeOrthodox = true,
                includeMuslim = true
            )
                .catch { e ->
                    _uiState.value = HolidayListUiState.Error(
                        message = e.message ?: "Failed to load holidays"
                    )
                }
                .collect { holidays ->
                    val sortedHolidays = holidays.sortedBy { it.date }
                    _uiState.value = HolidayListUiState.Success(
                        currentYear = currentYear,
                        allHolidays = sortedHolidays,
                        filteredHolidays = sortedHolidays.filter {
                            selectedFilters.contains(it.holiday.type)
                        },
                        selectedFilters = selectedFilters
                    )
                }
        }
    }

    private fun applyFilters() {
        val currentState = _uiState.value
        if (currentState is HolidayListUiState.Success) {
            _uiState.value = currentState.copy(
                filteredHolidays = currentState.allHolidays.filter {
                    selectedFilters.contains(it.holiday.type)
                },
                selectedFilters = selectedFilters
            )
        }
    }
}
