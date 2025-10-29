package com.ethiopiancalendar.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

// Blue Color Schemes
private val BlueLightColorScheme = lightColorScheme(
    primary = blue_light_primary,
    onPrimary = blue_light_onPrimary,
    primaryContainer = blue_light_primaryContainer,
    onPrimaryContainer = blue_light_onPrimaryContainer,
    secondary = blue_light_secondary,
    onSecondary = blue_light_onSecondary,
    secondaryContainer = blue_light_secondaryContainer,
    onSecondaryContainer = blue_light_onSecondaryContainer,
    tertiary = blue_light_tertiary,
    onTertiary = blue_light_onTertiary,
    tertiaryContainer = blue_light_tertiaryContainer,
    onTertiaryContainer = blue_light_onTertiaryContainer,
    background = blue_light_background,
    onBackground = blue_light_onBackground,
    surface = blue_light_surface,
    onSurface = blue_light_onSurface,
    surfaceVariant = blue_light_surfaceVariant,
    onSurfaceVariant = blue_light_onSurfaceVariant,
    error = blue_light_error,
    onError = blue_light_onError
)

private val BlueDarkColorScheme = darkColorScheme(
    primary = blue_dark_primary,
    onPrimary = blue_dark_onPrimary,
    primaryContainer = blue_dark_primaryContainer,
    onPrimaryContainer = blue_dark_onPrimaryContainer,
    secondary = blue_dark_secondary,
    onSecondary = blue_dark_onSecondary,
    secondaryContainer = blue_dark_secondaryContainer,
    onSecondaryContainer = blue_dark_onSecondaryContainer,
    tertiary = blue_dark_tertiary,
    onTertiary = blue_dark_onTertiary,
    tertiaryContainer = blue_dark_tertiaryContainer,
    onTertiaryContainer = blue_dark_onTertiaryContainer,
    background = blue_dark_background,
    onBackground = blue_dark_onBackground,
    surface = blue_dark_surface,
    onSurface = blue_dark_onSurface,
    surfaceVariant = blue_dark_surfaceVariant,
    onSurfaceVariant = blue_dark_onSurfaceVariant,
    error = blue_dark_error,
    onError = blue_dark_onError
)

// Red Color Schemes
private val RedLightColorScheme = lightColorScheme(
    primary = red_light_primary,
    onPrimary = red_light_onPrimary,
    primaryContainer = red_light_primaryContainer,
    onPrimaryContainer = red_light_onPrimaryContainer,
    secondary = red_light_secondary,
    onSecondary = red_light_onSecondary,
    secondaryContainer = red_light_secondaryContainer,
    onSecondaryContainer = red_light_onSecondaryContainer,
    tertiary = red_light_tertiary,
    onTertiary = red_light_onTertiary,
    tertiaryContainer = red_light_tertiaryContainer,
    onTertiaryContainer = red_light_onTertiaryContainer,
    background = red_light_background,
    onBackground = red_light_onBackground,
    surface = red_light_surface,
    onSurface = red_light_onSurface,
    surfaceVariant = red_light_surfaceVariant,
    onSurfaceVariant = red_light_onSurfaceVariant,
    error = red_light_error,
    onError = red_light_onError
)

private val RedDarkColorScheme = darkColorScheme(
    primary = red_dark_primary,
    onPrimary = red_dark_onPrimary,
    primaryContainer = red_dark_primaryContainer,
    onPrimaryContainer = red_dark_onPrimaryContainer,
    secondary = red_dark_secondary,
    onSecondary = red_dark_onSecondary,
    secondaryContainer = red_dark_secondaryContainer,
    onSecondaryContainer = red_dark_onSecondaryContainer,
    tertiary = red_dark_tertiary,
    onTertiary = red_dark_onTertiary,
    tertiaryContainer = red_dark_tertiaryContainer,
    onTertiaryContainer = red_dark_onTertiaryContainer,
    background = red_dark_background,
    onBackground = red_dark_onBackground,
    surface = red_dark_surface,
    onSurface = red_dark_onSurface,
    surfaceVariant = red_dark_surfaceVariant,
    onSurfaceVariant = red_dark_onSurfaceVariant,
    error = red_dark_error,
    onError = red_dark_onError
)

