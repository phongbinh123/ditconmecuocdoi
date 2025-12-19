package com.example.ffridge.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.ffridge.ui.components.BottomNavItem
import com.example.ffridge.ui.screens.add.AddScreen
import com.example.ffridge.ui.screens.auth.AuthScreen
import com.example.ffridge.ui.screens.chat.ChatScreen
import com.example.ffridge.ui.screens.inventory.InventoryScreen
import com.example.ffridge.ui.screens.recipes.RecipesScreen
import com.example.ffridge.ui.screens.settings.SettingsScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = "auth"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable("auth") {
            AuthScreen(
                onLoginSuccess = {
                    navController.navigate(BottomNavItem.Inventory.route) {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }

        composable(BottomNavItem.Inventory.route) {
            InventoryScreen(
                onAddClick = {
                    navController.navigate(BottomNavItem.Add.route)
                },
                onSettingsClick = {
                    navController.navigate("settings")
                }
            )
        }

        composable(BottomNavItem.Add.route) {
            AddScreen(
                onSaveSuccess = {
                    navController.navigate(BottomNavItem.Inventory.route) {
                        popUpTo(BottomNavItem.Inventory.route) { inclusive = false }
                    }
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }

        composable(BottomNavItem.Recipes.route) {
            RecipesScreen()
        }

        composable(BottomNavItem.Chat.route) {
            ChatScreen()
        }

        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate("auth") {
                        popUpTo(BottomNavItem.Inventory.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
