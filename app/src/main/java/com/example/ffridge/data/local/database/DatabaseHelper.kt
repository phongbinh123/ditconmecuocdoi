package com.example.ffridge.data.local.database

import com.example.ffridge.data.local.dao.IngredientDao
import com.example.ffridge.data.local.dao.RecipeDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow

class DatabaseHelper(
    private val ingredientDao: IngredientDao,
    private val recipeDao: RecipeDao
) {

    suspend fun cleanupExpiredIngredients(): Int {
        val currentTime = System.currentTimeMillis()
        return ingredientDao.deleteExpiredIngredients(currentTime)
    }

    suspend fun getDatabaseStats(): DatabaseStats {
        return DatabaseStats(
            totalIngredients = ingredientDao.getIngredientCount().first(),
            expiredIngredients = ingredientDao.getExpiredIngredientCount(System.currentTimeMillis()).first(),
            totalRecipes = recipeDao.getRecipeCount().first(),
            favoriteRecipes = recipeDao.getFavoriteRecipeCount().first()
        )
    }
}

data class DatabaseStats(
    val totalIngredients: Int,
    val expiredIngredients: Int,
    val totalRecipes: Int,
    val favoriteRecipes: Int
)
