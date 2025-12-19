package com.example.ffridge.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class Ingredient(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val quantity: String,
    val unit: String,
    val category: String,
    val expiryDate: Long? = null,
    val addedDate: Long = System.currentTimeMillis(),
    val notes: String? = null,
    val imageUrl: String? = null,
    val calories: Int? = null  // ADD THIS LINE
) : Parcelable

enum class IngredientCategory(val displayName: String, val icon: String) {
    DAIRY("Dairy", "🥛"),
    MEAT("Meat", "🥩"),
    PANTRY("Pantry", "📦"),
    FROZEN("Frozen", "❄️"),
    VEGETABLES("Vegetables", "🥬"),
    FRUITS("Fruits", "🍎"),
    OTHER("Other", "⭕")
}
