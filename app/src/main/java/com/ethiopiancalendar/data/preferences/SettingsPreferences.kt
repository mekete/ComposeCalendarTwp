package com.ethiopiancalendar.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings_preferences")

enum class CalendarType {
    ETHIOPIAN,
    GREGOREAN,
    HIRJI
}

enum class Language(val displayName: String) {
    ENGLISH("English"),
    AMHARIC("Amharic"),
    OROMIFFA("Oromiffa"),
    TIGRIGNA("Tigrigna"),
    FRENCH("French")
}

class SettingsPreferences(private val context: Context) {

    // Calendar Display Settings
    private val PRIMARY_CALENDAR_KEY = stringPreferencesKey("primary_calendar")
    private val DISPLAY_DUAL_CALENDAR_KEY = booleanPreferencesKey("display_dual_calendar")
    private val SECONDARY_CALENDAR_KEY = stringPreferencesKey("secondary_calendar")
    private val SHOW_ORTHODOX_DAY_NAMES_KEY = booleanPreferencesKey("show_orthodox_day_names")
    private val SHOW_ORTHODOX_FASTING_HOLIDAYS_KEY = booleanPreferencesKey("show_orthodox_fasting_holidays")
    private val SHOW_MUSLIM_HOLIDAYS_KEY = booleanPreferencesKey("show_muslim_holidays")
    private val SHOW_US_HOLIDAYS_KEY = booleanPreferencesKey("show_us_holidays")
    private val USE_GEEZ_NUMBERS_KEY = booleanPreferencesKey("use_geez_numbers")
    private val USE_24_HOUR_FORMAT_KEY = booleanPreferencesKey("use_24_hour_format_in_widgets")
    private val LANGUAGE_KEY = stringPreferencesKey("app_language")

    // Widget Settings
    private val DISPLAY_TWO_CLOCKS_KEY = booleanPreferencesKey("display_two_clocks")
    private val PRIMARY_WIDGET_TIMEZONE_KEY = stringPreferencesKey("primary_widget_timezone")
    private val SECONDARY_WIDGET_TIMEZONE_KEY = stringPreferencesKey("secondary_widget_timezone")
    private val USE_TRANSPARENT_BACKGROUND_KEY = booleanPreferencesKey("use_transparent_background")

    // Muslim Holiday Offset Settings (from Firebase Remote Config)
    private val DAY_OFFSET_EID_AL_ADHA_KEY = intPreferencesKey("config_day_offset_eid_al_adha")
    private val DAY_OFFSET_EID_AL_FITR_KEY = intPreferencesKey("config_day_offset_eid_al_fitir")
    private val DAY_OFFSET_MAWLID_KEY = intPreferencesKey("config_day_offset_mewlid")
    private val DAY_OFFSET_ETHIO_YEAR_KEY = intPreferencesKey("config_day_offset_ethio_year")

    // Flow properties for observing settings
    val primaryCalendar: Flow<CalendarType> = context.settingsDataStore.data.map { preferences ->
        val calendarString = preferences[PRIMARY_CALENDAR_KEY] ?: CalendarType.ETHIOPIAN.name
        try {
            CalendarType.valueOf(calendarString)
        } catch (e: IllegalArgumentException) {
            CalendarType.ETHIOPIAN
        }
    }

    val displayDualCalendar: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[DISPLAY_DUAL_CALENDAR_KEY] ?: false
    }

    val secondaryCalendar: Flow<CalendarType> = context.settingsDataStore.data.map { preferences ->
        val calendarString = preferences[SECONDARY_CALENDAR_KEY] ?: CalendarType.GREGOREAN.name
        try {
            CalendarType.valueOf(calendarString)
        } catch (e: IllegalArgumentException) {
            CalendarType.GREGOREAN
        }
    }

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

    val language: Flow<Language> = context.settingsDataStore.data.map { preferences ->
        val languageString = preferences[LANGUAGE_KEY] ?: Language.AMHARIC.name
        try {
            Language.valueOf(languageString)
        } catch (e: IllegalArgumentException) {
            Language.AMHARIC
        }
    }

    // Muslim Holiday Offset Flows
    val dayOffsetEidAlAdha: Flow<Int> = context.settingsDataStore.data.map { preferences ->
        preferences[DAY_OFFSET_EID_AL_ADHA_KEY] ?: 0
    }

    val dayOffsetEidAlFitr: Flow<Int> = context.settingsDataStore.data.map { preferences ->
        preferences[DAY_OFFSET_EID_AL_FITR_KEY] ?: 0
    }

    val dayOffsetMawlid: Flow<Int> = context.settingsDataStore.data.map { preferences ->
        preferences[DAY_OFFSET_MAWLID_KEY] ?: 0
    }

    val dayOffsetEthioYear: Flow<Int> = context.settingsDataStore.data.map { preferences ->
        preferences[DAY_OFFSET_ETHIO_YEAR_KEY] ?: 0
    }

    // Setter functions for updating settings
    suspend fun setPrimaryCalendar(calendar: CalendarType) {
        context.settingsDataStore.edit { preferences ->
            preferences[PRIMARY_CALENDAR_KEY] = calendar.name
        }
    }

    suspend fun setDisplayDualCalendar(value: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[DISPLAY_DUAL_CALENDAR_KEY] = value
        }
    }

    suspend fun setSecondaryCalendar(calendar: CalendarType) {
        context.settingsDataStore.edit { preferences ->
            preferences[SECONDARY_CALENDAR_KEY] = calendar.name
        }
    }

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

    suspend fun setLanguage(language: Language) {
        context.settingsDataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language.name
        }
    }

    // Setter functions for Muslim Holiday Offsets
    suspend fun setDayOffsetEidAlAdha(offset: Int) {
        context.settingsDataStore.edit { preferences ->
            preferences[DAY_OFFSET_EID_AL_ADHA_KEY] = offset
        }
    }

    suspend fun setDayOffsetEidAlFitr(offset: Int) {
        context.settingsDataStore.edit { preferences ->
            preferences[DAY_OFFSET_EID_AL_FITR_KEY] = offset
        }
    }

    suspend fun setDayOffsetMawlid(offset: Int) {
        context.settingsDataStore.edit { preferences ->
            preferences[DAY_OFFSET_MAWLID_KEY] = offset
        }
    }

    suspend fun setDayOffsetEthioYear(year: Int) {
        context.settingsDataStore.edit { preferences ->
            preferences[DAY_OFFSET_ETHIO_YEAR_KEY] = year
        }
    }
}
