package com.ethiopiancalendar

import android.app.Application
import com.ethiopiancalendar.alarm.NotificationHelper
import com.ethiopiancalendar.data.initialization.AppInitializationManager
import com.ethiopiancalendar.data.initialization.ReminderReregistrationManager
import com.ethiopiancalendar.data.remote.RemoteConfigManager
import com.ethiopiancalendar.widget.CalendarWidgetWorker
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class CalendarApplication : Application() {

    @Inject
    lateinit var remoteConfigManager: RemoteConfigManager

    @Inject
    lateinit var appInitializationManager: AppInitializationManager

    @Inject
    lateinit var reminderReregistrationManager: ReminderReregistrationManager

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging
        if (com.ethiopiancalendar.BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Timber.d("Ethiopian Calendar App starting...")

        // Create notification channels for event reminders
        NotificationHelper.createNotificationChannels(this)

        // Schedule periodic widget updates
        CalendarWidgetWorker.schedule(this)

        // 1.4 Initialize Firebase Remote Config for Muslim holiday offsets
        // This is called in AppInitializationManager but we keep it here for immediate config
        remoteConfigManager.initialize(isDebug = com.ethiopiancalendar.BuildConfig.DEBUG)

        // Execute comprehensive app initialization:
        // 1.1 Locale & Chronology Initialization
        // 1.2 First-Time Setup (if needed)
        // 1.3 Version-Upgrade Setup (if needed)
        // 1.4 Firebase Remote Config (refresh)
        // 1.6 Analytics and Usage Logging
        appInitializationManager.initialize()

        // 1.5 Re-register event reminders
        // Ensures alarms survive app updates and device reboots
        reminderReregistrationManager.reregisterReminders()

        Timber.d("Ethiopian Calendar App started successfully")
    }
}
