package com.ethiopiancalendar.ui.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ethiopiancalendar.R
import com.ethiopiancalendar.data.preferences.CalendarType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val primaryCalendar by viewModel.primaryCalendar.collectAsState()
    val displayDualCalendar by viewModel.displayDualCalendar.collectAsState()
    val secondaryCalendar by viewModel.secondaryCalendar.collectAsState()
    val showOrthodoxDayNames by viewModel.showOrthodoxDayNames.collectAsState()
    val showOrthodoxFastingHolidays by viewModel.showOrthodoxFastingHolidays.collectAsState()
    val showMuslimHolidays by viewModel.showMuslimHolidays.collectAsState()
    val showUsHolidays by viewModel.showUsHolidays.collectAsState()
    val useGeezNumbers by viewModel.useGeezNumbers.collectAsState()

    val displayTwoClocks by viewModel.displayTwoClocks.collectAsState()
    val primaryWidgetTimezone by viewModel.primaryWidgetTimezone.collectAsState()
    val secondaryWidgetTimezone by viewModel.secondaryWidgetTimezone.collectAsState()
    val useTransparentBackground by viewModel.useTransparentBackground.collectAsState()
    val use24HourFormat by viewModel.use24HourFormat.collectAsState()

    var showWidgetDialog by remember { mutableStateOf(false) }
    var showCalendarDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.screen_title_settings)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Calendar Display Section
            item {
                Text(
                    text = stringResource(R.string.settings_calendar_display),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Calendar to Display Button
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCalendarDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.settings_calendar_to_display),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = stringResource(R.string.cd_navigate),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            item {
                SettingSwitchItem(
                    title = stringResource(R.string.settings_show_orthodox_day_names),
                    checked = showOrthodoxDayNames,
                    onCheckedChange = { viewModel.setShowOrthodoxDayNames(it) }
                )
            }

            item {
                SettingSwitchItem(
                    title = stringResource(R.string.settings_show_orthodox_fasting_holidays),
                    checked = showOrthodoxFastingHolidays,
                    onCheckedChange = { viewModel.setShowOrthodoxFastingHolidays(it) }
                )
            }

            item {
                SettingSwitchItem(
                    title = stringResource(R.string.settings_show_muslim_holidays),
                    checked = showMuslimHolidays,
                    onCheckedChange = { viewModel.setShowMuslimHolidays(it) }
                )
            }

            item {
                SettingSwitchItem(
                    title = stringResource(R.string.settings_show_us_holidays),
                    checked = showUsHolidays,
                    onCheckedChange = { viewModel.setShowUsHolidays(it) }
                )
            }

            item {
                SettingSwitchItem(
                    title = stringResource(R.string.settings_use_geez_numbers),
                    checked = useGeezNumbers,
                    onCheckedChange = { viewModel.setUseGeezNumbers(it) }
                )
            }

            // Widget Options Section
            item {
                Text(
                    text = stringResource(R.string.settings_widget_options),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showWidgetDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.settings_widget_options),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = stringResource(R.string.cd_navigate),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Widget Settings Dialog
        if (showWidgetDialog) {
            WidgetSettingsDialog(
                displayTwoClocks = displayTwoClocks,
                primaryTimezone = primaryWidgetTimezone,
                secondaryTimezone = secondaryWidgetTimezone,
                useTransparentBackground = useTransparentBackground,
                use24HourFormat = use24HourFormat,
                onDisplayTwoClocksChange = { viewModel.setDisplayTwoClocks(it) },
                onPrimaryTimezoneChange = { viewModel.setPrimaryWidgetTimezone(it) },
                onSecondaryTimezoneChange = { viewModel.setSecondaryWidgetTimezone(it) },
                onUseTransparentBackgroundChange = { viewModel.setUseTransparentBackground(it) },
                onUse24HourFormatChange = { viewModel.setUse24HourFormat(it) },
                onDismiss = { showWidgetDialog = false }
            )
        }

        // Calendar Display Dialog
        if (showCalendarDialog) {
            CalendarDisplayDialog(
                primaryCalendar = primaryCalendar,
                displayDualCalendar = displayDualCalendar,
                secondaryCalendar = secondaryCalendar,
                onPrimaryCalendarChange = { viewModel.setPrimaryCalendar(it) },
                onDisplayDualCalendarChange = { viewModel.setDisplayDualCalendar(it) },
                onSecondaryCalendarChange = { viewModel.setSecondaryCalendar(it) },
                onDismiss = { showCalendarDialog = false }
            )
        }
    }
}

