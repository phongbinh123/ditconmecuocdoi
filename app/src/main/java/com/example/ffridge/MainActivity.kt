package com.example.ffridge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ffridge.data.model.AppTheme
import com.example.ffridge.data.repository.RepositoryProvider
import com.example.ffridge.ui.components.BottomNavBar
import com.example.ffridge.ui.navigation.AppNavGraph
import com.example.ffridge.ui.theme.FfridgeTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MainScreen()
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val userRepository = RepositoryProvider.getUserRepository()

    // Get user settings for theme and scale
    val settings by userRepository.getSettings().collectAsState(
        initial = com.example.ffridge.data.model.UserSettings()
    )

    // Get current route to hide bottom nav on certain screens
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // List of routes where bottom nav should be hidden
    val routesWithoutBottomNav = listOf("auth", "settings")
    val showBottomBar = currentRoute !in routesWithoutBottomNav

    // Apply UI scale
    CompositionLocalProvider(
        LocalDensity provides Density(
            density = LocalDensity.current.density * settings.uiScale,
            fontScale = LocalDensity.current.fontScale * settings.uiScale
        )
    ) {
        FfridgeTheme(appTheme = settings.theme) {
            Scaffold(
                bottomBar = {
                    // Only show bottom bar if not on auth or settings screen
                    if (showBottomBar) {
                        BottomNavBar(navController = navController)
                    }
                }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    AppNavGraph(navController = navController)
                }
            }
        }
    }
}
