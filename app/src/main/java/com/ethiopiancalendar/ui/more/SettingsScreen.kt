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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val showOrthodoxDayNames by viewModel.showOrthodoxDayNames.collectAsState()
    val showOrthodoxFastingHolidays by viewModel.showOrthodoxFastingHolidays.collectAsState()
    val showMuslimHolidays by viewModel.showMuslimHolidays.collectAsState()
    val showUsHolidays by viewModel.showUsHolidays.collectAsState()
    val useGeezNumbers by viewModel.useGeezNumbers.collectAsState()
    val use24HourFormat by viewModel.use24HourFormat.collectAsState()

    val displayTwoClocks by viewModel.displayTwoClocks.collectAsState()
    val primaryWidgetTimezone by viewModel.primaryWidgetTimezone.collectAsState()
    val secondaryWidgetTimezone by viewModel.secondaryWidgetTimezone.collectAsState()
    val useTransparentBackground by viewModel.useTransparentBackground.collectAsState()

    var showWidgetDialog by remember { mutableStateOf(false) }

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

            item {
                SettingSwitchItem(
                    title = stringResource(R.string.settings_use_24_hour_format),
                    checked = use24HourFormat,
                    onCheckedChange = { viewModel.setUse24HourFormat(it) }
                )
            }

            // Widget Options Section
            item {
                Text(
                    text = stringResource(R.string.settings_widget_options),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp, top = 16.dp)
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
                onDisplayTwoClocksChange = { viewModel.setDisplayTwoClocks(it) },
                onPrimaryTimezoneChange = { viewModel.setPrimaryWidgetTimezone(it) },
                onSecondaryTimezoneChange = { viewModel.setSecondaryWidgetTimezone(it) },
                onUseTransparentBackgroundChange = { viewModel.setUseTransparentBackground(it) },
                onDismiss = { showWidgetDialog = false }
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
