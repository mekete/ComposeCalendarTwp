package com.ethiopiancalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ethiopiancalendar.ui.converter.DateConverterScreen
import com.ethiopiancalendar.ui.holidaylist.HolidayListScreen
import com.ethiopiancalendar.ui.month.MonthCalendarScreen
import com.ethiopiancalendar.ui.settings.SettingsScreen
import com.ethiopiancalendar.ui.settings.ThemeViewModel
import com.ethiopiancalendar.ui.theme.EthiopianCalendarTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val appTheme by themeViewModel.appTheme.collectAsState()
            val themeMode by themeViewModel.themeMode.collectAsState()

            EthiopianCalendarTheme(
                appTheme = appTheme,
                themeMode = themeMode
            ) {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "month"

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "month",
            modifier = Modifier.padding(padding)
        ) {
            composable("month") {
                MonthCalendarScreen()
            }
            composable("today") {
                PlaceholderScreen("News, Events & Reminders")
            }
            composable("holiday") {
                PlaceholderScreen("Holidays & Days")
            }
            composable("converter") {
                DateConverterScreen()
            }
            composable("settings") {
                SettingsScreen()
            }
        }
    }
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem("month", "Month", Icons.Default.CalendarMonth),
    BottomNavItem("today", "Today", Icons.Default.Today),
    BottomNavItem("holiday", "Holiday", Icons.Default.Event),
    BottomNavItem("converter", "Convert", Icons.Default.SwapHoriz),
    BottomNavItem("settings", "Settings", Icons.Default.Settings)
)

@Composable
fun PlaceholderScreen(title: String) {
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Coming soon...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}