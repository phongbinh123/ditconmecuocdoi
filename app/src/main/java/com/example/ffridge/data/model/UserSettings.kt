package com.example.ffridge.data.model

data class UserSettings(
    val expiryNotifications: Boolean = true,
    val theme: AppTheme = AppTheme.FROST,
    val uiScale: Float = 1.0f,
    val notificationTime: String = "09:00"
)

enum class AppTheme {
    FROST, MIDNIGHT
}
