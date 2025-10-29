package com.ethiopiancalendar.ui.holidaylist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ethiopiancalendar.domain.model.HolidayOccurrence
import com.ethiopiancalendar.domain.model.HolidayType
import org.threeten.extra.chrono.EthiopicDate
import java.time.LocalDate
import java.time.temporal.ChronoField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HolidayListScreen(
    viewModel: HolidayListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ethiopian Holidays",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is HolidayListUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is HolidayListUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            is HolidayListUiState.Success -> {
                HolidayListContent(
                    modifier = Modifier.padding(padding),
                    currentYear = state.currentYear,
                    holidays = state.filteredHolidays,
                    selectedFilters = state.selectedFilters,
                    onYearIncrement = { viewModel.incrementYear() },
                    onYearDecrement = { viewModel.decrementYear() },
                    onFilterToggle = { holidayType -> viewModel.toggleFilter(holidayType) }
                )
            }
        }
    }
}

@Composable
private fun HolidayListContent(
    modifier: Modifier = Modifier,
    currentYear: Int,
    holidays: List<HolidayOccurrence>,
    selectedFilters: Set<HolidayType>,
    onYearIncrement: () -> Unit,
    onYearDecrement: () -> Unit,
    onFilterToggle: (HolidayType) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Year Selector Section
        YearSelectorSection(
            currentYear = currentYear,
            onYearIncrement = onYearIncrement,
            onYearDecrement = onYearDecrement
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Filter Section
        FilterSection(
            selectedFilters = selectedFilters,
            onFilterToggle = onFilterToggle
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Holiday List
        if (holidays.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No holidays to display",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(holidays) { holiday ->
                    HolidayListItem(holiday = holiday)
                }
            }
        }
    }
}

@Composable
private fun YearSelectorSection(
    currentYear: Int,
    onYearIncrement: () -> Unit,
    onYearDecrement: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onYearDecrement) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous Year"
                )
            }

            Text(
                text = "$currentYear E.C.",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .widthIn(min = 150.dp)
                    .padding(horizontal = 16.dp)
            )

            IconButton(onClick = onYearIncrement) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next Year"
                )
            }
        }
    }
}

@Composable
private fun FilterSection(
    selectedFilters: Set<HolidayType>,
    onFilterToggle: (HolidayType) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Filter by Type",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterCheckbox(
                    label = "Public",
                    holidayType = HolidayType.NATIONAL,
                    isChecked = selectedFilters.contains(HolidayType.NATIONAL),
                    onCheckedChange = { onFilterToggle(HolidayType.NATIONAL) }
                )

                FilterCheckbox(
                    label = "Orthodox",
                    holidayType = HolidayType.ORTHODOX_CHRISTIAN,
                    isChecked = selectedFilters.contains(HolidayType.ORTHODOX_CHRISTIAN),
                    onCheckedChange = { onFilterToggle(HolidayType.ORTHODOX_CHRISTIAN) }
                )

                FilterCheckbox(
                    label = "Muslim",
                    holidayType = HolidayType.MUSLIM,
                    isChecked = selectedFilters.contains(HolidayType.MUSLIM),
                    onCheckedChange = { onFilterToggle(HolidayType.MUSLIM) }
                )
            }
        }
    }
}

@Composable
private fun FilterCheckbox(
    label: String,
    holidayType: HolidayType,
    isChecked: Boolean,
    onCheckedChange: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = { onCheckedChange() }
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
private fun HolidayListItem(
    holiday: HolidayOccurrence
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Holiday Type Color Indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = holiday.holiday.type.color,
                        shape = MaterialTheme.shapes.small
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Holiday Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = holiday.holiday.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = formatEthiopicDateWithGregorian(holiday.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (holiday.holiday.description.isNotEmpty()) {
                    Text(
                        text = holiday.holiday.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Day Off Badge
            if (holiday.holiday.isDayOff) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "Day Off",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Format EthiopicDate with both Ethiopian and Gregorian dates
 */
private fun formatEthiopicDateWithGregorian(date: EthiopicDate): String {
    val monthNames = listOf(
        "Meskerem", "Tikimt", "Hidar", "Tahsas", "Tir", "Yekatit",
        "Megabit", "Miazia", "Ginbot", "Sene", "Hamle", "Nehase", "Pagume"
    )

    val year = date.get(ChronoField.YEAR_OF_ERA)
    val month = date.get(ChronoField.MONTH_OF_YEAR)
    val day = date.get(ChronoField.DAY_OF_MONTH)
    val gregorianDate = LocalDate.from(date)

    val monthName = if (month in 1..13) monthNames[month - 1] else "Unknown"

    return "$monthName $day, $year (${gregorianDate.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${gregorianDate.dayOfMonth}, ${gregorianDate.year})"
}
