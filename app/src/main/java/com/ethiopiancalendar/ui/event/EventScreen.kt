package com.ethiopiancalendar.ui.event

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ethiopiancalendar.R
import com.ethiopiancalendar.data.local.entity.EventInstance

@Composable
fun EventScreen(
    event: String,
    viewModel: EventViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddEventDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_event)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is EventUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is EventUiState.Success -> {
                    if (state.events.isEmpty()) {
                        EmptyEventsPlaceholder()
                    } else {
                        EventList(
                            events = state.events,
                            onEventClick = { /* TODO: Show event details */ },
                            onDeleteEvent = { viewModel.deleteEvent(it) }
                        )
                    }

                    // Show dialog if open
                    if (state.isDialogOpen) {
                        AddEventDialog(
                            onDismiss = { viewModel.hideAddEventDialog() },
                            onCreateEvent = { summary, description, startTime, endTime, isAllDay, recurrenceRule, reminderMinutes, category, ethYear, ethMonth, ethDay ->
                                viewModel.createEvent(
                                    summary = summary,
                                    description = description,
                                    startTime = startTime,
                                    endTime = endTime,
                                    isAllDay = isAllDay,
                                    recurrenceRule = recurrenceRule,
                                    reminderMinutesBefore = reminderMinutes,
                                    category = category,
                                    ethiopianYear = ethYear,
                                    ethiopianMonth = ethMonth,
                                    ethiopianDay = ethDay
                                )
                            }
                        )
                    }
                }

                is EventUiState.Error -> {
                    ErrorMessage(
                        message = state.message,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
private fun EventList(
    events: List<EventInstance>,
    onEventClick: (String) -> Unit,
    onDeleteEvent: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = events,
            key = { "${it.eventId}_${it.instanceStart.toInstant().toEpochMilli()}" }
        ) { event ->
            EventCard(
                event = event,
                onClick = { onEventClick(event.eventId) },
                onDelete = { onDeleteEvent(event.eventId) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventCard(
    event: EventInstance,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Event color indicator
            Box(
                modifier = Modifier
                    .size(4.dp, 60.dp)
                    .background(
                        color = Color(event.color),
                        shape = RoundedCornerShape(2.dp)
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Event details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = event.summary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Date and time
                val dateTimeText = if (event.isAllDay) {
                    stringResource(
                        R.string.event_date_all_day,
                        event.ethiopianDay,
                        event.ethiopianMonth,
                        event.ethiopianYear
                    )
                } else {
                    val timeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
                    val startTime = event.instanceStart.format(timeFormatter)
                    val endTime = event.instanceEnd?.format(timeFormatter) ?: ""
                    if (endTime.isNotEmpty()) {
                        "$startTime - $endTime"
                    } else {
                        startTime
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = dateTimeText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Description
                event.description?.let { desc ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Recurring indicator
                if (event.isRecurring) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.recurring_event),
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.recurring),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Reminder indicator
                event.reminderMinutesBefore?.let { minutes ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.reminder_minutes_before, minutes),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Delete button
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete_event),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_event_title)) },
            text = { Text(stringResource(R.string.delete_event_message, event.summary)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun EmptyEventsPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.no_events),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.tap_plus_to_add_event),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.error_loading_events),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
