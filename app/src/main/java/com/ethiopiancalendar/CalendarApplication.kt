package com.ethiopiancalendar

import android.app.Application
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
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

        // Trigger immediate widget update on app start to ensure widget has fresh data
        Log.d("CalendarApplication", "Triggering immediate widget update on app start")
        val immediateUpdate = OneTimeWorkRequestBuilder<CalendarWidgetWorker>().build()
        WorkManager.getInstance(this).enqueue(immediateUpdate)

        Timber.d("Ethiopian Calendar App started")
    }
}