@Composable
fun SettingSwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f).padding(end = 16.dp)
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
fun CalendarTypeSelector(
    title: String,
    selectedCalendar: CalendarType,
    onCalendarSelected: (CalendarType) -> Unit,
    disabledCalendar: CalendarType? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            CalendarTypeOption(
                text = stringResource(R.string.settings_calendar_ethiopian),
                selected = selectedCalendar == CalendarType.ETHIOPIAN,
                enabled = disabledCalendar != CalendarType.ETHIOPIAN,
                onClick = { onCalendarSelected(CalendarType.ETHIOPIAN) }
            )

            CalendarTypeOption(
                text = stringResource(R.string.settings_calendar_gregorean),
                selected = selectedCalendar == CalendarType.GREGOREAN,
                enabled = disabledCalendar != CalendarType.GREGOREAN,
                onClick = { onCalendarSelected(CalendarType.GREGOREAN) }
            )
        }
    }
}

@Composable
fun CalendarTypeOption(
    text: String,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            enabled = enabled,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                disabledSelectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                disabledUnselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
                   else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    }
}

@Composable
fun CalendarDisplayDialog(
    primaryCalendar: CalendarType,
    displayDualCalendar: Boolean,
    secondaryCalendar: CalendarType,
    onPrimaryCalendarChange: (CalendarType) -> Unit,
    onDisplayDualCalendarChange: (Boolean) -> Unit,
    onSecondaryCalendarChange: (CalendarType) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.settings_calendar_display_dialog_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Primary Calendar Section
                Text(
                    text = stringResource(R.string.settings_primary_calendar),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CalendarTypeOption(
                        text = stringResource(R.string.settings_calendar_ethiopian),
                        selected = primaryCalendar == CalendarType.ETHIOPIAN,
                        onClick = { onPrimaryCalendarChange(CalendarType.ETHIOPIAN) }
                    )

                    CalendarTypeOption(
                        text = stringResource(R.string.settings_calendar_gregorean),
                        selected = primaryCalendar == CalendarType.GREGOREAN,
                        onClick = { onPrimaryCalendarChange(CalendarType.GREGOREAN) }
                    )
                }

                HorizontalDivider()

                // Display Dual Calendar Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.settings_display_dual_calendar),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = displayDualCalendar,
                        onCheckedChange = onDisplayDualCalendarChange
                    )
                }

                // Secondary Calendar Section (shown only if dual calendar is enabled)
                if (displayDualCalendar) {
                    HorizontalDivider()

                    Text(
                        text = stringResource(R.string.settings_secondary_calendar),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CalendarTypeOption(
                            text = stringResource(R.string.settings_calendar_ethiopian),
                            selected = secondaryCalendar == CalendarType.ETHIOPIAN,
                            enabled = primaryCalendar != CalendarType.ETHIOPIAN,
                            onClick = {
                                if (primaryCalendar != CalendarType.ETHIOPIAN) {
                                    onSecondaryCalendarChange(CalendarType.ETHIOPIAN)
                                }
                            }
                        )

                        CalendarTypeOption(
                            text = stringResource(R.string.settings_calendar_gregorean),
                            selected = secondaryCalendar == CalendarType.GREGOREAN,
                            enabled = primaryCalendar != CalendarType.GREGOREAN,
                            onClick = {
                                if (primaryCalendar != CalendarType.GREGOREAN) {
                                    onSecondaryCalendarChange(CalendarType.GREGOREAN)
                                }
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.button_ok))
            }
        }
    )
}
