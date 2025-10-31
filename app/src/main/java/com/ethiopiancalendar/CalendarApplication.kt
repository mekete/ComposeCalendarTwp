package com.ethiopiancalendar

import android.app.Application
import com.ethiopiancalendar.alarm.NotificationHelper
import com.ethiopiancalendar.widget.CalendarWidgetWorker
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class CalendarApplication : Application() {

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

        Timber.d("Ethiopian Calendar App started")
    }
}
