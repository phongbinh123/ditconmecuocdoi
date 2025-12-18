package com.example.ffridge.util

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object DateUtils {

    // Date format constants
    const val DATE_FORMAT_DISPLAY = "dd MMM yyyy"
    const val DATE_FORMAT_STORAGE = "yyyy-MM-dd"
    const val DATE_FORMAT_FULL = "dd MMMM yyyy, HH:mm"

    /**
     * Format timestamp to display format
     */
    fun formatDateDisplay(timestamp: Long): String {
        val sdf = SimpleDateFormat(DATE_FORMAT_DISPLAY, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    /**
     * Format timestamp to storage format
     */
    fun formatDateStorage(timestamp: Long): String {
        val sdf = SimpleDateFormat(DATE_FORMAT_STORAGE, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    /**
     * Format timestamp to full format
     */
    fun formatDateFull(timestamp: Long): String {
        val sdf = SimpleDateFormat(DATE_FORMAT_FULL, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    /**
     * Get days until expiry
     */
    fun getDaysUntilExpiry(expiryDate: Long): Long {
        val now = System.currentTimeMillis()
        val diff = expiryDate - now
        return TimeUnit.MILLISECONDS.toDays(diff)
    }

    /**
     * Check if ingredient is expired
     */
    fun isExpired(expiryDate: Long): Boolean {
        return expiryDate < System.currentTimeMillis()
    }

    /**
     * Check if ingredient is expiring soon
     */
    fun isExpiringSoon(expiryDate: Long, warningDays: Int = 3): Boolean {
        val daysUntil = getDaysUntilExpiry(expiryDate)
        return daysUntil in 0..warningDays.toLong()
    }

    /**
     * Get relative time string (e.g., "2 days ago", "in 3 days")
     */
    fun getRelativeTimeString(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = timestamp - now
        val absDiff = Math.abs(diff)

        return when {
            absDiff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            absDiff < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(absDiff)
                if (diff > 0) "in $minutes min" else "$minutes min ago"
            }
            absDiff < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(absDiff)
                if (diff > 0) "in $hours hours" else "$hours hours ago"
            }
            absDiff < TimeUnit.DAYS.toMillis(7) -> {
                val days = TimeUnit.MILLISECONDS.toDays(absDiff)
                if (diff > 0) "in $days days" else "$days days ago"
            }
            else -> formatDateDisplay(timestamp)
        }
    }

    /**
     * Get expiry status color
     */
    fun getExpiryStatusColor(expiryDate: Long): Long {
        val days = getDaysUntilExpiry(expiryDate)
        return when {
            days < 0 -> 0xFFEF4444  // Red - Expired
            days <= 1 -> 0xFFEF4444  // Red - Critical
            days <= 3 -> 0xFFF59E0B  // Orange - Warning
            else -> 0xFF10B981      // Green - Fresh
        }
    }

    /**
     * Get expiry status text
     */
    fun getExpiryStatusText(expiryDate: Long): String {
        val days = getDaysUntilExpiry(expiryDate)
        return when {
            days < 0 -> "Expired ${Math.abs(days)} days ago"
            days == 0L -> "Expires today"
            days == 1L -> "Expires tomorrow"
            days <= 3 -> "Expires in $days days"
            else -> "Fresh"
        }
    }

    /**
     * Add days to current time
     */
    fun addDays(days: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, days)
        return calendar.timeInMillis
    }

    /**
     * Get start of day
     */
    fun getStartOfDay(timestamp: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * Get end of day
     */
    fun getEndOfDay(timestamp: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
}
