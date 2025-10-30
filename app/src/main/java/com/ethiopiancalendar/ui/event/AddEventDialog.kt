package com.ethiopiancalendar.ui.event

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ethiopiancalendar.R
import com.ethiopiancalendar.data.local.entity.EventEntity
import com.ethiopiancalendar.data.local.entity.RecurrenceEndOption
import com.ethiopiancalendar.data.local.entity.RecurrenceFrequency
import com.ethiopiancalendar.data.local.entity.RecurrenceRule
import com.ethiopiancalendar.data.local.entity.parseRRule
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(
    onDismiss: () -> Unit,
    editingEvent: EventEntity? = null,
    onCreateEvent: (
        summary: String,
        description: String?,
        startTime: ZonedDateTime,
        endTime: ZonedDateTime?,
        isAllDay: Boolean,
        recurrenceRule: RecurrenceRule?,
        reminderMinutesBefore: Int?,
        category: String,
        ethiopianYear: Int,
        ethiopianMonth: Int,
        ethiopianDay: Int
    ) -> Unit,
    onUpdateEvent: (
        eventId: String,
        summary: String,
        description: String?,
        startTime: ZonedDateTime,
        endTime: ZonedDateTime?,
        isAllDay: Boolean,
        recurrenceRule: RecurrenceRule?,
        reminderMinutesBefore: Int?,
        category: String,
        ethiopianYear: Int,
        ethiopianMonth: Int,
        ethiopianDay: Int
    ) -> Unit
) {
    val isEditMode = editingEvent != null

    // Parse recurrence rule if editing
    val parsedRecurrenceRule = editingEvent?.recurrenceRule?.parseRRule()

    // Form state - pre-filled with editingEvent data if in edit mode
    var title by remember { mutableStateOf(editingEvent?.summary ?: "") }
    var description by remember { mutableStateOf(editingEvent?.description ?: "") }
    var selectedDate by remember {
        mutableStateOf(
            editingEvent?.startTime?.toLocalDate() ?: LocalDate.now()
        )
    }
    var startTime by remember {
        mutableStateOf(
            editingEvent?.startTime?.toLocalTime() ?: LocalTime.of(9, 0)
        )
    }
    var endTime by remember {
        mutableStateOf(
            editingEvent?.endTime?.toLocalTime() ?: LocalTime.of(10, 0)
        )
    }
    var isAllDay by remember { mutableStateOf(editingEvent?.isAllDay ?: false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    // Recurrence state - pre-filled if editing
    var recurrenceFrequency by remember {
        mutableStateOf(parsedRecurrenceRule?.frequency ?: RecurrenceFrequency.NONE)
    }
    var selectedWeekDays by remember {
        mutableStateOf(parsedRecurrenceRule?.weekDays ?: setOf<DayOfWeek>())
    }
    var recurrenceEndOption by remember {
        mutableStateOf(parsedRecurrenceRule?.endOption ?: RecurrenceEndOption.NEVER)
    }
    var recurrenceEndDate by remember {
        mutableStateOf(
            parsedRecurrenceRule?.endDate?.let {
                java.time.Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
            }
        )
    }
    var showRecurrenceEndDatePicker by remember { mutableStateOf(false) }

    // Reminder state - pre-filled if editing
    var reminderEnabled by remember { mutableStateOf(editingEvent?.reminderMinutesBefore != null) }
    var reminderMinutes by remember { mutableStateOf(editingEvent?.reminderMinutesBefore ?: 30) }

    // Category state - pre-filled if editing
    var category by remember { mutableStateOf(editingEvent?.category ?: "PERSONAL") }

    // Error state
    var titleError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Text(
                    text = if (isEditMode) stringResource(R.string.edit_event) else stringResource(R.string.add_event),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Title input
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = false
                    },
                    label = { Text(stringResource(R.string.title)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = titleError,
                    supportingText = if (titleError) {
                        { Text(stringResource(R.string.title_required)) }
                    } else null
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.description_optional)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Date picker
                OutlinedCard(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.date),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = selectedDate.toString(),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // All-day toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isAllDay = !isAllDay }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.all_day_event),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = isAllDay,
                        onCheckedChange = { isAllDay = it }
                    )
                }

                // Time pickers (only if not all-day)
                if (!isAllDay) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Start time
                        OutlinedCard(
                            onClick = { showStartTimePicker = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = stringResource(R.string.start_time),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    Text(
                                        text = startTime.toString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // End time
                        OutlinedCard(
                            onClick = { showEndTimePicker = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = stringResource(R.string.end_time),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    Text(
                                        text = endTime?.toString() ?: "—",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Recurrence section
                RecurrenceSection(
                    recurrenceFrequency = recurrenceFrequency,
                    onRecurrenceFrequencyChange = { recurrenceFrequency = it },
                    selectedWeekDays = selectedWeekDays,
                    onWeekDaysChange = { selectedWeekDays = it },
                    recurrenceEndOption = recurrenceEndOption,
                    onRecurrenceEndOptionChange = { recurrenceEndOption = it },
                    recurrenceEndDate = recurrenceEndDate,
                    onRecurrenceEndDateClick = { showRecurrenceEndDatePicker = true }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Reminder section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { reminderEnabled = !reminderEnabled }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.reminder),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { reminderEnabled = it }
                    )
                }

                if (reminderEnabled) {
                    ReminderOptions(
                        selectedMinutes = reminderMinutes,
                        onMinutesChange = { reminderMinutes = it }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Category selection
                CategorySelector(
                    selectedCategory = category,
                    onCategoryChange = { category = it }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                titleError = true
                                return@Button
                            }

                            // Build ZonedDateTime
                            val zoneId = ZoneId.systemDefault()
                            val startDateTime = if (isAllDay) {
                                ZonedDateTime.of(selectedDate, LocalTime.MIDNIGHT, zoneId)
                            } else {
                                ZonedDateTime.of(selectedDate, startTime, zoneId)
                            }

                            val endDateTime = if (isAllDay || endTime == null) {
                                null
                            } else {
                                ZonedDateTime.of(selectedDate, endTime, zoneId)
                            }

                            // Build recurrence rule
                            val recurrenceRule = if (recurrenceFrequency != RecurrenceFrequency.NONE) {
                                RecurrenceRule(
                                    frequency = recurrenceFrequency,
                                    weekDays = if (recurrenceFrequency == RecurrenceFrequency.WEEKLY) selectedWeekDays else emptySet(),
                                    endOption = recurrenceEndOption,
                                    endDate = recurrenceEndDate?.atStartOfDay(zoneId)?.toInstant()?.toEpochMilli()
                                )
                            } else null

                            // Use existing Ethiopian date if editing, otherwise use placeholder
                            val ethiopianYear = editingEvent?.ethiopianYear ?: 2017
                            val ethiopianMonth = editingEvent?.ethiopianMonth ?: 1
                            val ethiopianDay = editingEvent?.ethiopianDay ?: 1

                            if (isEditMode && editingEvent != null) {
                                // Update existing event
                                onUpdateEvent(
                                    editingEvent.id,
                                    title.trim(),
                                    description.ifBlank { null },
                                    startDateTime,
                                    endDateTime,
                                    isAllDay,
                                    recurrenceRule,
                                    if (reminderEnabled) reminderMinutes else null,
                                    category,
                                    ethiopianYear,
                                    ethiopianMonth,
                                    ethiopianDay
                                )
                            } else {
                                // Create new event
                                onCreateEvent(
                                    title.trim(),
                                    description.ifBlank { null },
                                    startDateTime,
                                    endDateTime,
                                    isAllDay,
                                    recurrenceRule,
                                    if (reminderEnabled) reminderMinutes else null,
                                    category,
                                    ethiopianYear,
                                    ethiopianMonth,
                                    ethiopianDay
                                )
                            }
                        }
                    ) {
                        Text(if (isEditMode) stringResource(R.string.update) else stringResource(R.string.create))
                    }
                }
            }
        }
    }

    // Date picker dialogs
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time pickers
    if (showStartTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = startTime.hour,
            initialMinute = startTime.minute,
            is24Hour = true
        )
        TimePickerDialog(
            onDismissRequest = { showStartTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        startTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                        showStartTimePicker = false
                    }
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartTimePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }

    if (showEndTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = endTime?.hour ?: 10,
            initialMinute = endTime?.minute ?: 0,
            is24Hour = true
        )
        TimePickerDialog(
            onDismissRequest = { showEndTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        endTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                        showEndTimePicker = false
                    }
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndTimePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }

    if (showRecurrenceEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = (recurrenceEndDate ?: selectedDate.plusMonths(1))
                .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showRecurrenceEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            recurrenceEndDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                        }
                        showRecurrenceEndDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRecurrenceEndDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        text = content
    )
}

