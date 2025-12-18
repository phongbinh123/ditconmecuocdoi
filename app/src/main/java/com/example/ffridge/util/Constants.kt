package com.example.ffridge.util

object Constants {

    // App Info
    const val APP_NAME = "Ffridge"
    const val APP_VERSION = "1.0.0"

    // Database
    const val DATABASE_NAME = "ffridge_database"
    const val DATABASE_VERSION = 1

    // SharedPreferences
    const val PREFS_NAME = "ffridge_prefs"
    const val PREF_THEME = "app_theme"
    const val PREF_NOTIFICATIONS_ENABLED = "notifications_enabled"
    const val PREF_EXPIRY_REMINDER_DAYS = "expiry_reminder_days"

    // Notifications
    const val NOTIFICATION_CHANNEL_ID = "ffridge_expiry_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Ingredient Expiry"
    const val NOTIFICATION_CHANNEL_DESCRIPTION = "Notifications for expiring ingredients"

    // WorkManager
    const val EXPIRY_CHECK_WORK_NAME = "expiry_check_work"
    const val EXPIRY_CHECK_INTERVAL_HOURS = 24L

    // Common Units
    val COMMON_UNITS = listOf(
        "pcs",      // pieces
        "kg",       // kilograms
        "g",        // grams
        "lb",       // pounds
        "oz",       // ounces
        "L",        // liters
        "mL",       // milliliters
        "cup",
        "tbsp",     // tablespoon
        "tsp",      // teaspoon
        "box",
        "bag",
        "can",
        "bottle",
        "pack"
    )

    // Category Icons - ALL CATEGORIES
    val CATEGORY_ICONS = mapOf(
        "VEGETABLES" to "🥬",
        "FRUITS" to "🍎",
        "DAIRY" to "🥛",
        "MEAT" to "🥩",
        "FISH" to "🐟",
        "GRAINS" to "🌾",
        "BEVERAGES" to "🥤",
        "CONDIMENTS" to "🧂",
        "SNACKS" to "🍪",
        "FROZEN" to "🧊",
        "BAKERY" to "🍞",
        "SPICES" to "🌶️",
        "CANNED" to "🥫",
        "OTHER" to "📦"
    )

    // Date Formats
    const val DATE_FORMAT_DISPLAY = "dd MMM yyyy"
    const val DATE_FORMAT_STORAGE = "yyyy-MM-dd"
    const val DATE_FORMAT_FULL = "dd MMMM yyyy, HH:mm"

    // Expiry Warning
    const val EXPIRY_WARNING_DAYS = 3
    const val EXPIRY_CRITICAL_DAYS = 1

    // Recipe Difficulty Colors
    const val COLOR_DIFFICULTY_EASY = 0xFF10B981
    const val COLOR_DIFFICULTY_MEDIUM = 0xFFF59E0B
    const val COLOR_DIFFICULTY_HARD = 0xFFEF4444

    // Gemini AI
    const val GEMINI_MODEL = "gemini-pro"
    const val GEMINI_MAX_OUTPUT_TOKENS = 2048
    const val GEMINI_TEMPERATURE = 0.7f

    // Validation
    const val MIN_INGREDIENT_NAME_LENGTH = 2
    const val MAX_INGREDIENT_NAME_LENGTH = 100
    const val MIN_QUANTITY = 0.01
    const val MAX_QUANTITY = 9999.99

    // Pagination
    const val DEFAULT_PAGE_SIZE = 20
    const val MAX_PAGE_SIZE = 100
}
