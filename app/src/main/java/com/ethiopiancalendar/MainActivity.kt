package com.ethiopiancalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ethiopiancalendar.ui.converter.DateConverterScreen
import com.ethiopiancalendar.ui.event.EventScreen
import com.ethiopiancalendar.ui.holidaylist.HolidayListScreen
import com.ethiopiancalendar.ui.month.MonthCalendarScreen
import com.ethiopiancalendar.ui.more.MoreScreen
import com.ethiopiancalendar.ui.more.SettingsScreen
import com.ethiopiancalendar.ui.more.ThemeSettingScreen
import com.ethiopiancalendar.ui.more.ThemeViewModel
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
    val bottomNavItems = getBottomNavItems()

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route || ((currentRoute == "theme" || currentRoute == "settings") && item.route == "more"),
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
            composable("event") {
                EventScreen( "")
            }
            composable("holiday") {
                HolidayListScreen( )
            }
            composable("converter") {
                DateConverterScreen()
            }
            composable("more") {
                MoreScreen(
                    onNavigateToTheme = { navController.navigate("theme") },
                    onNavigateToSettings = { navController.navigate("settings") }
                )
            }
            composable("theme") {
                ThemeSettingScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("settings") {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun getBottomNavItems(): List<BottomNavItem> {
    return listOf(
        BottomNavItem("month", stringResource(R.string.nav_month), Icons.Default.CalendarMonth),
        BottomNavItem("event", stringResource(R.string.nav_today), Icons.Default.Event),
        BottomNavItem("holiday", stringResource(R.string.nav_holiday), Icons.Default.Celebration),
        BottomNavItem("converter", stringResource(R.string.nav_convert), Icons.Default.SwapHoriz),
        BottomNavItem("more", stringResource(R.string.nav_more), Icons.Default.MoreVert)
    )
}