// Green Color Schemes
private val GreenLightColorScheme = lightColorScheme(
    primary = green_light_primary,
    onPrimary = green_light_onPrimary,
    primaryContainer = green_light_primaryContainer,
    onPrimaryContainer = green_light_onPrimaryContainer,
    secondary = green_light_secondary,
    onSecondary = green_light_onSecondary,
    secondaryContainer = green_light_secondaryContainer,
    onSecondaryContainer = green_light_onSecondaryContainer,
    tertiary = green_light_tertiary,
    onTertiary = green_light_onTertiary,
    tertiaryContainer = green_light_tertiaryContainer,
    onTertiaryContainer = green_light_onTertiaryContainer,
    background = green_light_background,
    onBackground = green_light_onBackground,
    surface = green_light_surface,
    onSurface = green_light_onSurface,
    surfaceVariant = green_light_surfaceVariant,
    onSurfaceVariant = green_light_onSurfaceVariant,
    error = green_light_error,
    onError = green_light_onError
)

private val GreenDarkColorScheme = darkColorScheme(
    primary = green_dark_primary,
    onPrimary = green_dark_onPrimary,
    primaryContainer = green_dark_primaryContainer,
    onPrimaryContainer = green_dark_onPrimaryContainer,
    secondary = green_dark_secondary,
    onSecondary = green_dark_onSecondary,
    secondaryContainer = green_dark_secondaryContainer,
    onSecondaryContainer = green_dark_onSecondaryContainer,
    tertiary = green_dark_tertiary,
    onTertiary = green_dark_onTertiary,
    tertiaryContainer = green_dark_tertiaryContainer,
    onTertiaryContainer = green_dark_onTertiaryContainer,
    background = green_dark_background,
    onBackground = green_dark_onBackground,
    surface = green_dark_surface,
    onSurface = green_dark_onSurface,
    surfaceVariant = green_dark_surfaceVariant,
    onSurfaceVariant = green_dark_onSurfaceVariant,
    error = green_dark_error,
    onError = green_dark_onError
)

// Purple Color Schemes
private val PurpleLightColorScheme = lightColorScheme(
    primary = purple_light_primary,
    onPrimary = purple_light_onPrimary,
    primaryContainer = purple_light_primaryContainer,
    onPrimaryContainer = purple_light_onPrimaryContainer,
    secondary = purple_light_secondary,
    onSecondary = purple_light_onSecondary,
    secondaryContainer = purple_light_secondaryContainer,
    onSecondaryContainer = purple_light_onSecondaryContainer,
    tertiary = purple_light_tertiary,
    onTertiary = purple_light_onTertiary,
    tertiaryContainer = purple_light_tertiaryContainer,
    onTertiaryContainer = purple_light_onTertiaryContainer,
    background = purple_light_background,
    onBackground = purple_light_onBackground,
    surface = purple_light_surface,
    onSurface = purple_light_onSurface,
    surfaceVariant = purple_light_surfaceVariant,
    onSurfaceVariant = purple_light_onSurfaceVariant,
    error = purple_light_error,
    onError = purple_light_onError
)

