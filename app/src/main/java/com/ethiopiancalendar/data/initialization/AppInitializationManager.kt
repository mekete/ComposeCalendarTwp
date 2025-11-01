package com.ethiopiancalendar.data.initialization

import android.content.Context
import android.os.Build
import android.os.Bundle
import com.ethiopiancalendar.BuildConfig
import com.ethiopiancalendar.data.local.CalendarDatabase
import com.ethiopiancalendar.data.preferences.CalendarType
import com.ethiopiancalendar.data.preferences.SettingsPreferences
import com.ethiopiancalendar.data.remote.RemoteConfigManager
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import com.google.firebase.analytics.analytics

/**
 * Manages app initialization logic on launch.
 * Handles first-time setup, version upgrades, locale configuration,
 * Firebase services initialization, and analytics logging.
 */
@Singleton
class AppInitializationManager @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val settingsPreferences: SettingsPreferences,
    private val remoteConfigManager: RemoteConfigManager,
    private val database: CalendarDatabase
) {
    private val firebaseAnalytics: FirebaseAnalytics by lazy { Firebase.analytics }
    private val applicationScope = CoroutineScope(Dispatchers.IO)

    /**
     * Main initialization entry point.
     * Should be called from Application.onCreate()
     */
    fun initialize() {
        applicationScope.launch {
            try {
                Timber.d("Starting app initialization...")

                // 1. Get current version info
                val storedVersionCode = settingsPreferences.versionCode.first()
                val currentVersionCode = BuildConfig.VERSION_CODE
                val currentVersionName = BuildConfig.VERSION_NAME

                Timber.d("Stored version: $storedVersionCode, Current version: $currentVersionCode")

                // 2. Determine if first run or upgrade
                when {
                    storedVersionCode == -1 -> {
                        Timber.d("First-time setup detected")
                        handleFirstTimeSetup(currentVersionCode, currentVersionName)
                    }
                    storedVersionCode < currentVersionCode -> {
                        Timber.d("Version upgrade detected: $storedVersionCode -> $currentVersionCode")
                        handleVersionUpgrade(storedVersionCode, currentVersionCode, currentVersionName)
                    }
                    else -> {
                        Timber.d("Normal app launch")
                        handleNormalLaunch()
                    }
                }

                // 3. Initialize locale and chronology
                initializeLocaleSettings()

                // 4. Update last used timestamp
                settingsPreferences.setLastUsedTimestamp(System.currentTimeMillis())

                // 5. Log launch event
                logLaunchEvent()

                Timber.d("App initialization completed successfully")
            } catch (e: Exception) {
                Timber.e(e, "Error during app initialization")
                // Continue with cached data - don't block app launch
            }
        }
    }

    /**
     * 1.2 First-Time Setup
     */
    private suspend fun handleFirstTimeSetup(versionCode: Int, versionName: String) {
        try {
            Timber.d("Initializing first-time setup...")

            // 1.2.1 Initialize default calendar preferences
            settingsPreferences.setPrimaryCalendar(CalendarType.ETHIOPIAN)
            settingsPreferences.setSecondaryCalendar(CalendarType.GREGOREAN)
            settingsPreferences.setDisplayDualCalendar(true)
            Timber.d("Default calendar preferences set")

            // 1.2.2 Store version info
            settingsPreferences.setVersionCode(versionCode)
            settingsPreferences.setVersionName(versionName)
            settingsPreferences.setIsFirstRun(false)
            Timber.d("Version info stored: $versionCode ($versionName)")

            // 1.2.3 Fetch and store Firebase Installation ID
            try {
                val installationId = FirebaseInstallations.getInstance().id.await()
                settingsPreferences.setFirebaseInstallationId(installationId)
                Timber.d("Firebase Installation ID stored: $installationId")
            } catch (e: Exception) {
                Timber.e(e, "Failed to fetch Firebase Installation ID")
            }

            // 1.2.4 Subscribe to base Firebase Messaging topics
            subscribeToFCMTopics(listOf("general", "holiday-updates"))

            // Log first install event
            firebaseAnalytics.logEvent("app_first_install", Bundle().apply {
                putInt("user_type",  versionCode)
                putString("screen_count", versionName)
            })

            Timber.d("First-time setup completed")
        } catch (e: Exception) {
            Timber.e(e, "Error during first-time setup")
        }
    }

    /**
     * 1.3 Version-Upgrade Setup
     */
    private suspend fun handleVersionUpgrade(
        oldVersionCode: Int,
        newVersionCode: Int,
        newVersionName: String
    ) {
        try {
            Timber.d("Handling version upgrade from $oldVersionCode to $newVersionCode")

            // 1.3.1 Run migration logic
            runMigrations(oldVersionCode, newVersionCode)

            // 1.3.2 Check if upgrading from version without Muslim holidays support
            if (oldVersionCode < getVersionCodeWhenMuslimHolidaysAdded()) {
                handleMuslimHolidaysMigration()
            }

            // 1.3.3 Re-subscribe to Firebase topics (in case new topics were added)
            subscribeToFCMTopics(listOf("general", "holiday-updates"))

            // 1.3.4 Update stored version code
            settingsPreferences.setVersionCode(newVersionCode)
            settingsPreferences.setVersionName(newVersionName)

            // Log upgrade event
            firebaseAnalytics.logEvent("app_upgraded", Bundle().apply {
                putLong("old_version_code", oldVersionCode.toLong())
                putLong("new_version_code", newVersionCode.toLong())
                putString("new_version_name", newVersionName)
            })

            Timber.d("Version upgrade completed")
        } catch (e: Exception) {
            Timber.e(e, "Error during version upgrade")
        }
    }

    /**
     * Handle normal app launch (not first run or upgrade)
     */
    private suspend fun handleNormalLaunch() {
        try {
            Timber.d("Handling normal app launch")

            // Refresh Firebase Remote Config in background
            applicationScope.launch {
                try {
                    remoteConfigManager.initialize(BuildConfig.DEBUG)
                } catch (e: Exception) {
                    Timber.e(e, "Failed to refresh Remote Config on normal launch")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error during normal launch")
        }
    }

    /**
     * 1.1 Locale & Chronology Initialization
     */
    private suspend fun initializeLocaleSettings() {
        try {
            Timber.d("Initializing locale settings...")

            val storedPrimaryLocale = settingsPreferences.primaryLocale.first()
            val storedSecondaryLocale = settingsPreferences.secondaryLocale.first()

            // If no preference exists (first run)
            if (storedPrimaryLocale.isEmpty() || storedSecondaryLocale.isEmpty()) {
                val deviceLocale = getDeviceLocale()
                val countryCode = getDeviceCountryCode()

                Timber.d("Device locale: $deviceLocale, Country: $countryCode")

                // 1.1.2 Set locales based on device country
                val (primaryLocale, secondaryLocale) = when {
                    isDeviceInEthiopia(countryCode) -> {
                        // User is in Ethiopia
                        Pair("Africa/Addis_Ababa", "America/New_York")
                    }
                    else -> {
                        // User is NOT in Ethiopia
                        Pair("Africa/Addis_Ababa", deviceLocale)
                    }
                }

                settingsPreferences.setPrimaryLocale(primaryLocale)
                settingsPreferences.setSecondaryLocale(secondaryLocale)
                settingsPreferences.setDeviceCountryCode(countryCode)

                Timber.d("Locale preferences set: Primary=$primaryLocale, Secondary=$secondaryLocale")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error initializing locale settings")
        }
    }

    /**
     * 1.3.2 Handle Muslim holidays migration for Arabic-speaking countries
     */
    private suspend fun handleMuslimHolidaysMigration() {
        try {
            val countryCode = settingsPreferences.deviceCountryCode.first()

            if (isArabicSpeakingCountry(countryCode)) {
                // Enable Muslim holidays toggle but keep it hidden
                settingsPreferences.setShowMuslimHolidays(false)
                Timber.d("Muslim holidays enabled for Arabic-speaking country: $countryCode")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error during Muslim holidays migration")
        }
    }

    /**
     * 1.3.1 Run database and preference migrations
     */
    private suspend fun runMigrations(oldVersionCode: Int, newVersionCode: Int) {
        try {
            Timber.d("Running migrations from version $oldVersionCode to $newVersionCode")

            // Add specific migrations as needed when schema changes
            // Example:
            // if (oldVersionCode < 2) {
            //     // Migration for version 2
            // }

            // Database migrations are handled by Room Migration objects
            // This function is for preference/settings migrations

            Timber.d("Migrations completed")
        } catch (e: Exception) {
            Timber.e(e, "Error running migrations")
        }
    }

    /**
     * 1.2.4 Subscribe to Firebase Cloud Messaging topics
     */
    private fun subscribeToFCMTopics(topics: List<String>) {
        applicationScope.launch {
            try {
                val messaging = FirebaseMessaging.getInstance()
                topics.forEach { topic ->
                    messaging.subscribeToTopic(topic).await()
                    Timber.d("Subscribed to FCM topic: $topic")
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to subscribe to FCM topics")
            }
        }
    }

    /**
     * 1.6 Analytics and Usage Logging
     */
    private suspend fun logLaunchEvent() {
        try {
            val primaryCalendar = settingsPreferences.primaryCalendar.first()
            val secondaryCalendar = settingsPreferences.secondaryCalendar.first()
            val country = settingsPreferences.deviceCountryCode.first()
            val deviceLocale = getDeviceLocale()

            firebaseAnalytics.logEvent("app_launch", Bundle().apply {
                putString("app_version", BuildConfig.VERSION_NAME)
                putLong("version_code", BuildConfig.VERSION_CODE.toLong())
//                param("device_locale", deviceLocale)
//                param("primary_calendar", primaryCalendar.name)
//                param("secondary_calendar", secondaryCalendar.name)
//                param("country", country)
                putLong("android_version", Build.VERSION.SDK_INT.toLong())
            })

            Timber.d("Launch event logged to Firebase Analytics")
        } catch (e: Exception) {
            Timber.e(e, "Failed to log launch event")
        }
    }

    // Helper functions

    private fun getDeviceLocale(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0].toString()
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale.toString()
        }
    }

    private fun getDeviceCountryCode(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0].country
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale.country
        }
    }

    private fun isDeviceInEthiopia(countryCode: String): Boolean {
        return countryCode.equals("ET", ignoreCase = true)
    }

    private fun isArabicSpeakingCountry(countryCode: String): Boolean {
        val arabicCountries = setOf(
            "SA", "EG", "DZ", "SD", "IQ", "MA", "YE", "SY", "TN", "JO",
            "AE", "LB", "LY", "OM", "KW", "MR", "QA", "BH", "DJ", "SO"
        )
        return arabicCountries.contains(countryCode.uppercase())
    }

    private fun getVersionCodeWhenMuslimHolidaysAdded(): Int {
        // Return the version code when Muslim holidays feature was added
        // This can be updated when the feature is actually added
        return 1 // Placeholder - update with actual version
    }
}
