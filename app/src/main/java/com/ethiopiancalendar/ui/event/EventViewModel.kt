package com.ethiopiancalendar.ui.event

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ethiopiancalendar.alarm.AlarmScheduler
import com.ethiopiancalendar.data.local.entity.EventEntity
import com.ethiopiancalendar.data.local.entity.RecurrenceRule
import com.ethiopiancalendar.data.local.entity.toRRuleString
import com.ethiopiancalendar.data.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.ZonedDateTime
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for EventScreen.
 *
 * Follows the same pattern as HolidayListViewModel:
 * - Uses @HiltViewModel for dependency injection
 * - Exposes StateFlow for UI state
 * - Handles business logic and repository operations
 * - Uses viewModelScope for coroutines
 * - Integrates alarm scheduling for event reminders
 */
@HiltViewModel
class EventViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val alarmScheduler = AlarmScheduler(context)

    private val _uiState = MutableStateFlow<EventUiState>(EventUiState.Loading)
    val uiState: StateFlow<EventUiState> = _uiState.asStateFlow()

    init {
        loadEvents()
    }

    /**
     * Load all events from repository.
     * Uses Flow to automatically update UI when data changes.
     */
    private fun loadEvents() {
        viewModelScope.launch {
            eventRepository.getAllEvents()
                .catch { exception ->
                    Timber.e(exception, "Error loading events")
                    _uiState.value = EventUiState.Error(
                        message = exception.message ?: "Failed to load events"
                    )
                }
                .collect { events ->
                    // Convert EventEntity to EventInstance
                    val instances = events.map { event ->
                        event.toEventInstance()
                    }
                    _uiState.value = EventUiState.Success(
                        events = instances,
                        isDialogOpen = (_uiState.value as? EventUiState.Success)?.isDialogOpen ?: false
                    )
                }
        }
    }

    /**
     * Show the add event dialog.
     */
    fun showAddEventDialog() {
        val currentState = _uiState.value
        if (currentState is EventUiState.Success) {
            _uiState.value = currentState.copy(isDialogOpen = true)
        }
    }

    /**
     * Hide the add event dialog.
     */
    fun hideAddEventDialog() {
        val currentState = _uiState.value
        if (currentState is EventUiState.Success) {
            _uiState.value = currentState.copy(isDialogOpen = false)
        }
    }

    /**
     * Create a new event.
     *
     * @param summary Event title
     * @param description Event description (optional)
     * @param startTime Start date/time with timezone
     * @param endTime End date/time with timezone (optional)
     * @param isAllDay Whether the event is all-day
     * @param recurrenceRule Recurrence pattern (optional)
     * @param reminderMinutesBefore Minutes before event to remind (optional)
     * @param category Event category
     * @param ethiopianYear Ethiopian calendar year
     * @param ethiopianMonth Ethiopian calendar month
     * @param ethiopianDay Ethiopian calendar day
     */
    fun createEvent(
        summary: String,
        description: String? = null,
        startTime: ZonedDateTime,
        endTime: ZonedDateTime? = null,
        isAllDay: Boolean = false,
        recurrenceRule: RecurrenceRule? = null,
        reminderMinutesBefore: Int? = null,
        category: String = "PERSONAL",
        ethiopianYear: Int,
        ethiopianMonth: Int,
        ethiopianDay: Int
    ) {
        viewModelScope.launch {
            try {
                val event = EventEntity(
                    id = UUID.randomUUID().toString(),
                    summary = summary.trim(),
                    description = description?.trim(),
                    startTime = startTime,
                    endTime = endTime,
                    isAllDay = isAllDay,
                    timeZone = startTime.zone.id,
                    recurrenceRule = recurrenceRule?.toRRuleString(),
                    recurrenceEndDate = recurrenceRule?.endDate?.let {
                        ZonedDateTime.ofInstant(
                            java.time.Instant.ofEpochMilli(it),
                            startTime.zone
                        )
                    },
                    reminderMinutesBefore = reminderMinutesBefore,
                    category = category,
                    ethiopianYear = ethiopianYear,
                    ethiopianMonth = ethiopianMonth,
                    ethiopianDay = ethiopianDay,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                // Create event in database
                eventRepository.createEvent(event)

                // Schedule alarm if reminder is enabled
                if (reminderMinutesBefore != null) {
                    val scheduled = alarmScheduler.scheduleAlarm(event)
                    if (scheduled) {
                        Timber.d("Alarm scheduled for event: $summary")
                    } else {
                        Timber.w("Failed to schedule alarm for event: $summary")
                    }
                }

                hideAddEventDialog()
                Timber.d("Event created: $summary")
            } catch (e: Exception) {
                Timber.e(e, "Error creating event")
                _uiState.value = EventUiState.Error(
                    message = e.message ?: "Failed to create event"
                )
            }
        }
    }

    /**
     * Delete an event by ID.
     * Also cancels any scheduled alarms for the event.
     */
    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            try {
                // Cancel alarm if it exists
                alarmScheduler.cancelAlarm(eventId)

                // Delete event from database
                eventRepository.deleteEventById(eventId)

                Timber.d("Event deleted: $eventId")
            } catch (e: Exception) {
                Timber.e(e, "Error deleting event")
                _uiState.value = EventUiState.Error(
                    message = e.message ?: "Failed to delete event"
                )
            }
        }
    }

    /**
     * Helper function to convert EventEntity to EventInstance.
     */
    private fun EventEntity.toEventInstance() = com.ethiopiancalendar.data.local.entity.EventInstance(
        eventId = id,
        summary = summary,
        description = description,
        instanceStart = startTime,
        instanceEnd = endTime,
        isAllDay = isAllDay,
        category = category,
        color = color,
        reminderMinutesBefore = reminderMinutesBefore,
        ethiopianYear = ethiopianYear,
        ethiopianMonth = ethiopianMonth,
        ethiopianDay = ethiopianDay,
        isRecurring = recurrenceRule != null,
        originalEvent = this
    )
}