@Composable
private fun RecurrenceSection(
    recurrenceFrequency: RecurrenceFrequency,
    onRecurrenceFrequencyChange: (RecurrenceFrequency) -> Unit,
    selectedWeekDays: Set<DayOfWeek>,
    onWeekDaysChange: (Set<DayOfWeek>) -> Unit,
    recurrenceEndOption: RecurrenceEndOption,
    onRecurrenceEndOptionChange: (RecurrenceEndOption) -> Unit,
    recurrenceEndDate: LocalDate?,
    onRecurrenceEndDateClick: () -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.repeat),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Recurrence frequency options
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = recurrenceFrequency == RecurrenceFrequency.NONE,
                onClick = { onRecurrenceFrequencyChange(RecurrenceFrequency.NONE) },
                label = { Text(stringResource(R.string.no_repeat)) }
            )
            FilterChip(
                selected = recurrenceFrequency == RecurrenceFrequency.WEEKLY,
                onClick = { onRecurrenceFrequencyChange(RecurrenceFrequency.WEEKLY) },
                label = { Text(stringResource(R.string.weekly)) }
            )
        }

        // Weekly options
        if (recurrenceFrequency == RecurrenceFrequency.WEEKLY) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.repeat_on),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Weekday selection
            WeekDaySelector(
                selectedDays = selectedWeekDays,
                onDaysChange = onWeekDaysChange
            )

            Spacer(modifier = Modifier.height(12.dp))

            // End options
            Text(
                text = stringResource(R.string.ends),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = recurrenceEndOption == RecurrenceEndOption.NEVER,
                    onClick = { onRecurrenceEndOptionChange(RecurrenceEndOption.NEVER) },
                    label = { Text(stringResource(R.string.never)) }
                )
                FilterChip(
                    selected = recurrenceEndOption == RecurrenceEndOption.UNTIL,
                    onClick = { onRecurrenceEndOptionChange(RecurrenceEndOption.UNTIL) },
                    label = {
                        Text(
                            if (recurrenceEndDate != null)
                                stringResource(R.string.until_date, recurrenceEndDate.toString())
                            else
                                stringResource(R.string.until)
                        )
                    }
                )
            }

            if (recurrenceEndOption == RecurrenceEndOption.UNTIL && recurrenceEndDate == null) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onRecurrenceEndDateClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.DateRange, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.select_end_date))
                }
            }
        }
    }
}

