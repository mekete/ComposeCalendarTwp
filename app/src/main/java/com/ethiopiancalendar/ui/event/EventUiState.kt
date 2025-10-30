package com.ethiopiancalendar.ui.event

import com.ethiopiancalendar.data.local.entity.EventEntity
import com.ethiopiancalendar.data.local.entity.EventInstance

/**
 * Sealed class representing the UI state for EventScreen.
 *
 * Follows the same pattern as HolidayListUiState:
 * - Loading: Initial state or when loading data
 * - Success: Data loaded successfully with list of events
 * - Error: Failed to load data with error message
 */
sealed class EventUiState {
    /**
     * Loading state - shown when fetching events from database.
     */
    data object Loading : EventUiState()

    /**
     * Success state - events loaded successfully.
     *
     * @param events List of event instances (includes recurring event instances)
     * @param isDialogOpen Whether the add event dialog is currently shown
     * @param editingEvent Event currently being edited (null if creating new event)
     */
    data class Success(
        val events: List<EventInstance>,
        val isDialogOpen: Boolean = false,
        val editingEvent: EventEntity? = null
    ) : EventUiState()

    /**
     * Error state - failed to load events.
     *
     * @param message Error message to display to user
     */
    data class Error(val message: String) : EventUiState()
}
