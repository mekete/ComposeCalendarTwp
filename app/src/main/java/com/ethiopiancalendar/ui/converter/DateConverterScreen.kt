package com.ethiopiancalendar.ui.converter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ethiopiancalendar.R
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import org.threeten.extra.chrono.EthiopicDate
import java.time.LocalDate
import java.time.temporal.ChronoField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateConverterScreen(
    viewModel: DateConverterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Date picker states
    var showGregorianPicker by remember { mutableStateOf(false) }
    var showEthiopianPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.screen_title_date_converter),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Section 1: Gregorian to Ethiopian
            GregorianToEthiopianSection(
                gregorianDay = uiState.gregorianDay,
                gregorianMonth = uiState.gregorianMonth,
                gregorianYear = uiState.gregorianYear,
                result = uiState.ethiopianResult,
                error = uiState.gregorianError,
                onDayChange = { day ->
                    viewModel.setGregorianDate(day, uiState.gregorianMonth, uiState.gregorianYear)
                },
                onMonthChange = { month ->
                    viewModel.setGregorianDate(uiState.gregorianDay, month, uiState.gregorianYear)
                },
                onYearChange = { year ->
                    viewModel.setGregorianDate(uiState.gregorianDay, uiState.gregorianMonth, year)
                },
                onPickClick = { showGregorianPicker = true },
                onConvert = { viewModel.convertToEthiopian() }
            )

            HorizontalDivider()

            // Section 2: Ethiopian to Gregorian
            EthiopianToGregorianSection(
                ethiopianDay = uiState.ethiopianDay,
                ethiopianMonth = uiState.ethiopianMonth,
                ethiopianYear = uiState.ethiopianYear,
                result = uiState.gregorianResult,
                error = uiState.ethiopianError,
                onDayChange = { day ->
                    viewModel.setEthiopianDate(day, uiState.ethiopianMonth, uiState.ethiopianYear)
                },
                onMonthChange = { month ->
                    viewModel.setEthiopianDate(uiState.ethiopianDay, month, uiState.ethiopianYear)
                },
                onYearChange = { year ->
                    viewModel.setEthiopianDate(uiState.ethiopianDay, uiState.ethiopianMonth, year)
                },
                onPickClick = { showEthiopianPicker = true },
                onConvert = { viewModel.convertToGregorian() }
            )
        }
    }

    // Gregorian Date Picker Dialog
    if (showGregorianPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )

        DatePickerDialog(
            onDismissRequest = { showGregorianPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                            viewModel.setGregorianDateFromPicker(date)
                        }
                        showGregorianPicker = false
                    }
                ) {
                    Text(stringResource(R.string.button_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showGregorianPicker = false }) {
                    Text(stringResource(R.string.button_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Ethiopian Date Picker Dialog (simplified version)
    if (showEthiopianPicker) {
        EthiopianDatePickerDialog(
            onDismiss = { showEthiopianPicker = false },
            onDateSelected = { date ->
                viewModel.setEthiopianDateFromPicker(date)
                showEthiopianPicker = false
            }
        )
    }
}

@Composable
fun GregorianToEthiopianSection(
    gregorianDay: String,
    gregorianMonth: String,
    gregorianYear: String,
    result: String,
    error: String?,
    onDayChange: (String) -> Unit,
    onMonthChange: (String) -> Unit,
    onYearChange: (String) -> Unit,
    onPickClick: () -> Unit,
    onConvert: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.label_from_gregorian),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            // Input row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DateInputField(
                    value = gregorianDay,
                    onValueChange = onDayChange,
                    label = "Day",
                    placeholder = "DD",
                    modifier = Modifier.weight(1f)
                )

                DateInputField(
                    value = gregorianMonth,
                    onValueChange = onMonthChange,
                    label = "Month",
                    placeholder = "MM",
                    modifier = Modifier.weight(1f)
                )

                DateInputField(
                    value = gregorianYear,
                    onValueChange = onYearChange,
                    label = "Year",
                    placeholder = "YYYY",
                    modifier = Modifier.weight(1.5f)
                )

                IconButton(
                    onClick = onPickClick,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = stringResource(R.string.cd_pick_date)
                    )
                }
            }

            // Convert button
            Button(
                onClick = onConvert,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.button_to_ethiopian))
            }

            // Error message
            if (error != null) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Result display
            if (result.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.label_ethiopian_date),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = result,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EthiopianToGregorianSection(
    ethiopianDay: String,
    ethiopianMonth: String,
    ethiopianYear: String,
    result: String,
    error: String?,
    onDayChange: (String) -> Unit,
    onMonthChange: (String) -> Unit,
    onYearChange: (String) -> Unit,
    onPickClick: () -> Unit,
    onConvert: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.label_from_ethiopian),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            // Input row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DateInputField(
                    value = ethiopianDay,
                    onValueChange = onDayChange,
                    label = "Day",
                    placeholder = "DD",
                    modifier = Modifier.weight(1f)
                )

                DateInputField(
                    value = ethiopianMonth,
                    onValueChange = onMonthChange,
                    label = "Month",
                    placeholder = "MM",
                    modifier = Modifier.weight(1f)
                )

                DateInputField(
                    value = ethiopianYear,
                    onValueChange = onYearChange,
                    label = "Year",
                    placeholder = "YYYY",
                    modifier = Modifier.weight(1.5f)
                )

                IconButton(
                    onClick = onPickClick,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = stringResource(R.string.cd_pick_date)
                    )
                }
            }

            // Convert button
            Button(
                onClick = onConvert,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.button_to_gregorian))
            }

            // Error message
            if (error != null) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Result display
            if (result.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.label_gregorian_date),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = result,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            // Only allow digits
            if (newValue.all { it.isDigit() }) {
                onValueChange(newValue)
            }
        },
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = modifier
    )
}

@Composable
fun EthiopianDatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (EthiopicDate) -> Unit
) {
    val currentEthiopian = EthiopicDate.now()
    var selectedDay by remember { mutableStateOf(currentEthiopian.get(ChronoField.DAY_OF_MONTH)) }
    var selectedMonth by remember { mutableStateOf(currentEthiopian.get(ChronoField.MONTH_OF_YEAR)) }
    var selectedYear by remember { mutableStateOf(currentEthiopian.get(ChronoField.YEAR_OF_ERA)) }
    val monthNames = stringArrayResource(R.array.ethiopian_months)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_title_select_ethiopian_date)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Simple number pickers (simplified for this implementation)
                Text("Day: $selectedDay")
                Slider(
                    value = selectedDay.toFloat(),
                    onValueChange = { selectedDay = it.toInt() },
                    valueRange = 1f..30f,
                    steps = 28
                )

                Text("Month: $selectedMonth (${getEthiopianMonthName(selectedMonth, monthNames)})")
                Slider(
                    value = selectedMonth.toFloat(),
                    onValueChange = { selectedMonth = it.toInt() },
                    valueRange = 1f..13f,
                    steps = 11
                )

                Text("Year: $selectedYear")
                Slider(
                    value = selectedYear.toFloat(),
                    onValueChange = { selectedYear = it.toInt() },
                    valueRange = 2000f..2030f,
                    steps = 29
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    try {
                        val date = EthiopicDate.of(selectedYear, selectedMonth, selectedDay)
                        onDateSelected(date)
                    } catch (e: Exception) {
                        // Invalid date, just dismiss
                        onDismiss()
                    }
                }
            ) {
                Text(stringResource(R.string.button_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.button_cancel))
            }
        }
    )
}

private fun getEthiopianMonthName(month: Int, monthNames: Array<String>): String {
    return if (month in 1..13) monthNames[month - 1] else "Unknown"
}