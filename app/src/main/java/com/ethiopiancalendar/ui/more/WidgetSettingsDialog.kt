package com.ethiopiancalendar.ui.more

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ethiopiancalendar.R
import com.ethiopiancalendar.data.model.getTimeZoneList
import com.ethiopiancalendar.ui.components.AutocompleteTextField

@Composable
fun WidgetSettingsDialog(
    displayTwoClocks: Boolean,
    primaryTimezone: String,
    secondaryTimezone: String,
    useTransparentBackground: Boolean,
    use24HourFormat: Boolean,
    onDisplayTwoClocksChange: (Boolean) -> Unit,
    onPrimaryTimezoneChange: (String) -> Unit,
    onSecondaryTimezoneChange: (String) -> Unit,
    onUseTransparentBackgroundChange: (Boolean) -> Unit,
    onUse24HourFormatChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    // Get the list of timezones
    val timeZoneList = remember { getTimeZoneList() }

    // Find display names for the current timezone IDs
    val primaryDisplayName = remember(primaryTimezone) {
        timeZoneList.find { it.zoneId == primaryTimezone }?.displayName ?: primaryTimezone
    }
    val secondaryDisplayName = remember(secondaryTimezone) {
        timeZoneList.find { it.zoneId == secondaryTimezone }?.displayName ?: secondaryTimezone
    }

    var primaryTimezoneText by remember { mutableStateOf(primaryDisplayName) }
    var secondaryTimezoneText by remember { mutableStateOf(secondaryDisplayName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.settings_widget_dialog_title),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Display two clocks
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.settings_display_two_clocks),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = displayTwoClocks,
                        onCheckedChange = onDisplayTwoClocksChange
                    )
                }

                // Primary Widget timezone with autocomplete
                AutocompleteTextField(
                    value = primaryTimezoneText,
                    onValueChange = {
                        primaryTimezoneText = it
                    },
                    onTimezoneSelected = { timezone ->
                        primaryTimezoneText = timezone.displayName
                        onPrimaryTimezoneChange(timezone.zoneId)
                    },
                    timeZoneList = timeZoneList,
                    label = stringResource(R.string.settings_primary_timezone),
                    placeholder = stringResource(R.string.settings_timezone_hint),
                    modifier = Modifier.fillMaxWidth()
                )

                // Secondary Widget timezone with autocomplete
                AutocompleteTextField(
                    value = secondaryTimezoneText,
                    onValueChange = {
                        secondaryTimezoneText = it
                    },
                    onTimezoneSelected = { timezone ->
                        secondaryTimezoneText = timezone.displayName
                        onSecondaryTimezoneChange(timezone.zoneId)
                    },
                    timeZoneList = timeZoneList,
                    label = stringResource(R.string.settings_secondary_timezone),
                    placeholder = stringResource(R.string.settings_timezone_hint),
                    enabled = displayTwoClocks,
                    modifier = Modifier.fillMaxWidth()
                )

                // Use transparent background
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.settings_use_transparent_background),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = useTransparentBackground,
                        onCheckedChange = onUseTransparentBackgroundChange
                    )
                }

                // Use 24 hour format
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.settings_use_24_hour_format),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = use24HourFormat,
                        onCheckedChange = onUse24HourFormatChange
                    )
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
