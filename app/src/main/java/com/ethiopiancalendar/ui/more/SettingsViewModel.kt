package com.ethiopiancalendar.ui.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ethiopiancalendar.data.preferences.CalendarType
import com.ethiopiancalendar.data.preferences.SettingsPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsPreferences: SettingsPreferences
) : ViewModel() {

    // Calendar Display Settings
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

    val showOrthodoxDayNames: StateFlow<Boolean> = settingsPreferences.showOrthodoxDayNames
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val showOrthodoxFastingHolidays: StateFlow<Boolean> = settingsPreferences.showOrthodoxFastingHolidays
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val showMuslimHolidays: StateFlow<Boolean> = settingsPreferences.showMuslimHolidays
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val showUsHolidays: StateFlow<Boolean> = settingsPreferences.showUsHolidays
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val useGeezNumbers: StateFlow<Boolean> = settingsPreferences.useGeezNumbers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val use24HourFormat: StateFlow<Boolean> = settingsPreferences.use24HourFormat
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    // Widget Settings
    val displayTwoClocks: StateFlow<Boolean> = settingsPreferences.displayTwoClocks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val primaryWidgetTimezone: StateFlow<String> = settingsPreferences.primaryWidgetTimezone
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    val secondaryWidgetTimezone: StateFlow<String> = settingsPreferences.secondaryWidgetTimezone
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    val useTransparentBackground: StateFlow<Boolean> = settingsPreferences.useTransparentBackground
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    // Setter functions for Calendar Display Settings
    fun setPrimaryCalendar(calendar: CalendarType) {
        viewModelScope.launch {
            settingsPreferences.setPrimaryCalendar(calendar)
        }
    }

    fun setDisplayDualCalendar(value: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setDisplayDualCalendar(value)
        }
    }

    fun setSecondaryCalendar(calendar: CalendarType) {
        viewModelScope.launch {
            settingsPreferences.setSecondaryCalendar(calendar)
        }
    }

    fun setShowOrthodoxDayNames(value: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setShowOrthodoxDayNames(value)
        }
    }

    fun setShowOrthodoxFastingHolidays(value: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setShowOrthodoxFastingHolidays(value)
        }
    }

    fun setShowMuslimHolidays(value: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setShowMuslimHolidays(value)
        }
    }

    fun setShowUsHolidays(value: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setShowUsHolidays(value)
        }
    }

    fun setUseGeezNumbers(value: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setUseGeezNumbers(value)
        }
    }

    fun setUse24HourFormat(value: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setUse24HourFormat(value)
        }
    }

    // Setter functions for Widget Settings
    fun setDisplayTwoClocks(value: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setDisplayTwoClocks(value)
        }
    }

    fun setPrimaryWidgetTimezone(value: String) {
        viewModelScope.launch {
            settingsPreferences.setPrimaryWidgetTimezone(value)
        }
    }

    fun setSecondaryWidgetTimezone(value: String) {
        viewModelScope.launch {
            settingsPreferences.setSecondaryWidgetTimezone(value)
        }
    }

    fun setUseTransparentBackground(value: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setUseTransparentBackground(value)
        }
    }
}
