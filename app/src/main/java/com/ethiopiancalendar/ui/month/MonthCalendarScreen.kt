package com.ethiopiancalendar.ui.month

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ethiopiancalendar.domain.model.HolidayOccurrence
import kotlinx.coroutines.launch
import org.threeten.extra.chrono.EthiopicDate
import java.time.LocalDate
import java.time.temporal.ChronoField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthCalendarScreen(
    viewModel: MonthCalendarViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()

    // Initialize pager state with saved state support
    val pagerState = rememberPagerState(
        initialPage = rememberSaveable { viewModel.initialPage },
        pageCount = { MonthCalendarViewModel.TOTAL_PAGES }
    )

    // Track current page's Ethiopian date
    val currentEthiopicDate = remember(pagerState.currentPage) {
        viewModel.getEthiopicDateForPage(pagerState.currentPage)
    }

    // Content description for accessibility
    val monthDescription = formatEthiopicDate(currentEthiopicDate)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier
                                .fillMaxWidth()
                                .semantics {
                                    contentDescription = "Ethiopian Calendar, currently showing $monthDescription"
                                },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Ethiopian Calendar",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = monthDescription,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Today button - animate scroll to today's page
                    TextButton(
                        onClick = {
                            scope.launch {
                                val todayPage = viewModel.getTodayPage()
                                pagerState.animateScrollToPage(todayPage)
                            }
                        }
                    ) {
                        Text("Today")
                    }
                }
            )
        }
    ) { padding ->
        // HorizontalPager with recommended configuration
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            beyondViewportPageCount = 2, // Preload 2 pages on each side
            flingBehavior = PagerDefaults.flingBehavior(state = pagerState)
        ) { page ->
            // Load data for this page
            val monthData by viewModel.loadMonthDataForPage(page)
                    .collectAsState(initial = MonthCalendarUiState.Loading)

            MonthCalendarPage(
                uiState = monthData,
                onDateClick = { date ->
                    viewModel.selectDate(date)
                    // Optionally navigate to the month of the selected date
                    val datePage = viewModel.getPageForEthiopicDate(date)
                    if (datePage != page) {
                        scope.launch {
                            pagerState.animateScrollToPage(datePage)
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun MonthCalendarPage(
    uiState: MonthCalendarUiState,
    onDateClick: (EthiopicDate) -> Unit
) {
    when (uiState) {
        is MonthCalendarUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is MonthCalendarUiState.Success -> {
            MonthCalendarContent(
                state = uiState,
                onDateClick = onDateClick
            )
        }

        is MonthCalendarUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Error loading calendar",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun MonthCalendarContent(
    state: MonthCalendarUiState.Success,
    onDateClick: (EthiopicDate) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
    ) {
        // Weekday headers
        WeekdayHeader()

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(state.dateList.size) { index ->
                val date = state.dateList[index]
                DateCell(
                    date = date,
                    currentMonth = state.currentMonth.get(ChronoField.MONTH_OF_YEAR),
                    isToday = date == EthiopicDate.now(),
                    isSelected = date == state.selectedDate,
                    holidays = state.holidays.filter {
                        it.actualEthiopicDate.get(ChronoField.DAY_OF_MONTH) == date.get(ChronoField.DAY_OF_MONTH) &&
                                it.actualEthiopicDate.get(ChronoField.MONTH_OF_YEAR) == date.get(ChronoField.MONTH_OF_YEAR) &&
                                it.actualEthiopicDate.get(ChronoField.YEAR_OF_ERA) == date.get(ChronoField.YEAR_OF_ERA)
                    },
                    onClick = { onDateClick(date) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Holiday list
        HolidayListSection(
            holidays = state.holidays,
            modifier = Modifier.height(150.dp)
        )
    }
}

@Composable
fun WeekdayHeader() {
    val weekdays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        weekdays.forEach { day ->
            Text(
                text = day,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun DateCell(
    date: EthiopicDate,
    currentMonth: Int,
    isToday: Boolean,
    isSelected: Boolean,
    holidays: List<HolidayOccurrence>,
    onClick: () -> Unit
) {
    val isCurrentMonth = date.get(ChronoField.MONTH_OF_YEAR) == currentMonth
    val dayOfMonth = date.get(ChronoField.DAY_OF_MONTH)
    val gregorianDate = LocalDate.from(date)

    val backgroundColor = when {
        isToday -> MaterialTheme.colorScheme.primaryContainer
        isSelected -> MaterialTheme.colorScheme.secondaryContainer
        else -> Color.Transparent
    }

    val textColor = when {
        !isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        isToday || isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    // Accessibility content description
    val contentDesc = buildString {
        append(formatEthiopicDateFull(date))
        if (isToday) append(", Today")
        if (isSelected) append(", Selected")
        if (holidays.isNotEmpty()) {
            val holidayNames = holidays.joinToString(", ") { it.holiday.name }
            append(", Holidays: $holidayNames")
        }
    }

    Box(
        modifier = Modifier
                .aspectRatio(1f)
                .clip(CircleShape)
                .background(backgroundColor)
                .clickable { onClick() }
                .padding(4.dp)
                .semantics {
                    this.contentDescription = contentDesc
                },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Gregorian date (small, top)
            Text(
                text = gregorianDate.dayOfMonth.toString(),
                fontSize = 10.sp,
                color = textColor.copy(alpha = 0.6f)
            )

            // Ethiopian date (large, main)
            Text(
                text = dayOfMonth.toString(),
                fontSize = 16.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )

            // Holiday indicators - show up to 3 colored circles
            if (holidays.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                            .height(8.dp)
                            .padding(top = 2.dp)
                ) {
                    holidays.take(3).forEach { holiday ->
                        Box(
                            modifier = Modifier
                                    .size(5.dp)
                                    .padding(horizontal = 0.5.dp)
                                    .clip(CircleShape)
                                    .background(holiday.holiday.type.getColor())
                        )
                    }
                }
            } else {
                // Empty space to maintain consistent cell height
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun HolidayListSection(
    holidays: List<HolidayOccurrence>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        if (holidays.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No holidays this month",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
            ) {
                Text(
                    text = "Holidays",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                holidays.take(3).forEach { holiday ->
                    HolidayItem(holiday = holiday)
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
fun HolidayItem(holiday: HolidayOccurrence) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Color indicator
        Box(
            modifier = Modifier
                    .size(4.dp, 30.dp)
                    .background(holiday.holiday.type.getColor())
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = holiday.holiday.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = formatEthiopicDate(holiday.actualEthiopicDate),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

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

/**
 * Format EthiopicDate to readable string
 */
private fun formatEthiopicDate(date: EthiopicDate): String {
    val monthNames = listOf(
        "Meskerem", "Tikimt", "Hidar", "Tahsas", "Tir", "Yekatit",
        "Megabit", "Miazia", "Ginbot", "Sene", "Hamle", "Nehase", "Pagume"
    )

    val year = date.get(ChronoField.YEAR_OF_ERA)
    val month = date.get(ChronoField.MONTH_OF_YEAR)
    val day = date.get(ChronoField.DAY_OF_MONTH)

    val monthName = if (month in 1..13) monthNames[month - 1] else "Unknown"

    return "$monthName $day, $year"
}

/**
 * Format EthiopicDate with full details for accessibility
 */
private fun formatEthiopicDateFull(date: EthiopicDate): String {
    val monthNames = listOf(
        "Meskerem", "Tikimt", "Hidar", "Tahsas", "Tir", "Yekatit",
        "Megabit", "Miazia", "Ginbot", "Sene", "Hamle", "Nehase", "Pagume"
    )

    val year = date.get(ChronoField.YEAR_OF_ERA)
    val month = date.get(ChronoField.MONTH_OF_YEAR)
    val day = date.get(ChronoField.DAY_OF_MONTH)
    val gregorianDate = LocalDate.from(date)

    val monthName = if (month in 1..13) monthNames[month - 1] else "Unknown"

    return "$monthName $day, $year (${gregorianDate.month.name} ${gregorianDate.dayOfMonth}, ${gregorianDate.year})"
}