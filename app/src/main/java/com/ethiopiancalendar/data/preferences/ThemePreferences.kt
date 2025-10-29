package com.ethiopiancalendar.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ethiopiancalendar.ui.theme.AppTheme
import com.ethiopiancalendar.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "theme_preferences")

class ThemePreferences(private val context: Context) {
    private val APP_THEME_KEY = intPreferencesKey("app_theme")
    private val THEME_MODE_KEY = intPreferencesKey("theme_mode")

    val appTheme: Flow<AppTheme> = context.dataStore.data.map { preferences ->
        AppTheme.fromOrdinal(preferences[APP_THEME_KEY] ?: AppTheme.BLUE.ordinal)
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        ThemeMode.fromOrdinal(preferences[THEME_MODE_KEY] ?: ThemeMode.SYSTEM.ordinal)
    }

    suspend fun setAppTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[APP_THEME_KEY] = theme.ordinal
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode.ordinal
        }
    }
}
