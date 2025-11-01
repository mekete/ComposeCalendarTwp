package com.ethiopiancalendar

import android.app.Application
import com.ethiopiancalendar.alarm.NotificationHelper
import com.ethiopiancalendar.data.remote.RemoteConfigManager
import com.ethiopiancalendar.widget.CalendarWidgetWorker
import com.google.firebase.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class CalendarApplication : Application() {

    @Inject
    lateinit var remoteConfigManager: RemoteConfigManager

    override fun onCreate() {
        super.onCreate()

        // Initialize ThreeTenBP for Ethiopian calendar

        ////         Initialize Timber for logging
        //        if (BuildConfig.DEBUG) {
        //            Timber.plant(Timber.DebugTree())
        //        }

        // Create notification channels for event reminders
        NotificationHelper.createNotificationChannels(this)

        // Schedule periodic widget updates
        CalendarWidgetWorker.schedule(this)

        // Initialize Firebase Remote Config for Muslim holiday offsets
        remoteConfigManager.initialize(isDebug = BuildConfig.DEBUG)

        Timber.d("Ethiopian Calendar App started")
    }
}