@Composable
private fun WeekDaySelector(
    selectedDays: Set<DayOfWeek>,
    onDaysChange: (Set<DayOfWeek>) -> Unit
) {
    val weekDays = listOf(
        DayOfWeek.MONDAY to "M",
        DayOfWeek.TUESDAY to "T",
        DayOfWeek.WEDNESDAY to "W",
        DayOfWeek.THURSDAY to "T",
        DayOfWeek.FRIDAY to "F",
        DayOfWeek.SATURDAY to "S",
        DayOfWeek.SUNDAY to "S"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        weekDays.forEach { (day, label) ->
            val isSelected = selectedDays.contains(day)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable {
                        val newDays = if (isSelected) {
                            selectedDays - day
                        } else {
                            selectedDays + day
                        }
                        onDaysChange(newDays)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun ReminderOptions(
    selectedMinutes: Int,
    onMinutesChange: (Int) -> Unit
) {
    val reminderOptions = listOf(
        0 to R.string.at_time_of_event,
        5 to R.string.minutes_before_5,
        15 to R.string.minutes_before_15,
        30 to R.string.minutes_before_30,
        60 to R.string.hour_before_1
    )

    Column(modifier = Modifier.padding(start = 16.dp, top = 8.dp)) {
        reminderOptions.forEach { (minutes, stringRes) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onMinutesChange(minutes) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedMinutes == minutes,
                    onClick = { onMinutesChange(minutes) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(stringRes),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun CategorySelector(
    selectedCategory: String,
    onCategoryChange: (String) -> Unit
) {
    val categories = listOf("PERSONAL", "WORK", "RELIGIOUS", "BIRTHDAY", "ANNIVERSARY")

    Column {
        Text(
            text = stringResource(R.string.category),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.take(3).forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { onCategoryChange(cat) },
                    label = { Text(cat) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
