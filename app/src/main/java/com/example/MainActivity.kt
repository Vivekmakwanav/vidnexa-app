package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.api.RetrofitClient
import com.example.data.database.AppDatabase
import com.example.data.download.DownloadManager
import com.example.data.repository.VidnexaRepository
import com.example.data.utils.SettingsManager
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.VidnexaViewModel
import com.example.ui.viewmodel.VidnexaViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // --- Core Clean Architecture Dependency Injection ---
        val settingsManager = SettingsManager(applicationContext)
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = VidnexaRepository(
            api = RetrofitClient.api,
            downloadDao = database.downloadDao(),
            favoriteDao = database.favoriteDao()
        )
        val downloadManager = DownloadManager(applicationContext, repository)

        // ViewModel Factory Binding
        val viewModelFactory = VidnexaViewModelFactory(repository, downloadManager, settingsManager)
        val viewModel = ViewModelProvider(this, viewModelFactory)[VidnexaViewModel::class.java]

        setContent {
            val useDarkTheme by viewModel.useDarkTheme.collectAsStateWithLifecycle()

            MyApplicationTheme(darkTheme = useDarkTheme) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                // Screens where the bottom navigation bar is shown
                val mainScreens = listOf("home", "history", "favorites", "settings", "help")
                val showBottomBar = currentDestination?.route in mainScreens

                val activePillColor = if (useDarkTheme) CleanPillActiveDark else CleanPillActive
                val activeContentColor = if (useDarkTheme) TextDarkMain else CharcoalDark
                val inactiveContentColor = if (useDarkTheme) TextDarkSecondary else CharcoalLight

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar(
                                containerColor = if (useDarkTheme) CleanSurfaceDark else CleanNavigationBg,
                                tonalElevation = 0.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("system_navigation_bar")
                            ) {
                                NavigationBarItem(
                                    selected = currentDestination?.hierarchy?.any { it.route == "home" } == true,
                                    onClick = {
                                        navController.navigate("home") {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                    label = { Text("Home", style = MaterialTheme.typography.labelSmall) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = activeContentColor,
                                        selectedTextColor = activeContentColor,
                                        indicatorColor = activePillColor,
                                        unselectedIconColor = inactiveContentColor,
                                        unselectedTextColor = inactiveContentColor
                                    ),
                                    modifier = Modifier.testTag("nav_home_tab")
                                )

                                NavigationBarItem(
                                    selected = currentDestination?.hierarchy?.any { it.route == "history" } == true,
                                    onClick = {
                                        navController.navigate("history") {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Download, contentDescription = "Download Log") },
                                    label = { Text("Log", style = MaterialTheme.typography.labelSmall) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = activeContentColor,
                                        selectedTextColor = activeContentColor,
                                        indicatorColor = activePillColor,
                                        unselectedIconColor = inactiveContentColor,
                                        unselectedTextColor = inactiveContentColor
                                    ),
                                    modifier = Modifier.testTag("nav_history_tab")
                                )

                                NavigationBarItem(
                                    selected = currentDestination?.hierarchy?.any { it.route == "favorites" } == true,
                                    onClick = {
                                        navController.navigate("favorites") {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Star, contentDescription = "Bookmarks") },
                                    label = { Text("Bookmarks", style = MaterialTheme.typography.labelSmall) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = activeContentColor,
                                        selectedTextColor = activeContentColor,
                                        indicatorColor = activePillColor,
                                        unselectedIconColor = inactiveContentColor,
                                        unselectedTextColor = inactiveContentColor
                                    ),
                                    modifier = Modifier.testTag("nav_favorites_tab")
                                )

                                NavigationBarItem(
                                    selected = currentDestination?.hierarchy?.any { it.route == "settings" } == true,
                                    onClick = {
                                        navController.navigate("settings") {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                    label = { Text("Settings", style = MaterialTheme.typography.labelSmall) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = activeContentColor,
                                        selectedTextColor = activeContentColor,
                                        indicatorColor = activePillColor,
                                        unselectedIconColor = inactiveContentColor,
                                        unselectedTextColor = inactiveContentColor
                                    ),
                                    modifier = Modifier.testTag("nav_settings_tab")
                                )

                                NavigationBarItem(
                                    selected = currentDestination?.hierarchy?.any { it.route == "help" } == true,
                                    onClick = {
                                        navController.navigate("help") {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Help, contentDescription = "Help") },
                                    label = { Text("Help", style = MaterialTheme.typography.labelSmall) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = activeContentColor,
                                        selectedTextColor = activeContentColor,
                                        indicatorColor = activePillColor,
                                        unselectedIconColor = inactiveContentColor,
                                        unselectedTextColor = inactiveContentColor
                                    ),
                                    modifier = Modifier.testTag("nav_help_tab")
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        composable("home") {
                            HomeScreen(viewModel = viewModel, navController = navController)
                        }
                        composable("download") {
                            DownloadScreen(viewModel = viewModel, navController = navController)
                        }
                        composable("history") {
                            HistoryScreen(viewModel = viewModel, navController = navController)
                        }
                        composable("favorites") {
                            FavoritesScreen(viewModel = viewModel, navController = navController)
                        }
                        composable("settings") {
                            SettingsScreen(viewModel = viewModel, navController = navController)
                        }
                        composable("help") {
                            HelpScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }
}
