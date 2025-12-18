package com.example.ffridge.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ffridge.data.local.database.FfridgeDatabase
import com.example.ffridge.data.repository.IngredientRepository
import com.example.ffridge.domain.usecase.CheckExpiryUseCase
import kotlinx.coroutines.flow.first

class ExpiryCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val database = FfridgeDatabase.getDatabase(applicationContext)
            val repository = IngredientRepository(database.ingredientDao())
            val checkExpiryUseCase = CheckExpiryUseCase(repository)

            // Get the list of expiring ingredients
            val expiringIngredientsList = checkExpiryUseCase.getExpiringIngredients().first()

            // Send notification if any ingredients are expiring
            if (expiringIngredientsList.isNotEmpty()) {
                NotificationHelper.showExpiryNotification(
                    applicationContext,
                    expiringIngredientsList  // Pass the list of ingredients
                )
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