private val PurpleDarkColorScheme = darkColorScheme(
    primary = purple_dark_primary,
    onPrimary = purple_dark_onPrimary,
    primaryContainer = purple_dark_primaryContainer,
    onPrimaryContainer = purple_dark_onPrimaryContainer,
    secondary = purple_dark_secondary,
    onSecondary = purple_dark_onSecondary,
    secondaryContainer = purple_dark_secondaryContainer,
    onSecondaryContainer = purple_dark_onSecondaryContainer,
    tertiary = purple_dark_tertiary,
    onTertiary = purple_dark_onTertiary,
    tertiaryContainer = purple_dark_tertiaryContainer,
    onTertiaryContainer = purple_dark_onTertiaryContainer,
    background = purple_dark_background,
    onBackground = purple_dark_onBackground,
    surface = purple_dark_surface,
    onSurface = purple_dark_onSurface,
    surfaceVariant = purple_dark_surfaceVariant,
    onSurfaceVariant = purple_dark_onSurfaceVariant,
    error = purple_dark_error,
    onError = purple_dark_onError
)

// Orange Color Schemes
private val OrangeLightColorScheme = lightColorScheme(
    primary = orange_light_primary,
    onPrimary = orange_light_onPrimary,
    primaryContainer = orange_light_primaryContainer,
    onPrimaryContainer = orange_light_onPrimaryContainer,
    secondary = orange_light_secondary,
    onSecondary = orange_light_onSecondary,
    secondaryContainer = orange_light_secondaryContainer,
    onSecondaryContainer = orange_light_onSecondaryContainer,
    tertiary = orange_light_tertiary,
    onTertiary = orange_light_onTertiary,
    tertiaryContainer = orange_light_tertiaryContainer,
    onTertiaryContainer = orange_light_onTertiaryContainer,
    background = orange_light_background,
    onBackground = orange_light_onBackground,
    surface = orange_light_surface,
    onSurface = orange_light_onSurface,
    surfaceVariant = orange_light_surfaceVariant,
    onSurfaceVariant = orange_light_onSurfaceVariant,
    error = orange_light_error,
    onError = orange_light_onError
)

private val OrangeDarkColorScheme = darkColorScheme(
    primary = orange_dark_primary,
    onPrimary = orange_dark_onPrimary,
    primaryContainer = orange_dark_primaryContainer,
    onPrimaryContainer = orange_dark_onPrimaryContainer,
    secondary = orange_dark_secondary,
    onSecondary = orange_dark_onSecondary,
    secondaryContainer = orange_dark_secondaryContainer,
    onSecondaryContainer = orange_dark_onSecondaryContainer,
    tertiary = orange_dark_tertiary,
    onTertiary = orange_dark_onTertiary,
    tertiaryContainer = orange_dark_tertiaryContainer,
    onTertiaryContainer = orange_dark_onTertiaryContainer,
    background = orange_dark_background,
    onBackground = orange_dark_onBackground,
    surface = orange_dark_surface,
    onSurface = orange_dark_onSurface,
    surfaceVariant = orange_dark_surfaceVariant,
    onSurfaceVariant = orange_dark_onSurfaceVariant,
    error = orange_dark_error,
    onError = orange_dark_onError
)

fun getColorScheme(appTheme: AppTheme, isDark: Boolean): ColorScheme {
    return when (appTheme) {
        AppTheme.BLUE -> if (isDark) BlueDarkColorScheme else BlueLightColorScheme
        AppTheme.RED -> if (isDark) RedDarkColorScheme else RedLightColorScheme
        AppTheme.GREEN -> if (isDark) GreenDarkColorScheme else GreenLightColorScheme
        AppTheme.PURPLE -> if (isDark) PurpleDarkColorScheme else PurpleLightColorScheme
        AppTheme.ORANGE -> if (isDark) OrangeDarkColorScheme else OrangeLightColorScheme
    }
}

@Composable
fun EthiopianCalendarTheme(
    appTheme: AppTheme = AppTheme.BLUE,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val systemInDarkTheme = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> systemInDarkTheme
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = getColorScheme(appTheme, darkTheme)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as Activity
            val window = activity.window

            WindowCompat.setDecorFitsSystemWindows(window, true)
            WindowInsetsControllerCompat(window, window.decorView).apply {
                isAppearanceLightStatusBars = !darkTheme
            }
            window.statusBarColor = colorScheme.primary.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
