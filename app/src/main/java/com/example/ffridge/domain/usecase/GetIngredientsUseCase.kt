package com.example.ffridge.domain.usecase

import com.example.ffridge.data.model.Ingredient
import com.example.ffridge.data.model.IngredientCategory
import com.example.ffridge.data.repository.IngredientRepository
import kotlinx.coroutines.flow.Flow

class GetIngredientsUseCase(
    private val repository: IngredientRepository
) {

    operator fun invoke(): Flow<List<Ingredient>> {
        return repository.getAllIngredients()
    }

    fun byCategory(category: IngredientCategory): Flow<List<Ingredient>> {
        return repository.getIngredientsByCategory(category)
    }
}
