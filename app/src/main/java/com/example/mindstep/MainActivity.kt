package com.example.mindstep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mindstep.screens.ConfigScreen
import com.example.mindstep.screens.HistoryScreen
import com.example.mindstep.screens.HomeScreen
import com.example.mindstep.screens.NewEntryScreen
import com.example.mindstep.ui.theme.MindStepTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MindStepTheme {
                MindStepApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun MindStepApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            MainDestinations.entries.forEach { dest ->
                val selected = currentRoute == dest.route
                item(
                    icon = {
                        Icon(
                            dest.icon,
                            contentDescription = dest.label
                        )
                    },
                    label = { Text(dest.label) },
                    selected = selected,
                    onClick = {
                        if (currentRoute == AppScreen.NewEntry.route) {
                            navController.popBackStack()
                        }
                        navController.navigate(dest.route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            // on the back stack as users select items
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when
                            // reselecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            floatingActionButton = {
                if (currentRoute != AppScreen.NewEntry.route) {
                    FloatingActionButton(onClick = { navController.navigate(AppScreen.NewEntry.route) }) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = MainDestinations.DASHBOARD.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(MainDestinations.DASHBOARD.route) { HomeScreen() }
                composable(MainDestinations.HISTORY.route) { HistoryScreen() }
                composable(MainDestinations.CONFIG.route) { ConfigScreen() }
                composable(AppScreen.NewEntry.route) {
                    NewEntryScreen(
                        onSaveSuccess = {
                            val returnedToDashboard = navController.popBackStack(
                                MainDestinations.DASHBOARD.route,
                                inclusive = false
                            )
                            if (!returnedToDashboard) {
                                navController.navigate(MainDestinations.DASHBOARD.route) {
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

enum class MainDestinations(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    DASHBOARD("dashboard", "Dashboard", Icons.Default.Home),
    HISTORY("history","Histórico", Icons.Default.History),
    CONFIG("config","Config", Icons.Default.Settings),
}

sealed class AppScreen(val route: String) {
    object NewEntry : AppScreen("new_entry")
}
