package com.ethiopiancalendar.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ethiopiancalendar.data.local.converter.DateConverter
import com.ethiopiancalendar.data.local.dao.EventDao
import com.ethiopiancalendar.data.local.entity.EventEntity

/**
 * Room database for the Calendar application.
 *
 * This database stores:
 * - Events and reminders
 * - (Future: Holiday customizations, user preferences, sync state)
 *
 * Version 1: Initial schema with EventEntity
 *
 * Migration strategy:
 * - For development: Use fallbackToDestructiveMigration()
 * - For production: Provide migration paths for schema changes
 */
@Database(
    entities = [
        EventEntity::class
    ],
    version = 1,
    exportSchema = true  // Enable schema export for version control
)
@TypeConverters(DateConverter::class)
abstract class CalendarDatabase : RoomDatabase() {

    /**
     * Provides access to EventDao for event operations.
     */
    abstract fun eventDao(): EventDao

    companion object {
        const val DATABASE_NAME = "calendar_database"
    }
}
