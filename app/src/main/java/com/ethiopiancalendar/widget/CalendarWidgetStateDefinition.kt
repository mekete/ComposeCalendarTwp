package com.ethiopiancalendar.widget

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import androidx.datastore.dataStoreFile
import androidx.glance.state.GlanceStateDefinition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

/**
 * CalendarWidgetStateDefinition - Defines how widget state is stored and retrieved
 *
 * Stores:
 * - List of upcoming events for display in widget
 */
object CalendarWidgetStateDefinition : GlanceStateDefinition<CalendarWidgetState> {

    private const val DATA_STORE_FILENAME = "calendar_widget_state"
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    override suspend fun getDataStore(
        context: Context,
        fileKey: String
    ): DataStore<CalendarWidgetState> {
        return context.calendarWidgetStateDataStore
    }

    override fun getLocation(context: Context, fileKey: String): File {
        return context.dataStoreFile(DATA_STORE_FILENAME)
    }

    private val Context.calendarWidgetStateDataStore by dataStore(
        fileName = DATA_STORE_FILENAME,
        serializer = CalendarWidgetStateSerializer
    )
}

/**
 * CalendarWidgetState - State data for the widget
 */
@Serializable
data class CalendarWidgetState(
    val events: List<WidgetEvent> = emptyList()
)

/**
 * WidgetEvent - Simplified event model for widget display
 */
@Serializable
data class WidgetEvent(
    val id: String,
    val title: String,
    val startTime: Long,
    val endTime: Long?,
    val isAllDay: Boolean,
    val color: Int,
    val category: String
)

/**
 * Serializer for CalendarWidgetState using JSON
 */
object CalendarWidgetStateSerializer : Serializer<CalendarWidgetState> {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    override val defaultValue: CalendarWidgetState
        get() = CalendarWidgetState()

    override suspend fun readFrom(input: InputStream): CalendarWidgetState {
        return try {
            withContext(Dispatchers.IO) {
                val text = input.readBytes().decodeToString()
                if (text.isEmpty()) {
                    defaultValue
                } else {
                    json.decodeFromString(text)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw CorruptionException("Cannot read widget state", e)
        }
    }

    override suspend fun writeTo(t: CalendarWidgetState, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(
                json.encodeToString(t).encodeToByteArray()
            )
        }
    }
}
