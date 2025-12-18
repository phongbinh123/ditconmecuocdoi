package com.example.ffridge.domain.usecase

import com.example.ffridge.data.model.Recipe
import com.example.ffridge.data.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow

class GetRecipesUseCase(
    private val repository: RecipeRepository
) {
    operator fun invoke(): Flow<List<Recipe>> {
        return repository.getAllRecipes()
    }
}
