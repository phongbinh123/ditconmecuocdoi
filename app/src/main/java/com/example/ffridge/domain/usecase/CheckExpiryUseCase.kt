package com.example.ffridge.domain.usecase

import com.example.ffridge.data.model.Ingredient
import com.example.ffridge.data.repository.IngredientRepository
import com.example.ffridge.domain.model.ExpiryStatus
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.abs

class CheckExpiryUseCase(
    private val repository: IngredientRepository
) {
    fun getExpiringIngredients(): Flow<List<Ingredient>> {
        return repository.getExpiringSoonIngredients()
    }

    fun getExpiredIngredients(): Flow<List<Ingredient>> {
        return repository.getExpiredIngredients()
    }

    fun checkExpiryStatus(ingredient: Ingredient): ExpiryStatus {
        if (ingredient.expiryDate == null) {
            return ExpiryStatus.NoExpiry
        }

        val expiryLocalDate = Instant.ofEpochMilli(ingredient.expiryDate).atZone(ZoneId.systemDefault()).toLocalDate()
        val currentLocalDate = LocalDate.now(ZoneId.systemDefault())

        val daysUntilExpiry = ChronoUnit.DAYS.between(currentLocalDate, expiryLocalDate).toInt()

        return when {
            daysUntilExpiry < 0 -> ExpiryStatus.Expired(abs(daysUntilExpiry))
            daysUntilExpiry == 0 -> ExpiryStatus.ExpiringToday
            daysUntilExpiry <= 3 -> ExpiryStatus.ExpiringSoon(daysUntilExpiry)
            daysUntilExpiry <= 7 -> ExpiryStatus.ExpiringThisWeek(daysUntilExpiry)
            else -> ExpiryStatus.Fresh(daysUntilExpiry)
        }
    }
}
