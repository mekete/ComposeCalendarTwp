package com.ethiopiancalendar.di

import android.content.Context
import androidx.room.Room
import com.ethiopiancalendar.data.local.CalendarDatabase
import com.ethiopiancalendar.data.local.dao.EventDao
import com.ethiopiancalendar.data.preferences.SettingsPreferences
import com.ethiopiancalendar.data.preferences.ThemePreferences
import com.ethiopiancalendar.data.repository.EventRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideThemePreferences(@ApplicationContext context: Context): ThemePreferences {
        return ThemePreferences(context)
    }

    @Provides
    @Singleton
    fun provideSettingsPreferences(@ApplicationContext context: Context): SettingsPreferences {
        return SettingsPreferences(context)
    }

    // ========== Room Database ==========

    @Provides
    @Singleton
    fun provideCalendarDatabase(@ApplicationContext context: Context): CalendarDatabase {
        return Room.databaseBuilder(
            context,
            CalendarDatabase::class.java,
            CalendarDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration(false) // For development - replace with migrations in production
            .build()
    }

    @Provides
    @Singleton
    fun provideEventDao(database: CalendarDatabase): EventDao {
        return database.eventDao()
    }

    @Provides
    @Singleton
    fun provideEventRepository(eventDao: EventDao): EventRepository {
        return EventRepository(eventDao)
    }
}
