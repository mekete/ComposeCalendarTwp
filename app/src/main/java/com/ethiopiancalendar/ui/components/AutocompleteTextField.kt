package com.ethiopiancalendar.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.ethiopiancalendar.data.model.TimeZoneData

/**
 * An autocomplete text field component for timezone selection.
 *
 * Features:
 * - Real-time filtering based on user input
 * - Case-insensitive search across searchable cities
 * - Dropdown suggestions list
 * - Displays timezone display name while storing zone ID
 *
 * @param value Current text value in the input field
 * @param onValueChange Callback when the text value changes
 * @param onTimezoneSelected Callback when a timezone is selected from suggestions
 * @param timeZoneList List of available timezones to search through
 * @param label Label for the text field
 * @param placeholder Placeholder text when field is empty
 * @param enabled Whether the field is enabled
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutocompleteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onTimezoneSelected: (TimeZoneData) -> Unit,
    timeZoneList: List<TimeZoneData>,
    label: String,
    placeholder: String,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf(value) }

    // Update searchText when value prop changes (e.g., when dialog opens with existing value)
    LaunchedEffect(value) {
        if (searchText != value) {
            searchText = value
        }
    }

    // Filter timezones based on search text
    val filteredTimezones = remember(searchText, timeZoneList) {
        if (searchText.isBlank()) {
            emptyList()
        } else {
            timeZoneList.filter { timezone ->
                // Search in searchable cities string (case-insensitive)
                timezone.searchableCities.contains(searchText, ignoreCase = true) ||
                // Also search in display name
                timezone.displayName.contains(searchText, ignoreCase = true)
            }.take(10) // Limit to 10 suggestions for better UX
        }
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = searchText,
            onValueChange = { newValue ->
                searchText = newValue
                onValueChange(newValue)
                expanded = newValue.isNotEmpty() && filteredTimezones.isNotEmpty()
            },
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = enabled
        )

        // Dropdown suggestions
        DropdownMenu(
            expanded = expanded && filteredTimezones.isNotEmpty(),
            onDismissRequest = { expanded = false },
            properties = PopupProperties(focusable = false),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(
                modifier = Modifier
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                filteredTimezones.forEach { timezone ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = timezone.displayName,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = timezone.zoneId,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onClick = {
                            searchText = timezone.displayName
                            onValueChange(timezone.displayName)
                            onTimezoneSelected(timezone)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
