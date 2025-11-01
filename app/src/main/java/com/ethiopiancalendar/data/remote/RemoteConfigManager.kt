package com.ethiopiancalendar.data.remote

import com.ethiopiancalendar.R
import com.ethiopiancalendar.data.preferences.SettingsPreferences
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages Firebase Remote Config for Muslim holiday date offsets.
 *
 * This manager handles:
 * - Initialization of Firebase Remote Config
 * - Fetching and activating remote config values
 * - Syncing remote config values to local preferences
 *
 * Remote Config Keys:
 * - config_day_offset_eid_al_adha: Day offset for Eid al-Adha
 * - config_day_offset_eid_al_fitir: Day offset for Eid al-Fitr
 * - config_day_offset_mewlid: Day offset for Mawlid al-Nabi
 * - config_day_offset_ethio_year: Ethiopian year for which the offsets apply
 */
@Singleton
class RemoteConfigManager @Inject constructor(
    private val settingsPreferences: SettingsPreferences
) {
    private val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    companion object {
        // Remote Config Keys
        const val KEY_DAY_OFFSET_EID_AL_ADHA = "config_day_offset_eid_al_adha"
        const val KEY_DAY_OFFSET_EID_AL_FITR = "config_day_offset_eid_al_fitir"
        const val KEY_DAY_OFFSET_MAWLID = "config_day_offset_mewlid"
        const val KEY_DAY_OFFSET_ETHIO_YEAR = "config_day_offset_ethio_year"

        // Cache expiration time (in seconds)
        private const val CACHE_EXPIRATION_DEBUG = 60L // 1 minute for debug
        private const val CACHE_EXPIRATION_RELEASE = 6 * 3600L // 6 hours for release
    }

    /**
     * Initialize Firebase Remote Config with default values and fetch settings
     */
    fun initialize(isDebug: Boolean = false) {
        Timber.d("Initializing RemoteConfigManager")

        // Configure Remote Config settings
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(
                if (isDebug) CACHE_EXPIRATION_DEBUG else CACHE_EXPIRATION_RELEASE
            )
            .build()

        remoteConfig.setConfigSettingsAsync(configSettings)

        // Set default values from XML resource
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        // Fetch and activate config values
        fetchAndActivate()
    }

    /**
     * Fetch latest config values from Firebase and activate them
     */
    fun fetchAndActivate() {
        coroutineScope.launch {
            try {
                Timber.d("Fetching Remote Config values")
                val updated = remoteConfig.fetchAndActivate().await()

                if (updated) {
                    Timber.d("Remote Config values updated")
                    syncToPreferences()
                } else {
                    Timber.d("Remote Config values already up to date")
                    // Still sync to ensure preferences are up to date
                    syncToPreferences()
                }
            } catch (e: Exception) {
                Timber.e(e, "Error fetching Remote Config")
            }
        }
    }

    /**
     * Sync Remote Config values to local preferences
     */
    private suspend fun syncToPreferences() {
        try {
            Timber.d("Syncing Remote Config to preferences")

            val eidAlAdhaOffset = remoteConfig.getLong(KEY_DAY_OFFSET_EID_AL_ADHA).toInt()
            val eidAlFitrOffset = remoteConfig.getLong(KEY_DAY_OFFSET_EID_AL_FITR).toInt()
            val mawlidOffset = remoteConfig.getLong(KEY_DAY_OFFSET_MAWLID).toInt()
            val ethioYear = remoteConfig.getLong(KEY_DAY_OFFSET_ETHIO_YEAR).toInt()

            settingsPreferences.setDayOffsetEidAlAdha(eidAlAdhaOffset)
            settingsPreferences.setDayOffsetEidAlFitr(eidAlFitrOffset)
            settingsPreferences.setDayOffsetMawlid(mawlidOffset)
            settingsPreferences.setDayOffsetEthioYear(ethioYear)

            Timber.d(
                "Synced offsets - Year: $ethioYear, " +
                "Eid al-Adha: $eidAlAdhaOffset, " +
                "Eid al-Fitr: $eidAlFitrOffset, " +
                "Mawlid: $mawlidOffset"
            )
        } catch (e: Exception) {
            Timber.e(e, "Error syncing Remote Config to preferences")
        }
    }

    /**
     * Get current value from Remote Config (without fetching)
     */
    fun getEidAlAdhaOffset(): Int = remoteConfig.getLong(KEY_DAY_OFFSET_EID_AL_ADHA).toInt()
    fun getEidAlFitrOffset(): Int = remoteConfig.getLong(KEY_DAY_OFFSET_EID_AL_FITR).toInt()
    fun getMawlidOffset(): Int = remoteConfig.getLong(KEY_DAY_OFFSET_MAWLID).toInt()
    fun getEthioYear(): Int = remoteConfig.getLong(KEY_DAY_OFFSET_ETHIO_YEAR).toInt()
}
