package com.ethiopiancalendar.ui.month

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ethiopiancalendar.domain.model.EthiopianDate
import com.ethiopiancalendar.domain.model.HolidayOccurrence

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthCalendarScreen(
    viewModel: MonthCalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Ethiopian Calendar",
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (uiState is MonthCalendarUiState.Success) {
                            Text(
                                text = (uiState as MonthCalendarUiState.Success).currentMonth.format(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.previousMonth() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Previous Month")
                    }
                    
                    TextButton(onClick = { viewModel.goToToday() }) {
                        Text("Today")
                    }
                    
                    IconButton(onClick = { viewModel.nextMonth() }) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Next Month")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
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
                    state = state,
                    onDateClick = { date -> viewModel.selectDate(date) },
                    modifier = Modifier.padding(padding)
                )
            }
            
            is MonthCalendarUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Error: ${state.message}")
                }
            }
        }
    }
}

@Composable
fun MonthCalendarContent(
    state: MonthCalendarUiState.Success,
    onDateClick: (EthiopianDate) -> Unit,
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
            items(state.dateList) { date ->
                DateCell(
                    date = date,
                    currentMonth = state.currentMonth.month,
                    isToday = date == EthiopianDate.now(),
                    isSelected = date == state.selectedDate,
                    holidays = state.holidays.filter { 
                        it.actualEthiopianDate.day == date.day &&
                        it.actualEthiopianDate.month == date.month
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
    date: EthiopianDate,
    currentMonth: Int,
    isToday: Boolean,
    isSelected: Boolean,
    holidays: List<HolidayOccurrence>,
    onClick: () -> Unit
) {
    val isCurrentMonth = date.month == currentMonth
    
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
    
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Gregorian date (small, top)
            val gregorianDate = date.toGregorianDate()
            Text(
                text = gregorianDate.dayOfMonth.toString(),
                fontSize = 10.sp,
                color = textColor.copy(alpha = 0.6f)
            )
            
            // Ethiopian date (large, main)
            Text(
                text = date.day.toString(),
                fontSize = 16.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )
            
            // Holiday indicator
            if (holidays.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(2.dp)
                        .background(holidays.first().holiday.type.getColor())
                )
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
                text = holiday.actualEthiopianDate.format(),
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
