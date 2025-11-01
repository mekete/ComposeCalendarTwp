package com.ethiopiancalendar.ui.month

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ethiopiancalendar.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.ethiopiancalendar.data.preferences.CalendarType
import com.ethiopiancalendar.domain.model.HolidayOccurrence
import kotlinx.coroutines.launch
import org.threeten.extra.chrono.EthiopicDate
import java.time.LocalDate
import java.time.temporal.ChronoField
import java.util.Locale

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
    val monthNames = stringArrayResource(R.array.ethiopian_months)
    val monthDescription = formatEthiopicDate(currentEthiopicDate, monthNames)
    val monthDescriptionCd = stringResource( R.string.cd_calendar_state, monthDescription )
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier
                                .fillMaxWidth()
                                .semantics {
                                    contentDescription = monthDescriptionCd
                                },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.screen_title_month_calendar),
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
                        Text(stringResource(R.string.button_today))
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
            //beyondViewportPageCount = 2, // Preload 2 pages on each side
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
                        text = stringResource(R.string.error_loading_calendar),
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
                    currentGregorianYear = state.currentGregorianYear,
                    currentGregorianMonth = state.currentGregorianMonth,
                    isToday = date == EthiopicDate.now(),
                    isSelected = date == state.selectedDate,
                    holidays = state.holidays.filter {
                        it.actualEthiopicDate.get(ChronoField.DAY_OF_MONTH) == date.get(ChronoField.DAY_OF_MONTH) &&
                                it.actualEthiopicDate.get(ChronoField.MONTH_OF_YEAR) == date.get(ChronoField.MONTH_OF_YEAR) &&
                                it.actualEthiopicDate.get(ChronoField.YEAR_OF_ERA) == date.get(ChronoField.YEAR_OF_ERA)
                    },
                    primaryCalendar = state.primaryCalendar,
                    displayDualCalendar = state.displayDualCalendar,
                    secondaryCalendar = state.secondaryCalendar,
                    onClick = { onDateClick(date) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Holiday list
        HolidayListSection(
            holidays = state.holidays,
            modifier = Modifier.height(200.dp)
        )
    }
}

