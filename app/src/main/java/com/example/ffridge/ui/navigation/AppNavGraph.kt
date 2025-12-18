package com.example.ffridge.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.ffridge.ui.components.BottomNavItem
import com.example.ffridge.ui.screens.add.AddScreen
import com.example.ffridge.ui.screens.chat.ChatScreen
import com.example.ffridge.ui.screens.inventory.InventoryScreen
import com.example.ffridge.ui.screens.recipes.RecipesScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = BottomNavItem.Inventory.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(BottomNavItem.Inventory.route) {
            InventoryScreen(
                onAddClick = {
                    navController.navigate(BottomNavItem.Add.route)
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
    }
}
