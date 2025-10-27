package com.ethiopiancalendar

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class CalendarApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize ThreeTenBP for Ethiopian calendar
        AndroidThreeTen.init(this)
        
////         Initialize Timber for logging
//        if (BuildConfig.DEBUG) {
//            Timber.plant(Timber.DebugTree())
//        }
        
        Timber.d("Ethiopian Calendar App started")
    }
}