@Composable
fun WeekdayHeader() {
    val weekdays = stringArrayResource(R.array.weekday_names_short).toList()

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
    currentGregorianYear: Int?,
    currentGregorianMonth: Int?,
    isToday: Boolean,
    isSelected: Boolean,
    holidays: List<HolidayOccurrence>,
    primaryCalendar: CalendarType,
    displayDualCalendar: Boolean,
    secondaryCalendar: CalendarType,
    onClick: () -> Unit
) {
    val ethiopianDayOfMonth = date.get(ChronoField.DAY_OF_MONTH)
    val gregorianDate = LocalDate.from(date)
    val gregorianDayOfMonth = gregorianDate.dayOfMonth
    val monthNames = stringArrayResource(R.array.ethiopian_months)

    // Determine if the date is in the "current" month based on primary calendar
    val isCurrentMonth = when (primaryCalendar) {
        CalendarType.GREGOREAN -> {
            // Check against Gregorian month when Gregorian is primary
            currentGregorianYear != null && currentGregorianMonth != null &&
                    gregorianDate.year == currentGregorianYear &&
                    gregorianDate.monthValue == currentGregorianMonth
        }
        else -> {
            // Check against Ethiopian month for Ethiopian and other calendars
            date.get(ChronoField.MONTH_OF_YEAR) == currentMonth
        }
    }

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
        append(formatEthiopicDateFull(date, monthNames))
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
                .padding(2.dp)
                .semantics {
                    this.contentDescription = contentDesc
                },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            // Determine what to display based on preferences
            when {
                // Dual calendar display
                displayDualCalendar -> {
                    if (primaryCalendar == CalendarType.ETHIOPIAN && secondaryCalendar == CalendarType.GREGOREAN) {
                        // Ethiopian (large), Gregorian (small)
                        Row(
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 2.dp, top = 2.dp)
                        ) {
                            Text(
                                text = gregorianDayOfMonth.toString(),
                                fontSize = 8.sp,
                                color = textColor.copy(alpha = 0.6f),
                            )
                            holidays.take(3).forEach { holiday ->
                                Box(
                                    modifier = Modifier
                                            .size(8.dp)
                                            .padding(horizontal = 2.dp)
                                            .clip(CircleShape)
                                            .background(holiday.holiday.type.getColor())
                                )
                            }
                        }

                        Text(
                            text = ethiopianDayOfMonth.toString(),
                            fontSize = 14.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            color = textColor
                        )
                    } else if (primaryCalendar == CalendarType.GREGOREAN && secondaryCalendar == CalendarType.ETHIOPIAN) {
                        // Gregorian (large), Ethiopian (small)
                        Row(
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 2.dp, top = 2.dp)
                        ) {
                            Text(
                                text = ethiopianDayOfMonth.toString(),
                                fontSize = 8.sp,
                                color = textColor.copy(alpha = 0.6f),
                            )
                            holidays.take(3).forEach { holiday ->
                                Box(
                                    modifier = Modifier
                                            .size(8.dp)
                                            .padding(horizontal = 2.dp)
                                            .clip(CircleShape)
                                            .background(holiday.holiday.type.getColor())
                                )
                            }
                        }

                        Text(
                            text = gregorianDayOfMonth.toString(),
                            fontSize = 14.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            color = textColor
                        )
                    } else {
                        // Fallback to Ethiopian primary
                        Row(
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 2.dp, top = 2.dp)
                        ) {
                            holidays.take(3).forEach { holiday ->
                                Box(
                                    modifier = Modifier
                                            .size(8.dp)
                                            .padding(horizontal = 2.dp)
                                            .clip(CircleShape)
                                            .background(holiday.holiday.type.getColor())
                                )
                            }
                        }

                        Text(
                            text = ethiopianDayOfMonth.toString(),
                            fontSize = 14.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            color = textColor
                        )
                    }
                }
                // Single calendar display (primary only)
                else -> {
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 2.dp, top = 2.dp)
                    ) {
                        holidays.take(3).forEach { holiday ->
                            Box(
                                modifier = Modifier
                                        .size(8.dp)
                                        .padding(horizontal = 2.dp)
                                        .clip(CircleShape)
                                        .background(holiday.holiday.type.getColor())
                            )
                        }
                    }

                    Text(
                        text = if (primaryCalendar == CalendarType.ETHIOPIAN) {
                            ethiopianDayOfMonth.toString()
                        } else {
                            gregorianDayOfMonth.toString()
                        },
                        fontSize = 14.sp,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun HolidayListSection(
    holidays: List<HolidayOccurrence>,
    modifier: Modifier = Modifier
) {
    val monthNames = stringArrayResource(R.array.ethiopian_months)

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
                    text = stringResource(R.string.empty_no_holidays_month),
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
                    text = stringResource(R.string.label_holidays),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                holidays.take(3).forEach { holiday ->
                    HolidayItem(holiday = holiday, monthNames = monthNames)
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
fun HolidayItem(
    holiday: HolidayOccurrence,
    monthNames: Array<String>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = String.format(Locale.US,"%02d",  holiday.holiday.ethiopianDay ),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Thin
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                    .size(4.dp, 30.dp)
                    .background(holiday.holiday.type.getColor())
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = holiday.holiday.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = formatEthiopicDate(holiday.actualEthiopicDate, monthNames),
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
                    text = stringResource(R.string.label_day_off),
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
private fun formatEthiopicDate(date: EthiopicDate, monthNames: Array<String>): String {
    val year = date.get(ChronoField.YEAR_OF_ERA)
    val month = date.get(ChronoField.MONTH_OF_YEAR)
    val day = date.get(ChronoField.DAY_OF_MONTH)

    val monthName = if (month in 1..13) monthNames[month - 1] else "Unknown"

    return "$monthName $day, $year"
}

/**
 * Format EthiopicDate with full details for accessibility
 */
private fun formatEthiopicDateFull(date: EthiopicDate, monthNames: Array<String>): String {
    val year = date.get(ChronoField.YEAR_OF_ERA)
    val month = date.get(ChronoField.MONTH_OF_YEAR)
    val day = date.get(ChronoField.DAY_OF_MONTH)
    val gregorianDate = LocalDate.from(date)

    val monthName = if (month in 1..13) monthNames[month - 1] else "Unknown"

    return "$monthName $day, $year (${gregorianDate.month.name} ${gregorianDate.dayOfMonth}, ${gregorianDate.year})"
}