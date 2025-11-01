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

@Composable
fun WidgetSettingsDialog(
    displayTwoClocks: Boolean,
    primaryTimezone: String,
    secondaryTimezone: String,
    useTransparentBackground: Boolean,
    onDisplayTwoClocksChange: (Boolean) -> Unit,
    onPrimaryTimezoneChange: (String) -> Unit,
    onSecondaryTimezoneChange: (String) -> Unit,
    onUseTransparentBackgroundChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var primaryTimezoneText by remember { mutableStateOf(primaryTimezone) }
    var secondaryTimezoneText by remember { mutableStateOf(secondaryTimezone) }

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

                // Primary Widget timezone
                OutlinedTextField(
                    value = primaryTimezoneText,
                    onValueChange = {
                        primaryTimezoneText = it
                        onPrimaryTimezoneChange(it)
                    },
                    label = { Text(stringResource(R.string.settings_primary_timezone)) },
                    placeholder = { Text(stringResource(R.string.settings_timezone_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Secondary Widget timezone
                OutlinedTextField(
                    value = secondaryTimezoneText,
                    onValueChange = {
                        secondaryTimezoneText = it
                        onSecondaryTimezoneChange(it)
                    },
                    label = { Text(stringResource(R.string.settings_secondary_timezone)) },
                    placeholder = { Text(stringResource(R.string.settings_timezone_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = displayTwoClocks
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
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.button_ok))
            }
        }
    )
}
