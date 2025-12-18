package com.example.ffridge.util

import com.example.ffridge.data.model.Ingredient
import java.util.concurrent.TimeUnit

/**
 * Get days until expiry date
 */
fun Ingredient.getDaysUntil(): Int {
    expiryDate?.let { expiry ->
        val now = System.currentTimeMillis()
        val diff = expiry - now
        return TimeUnit.MILLISECONDS.toDays(diff).toInt()
    }
    return Int.MAX_VALUE
}

/**
 * Check if ingredient is expired
 */
fun Ingredient.isExpired(): Boolean {
    expiryDate?.let { expiry ->
        return expiry < System.currentTimeMillis()
    }
    return false
}

/**
 * Check if ingredient is expiring soon (within 3 days)
 */
fun Ingredient.isExpiringSoon(): Boolean {
    val days = getDaysUntil()
    return days in 0..3
}

/**
 * Get expiry status
 */
fun Ingredient.getExpiryStatus(): ExpiryStatus {
    val days = getDaysUntil()
    return when {
        days < 0 -> ExpiryStatus.EXPIRED
        days == 0 -> ExpiryStatus.EXPIRES_TODAY
        days <= 1 -> ExpiryStatus.CRITICAL
        days <= 3 -> ExpiryStatus.WARNING
        else -> ExpiryStatus.FRESH
    }
}

/**
 * Expiry status enum
 */
enum class ExpiryStatus {
    EXPIRED,
    EXPIRES_TODAY,
    CRITICAL,
    WARNING,
    FRESH
}

/**
 * Format quantity with unit
 */
fun Ingredient.formattedQuantity(): String {
    return "$quantity $unit"
}

/**
 * Get category icon
 */
fun Ingredient.getCategoryIcon(): String {
    return Constants.CATEGORY_ICONS[this.category] ?: "📦"
}
