package com.example.ffridge.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    object Inventory : BottomNavItem(
        route = "inventory",
        title = "Fridge",
        icon = Icons.Outlined.Kitchen,  // Changed from KitchenOutlined
        selectedIcon = Icons.Filled.Kitchen
    )

    object Add : BottomNavItem(
        route = "add",
        title = "Add",
        icon = Icons.Outlined.AddCircle,
        selectedIcon = Icons.Filled.AddCircle
    )

    object Recipes : BottomNavItem(
        route = "recipes",
        title = "Cook",
        icon = Icons.Outlined.Restaurant,  // Changed from RestaurantMenuOutlined
        selectedIcon = Icons.Filled.Restaurant
    )

    object Chat : BottomNavItem(
        route = "chat",
        title = "Chef",
        icon = Icons.Outlined.ChatBubble,
        selectedIcon = Icons.Filled.ChatBubble
    )
}

val bottomNavItems = listOf(
    BottomNavItem.Inventory,
    BottomNavItem.Add,
    BottomNavItem.Recipes,
    BottomNavItem.Chat
)
