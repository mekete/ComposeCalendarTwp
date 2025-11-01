package com.ethiopiancalendar.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings_preferences")

class SettingsPreferences(private val context: Context) {

    // Calendar Display Settings
    private val SHOW_ORTHODOX_DAY_NAMES_KEY = booleanPreferencesKey("show_orthodox_day_names")
    private val SHOW_ORTHODOX_FASTING_HOLIDAYS_KEY = booleanPreferencesKey("show_orthodox_fasting_holidays")
    private val SHOW_MUSLIM_HOLIDAYS_KEY = booleanPreferencesKey("show_muslim_holidays")
    private val SHOW_US_HOLIDAYS_KEY = booleanPreferencesKey("show_us_holidays")
    private val USE_GEEZ_NUMBERS_KEY = booleanPreferencesKey("use_geez_numbers")
    private val USE_24_HOUR_FORMAT_KEY = booleanPreferencesKey("use_24_hour_format_in_widgets")

    // Widget Settings
    private val DISPLAY_TWO_CLOCKS_KEY = booleanPreferencesKey("display_two_clocks")
    private val PRIMARY_WIDGET_TIMEZONE_KEY = stringPreferencesKey("primary_widget_timezone")
    private val SECONDARY_WIDGET_TIMEZONE_KEY = stringPreferencesKey("secondary_widget_timezone")
    private val USE_TRANSPARENT_BACKGROUND_KEY = booleanPreferencesKey("use_transparent_background")

    // Flow properties for observing settings
    val showOrthodoxDayNames: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[SHOW_ORTHODOX_DAY_NAMES_KEY] ?: false
    }

    val showOrthodoxFastingHolidays: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[SHOW_ORTHODOX_FASTING_HOLIDAYS_KEY] ?: false
    }

    val showMuslimHolidays: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[SHOW_MUSLIM_HOLIDAYS_KEY] ?: false
    }

    val showUsHolidays: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[SHOW_US_HOLIDAYS_KEY] ?: false
    }

    val useGeezNumbers: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[USE_GEEZ_NUMBERS_KEY] ?: false
    }

    val use24HourFormat: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[USE_24_HOUR_FORMAT_KEY] ?: false
    }

    val displayTwoClocks: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[DISPLAY_TWO_CLOCKS_KEY] ?: false
    }

    val primaryWidgetTimezone: Flow<String> = context.settingsDataStore.data.map { preferences ->
        preferences[PRIMARY_WIDGET_TIMEZONE_KEY] ?: ""
    }

    val secondaryWidgetTimezone: Flow<String> = context.settingsDataStore.data.map { preferences ->
        preferences[SECONDARY_WIDGET_TIMEZONE_KEY] ?: ""
    }

    val useTransparentBackground: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[USE_TRANSPARENT_BACKGROUND_KEY] ?: false
    }

    // Setter functions for updating settings
    suspend fun setShowOrthodoxDayNames(value: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[SHOW_ORTHODOX_DAY_NAMES_KEY] = value
        }
    }

    suspend fun setShowOrthodoxFastingHolidays(value: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[SHOW_ORTHODOX_FASTING_HOLIDAYS_KEY] = value
        }
    }

    suspend fun setShowMuslimHolidays(value: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[SHOW_MUSLIM_HOLIDAYS_KEY] = value
        }
    }

    suspend fun setShowUsHolidays(value: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[SHOW_US_HOLIDAYS_KEY] = value
        }
    }

    suspend fun setUseGeezNumbers(value: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[USE_GEEZ_NUMBERS_KEY] = value
        }
    }

    suspend fun setUse24HourFormat(value: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[USE_24_HOUR_FORMAT_KEY] = value
        }
    }

    suspend fun setDisplayTwoClocks(value: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[DISPLAY_TWO_CLOCKS_KEY] = value
        }
    }

    suspend fun setPrimaryWidgetTimezone(value: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[PRIMARY_WIDGET_TIMEZONE_KEY] = value
        }
    }

    suspend fun setSecondaryWidgetTimezone(value: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[SECONDARY_WIDGET_TIMEZONE_KEY] = value
        }
    }

    suspend fun setUseTransparentBackground(value: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[USE_TRANSPARENT_BACKGROUND_KEY] = value
        }
    }
}
