package com.example.ffridge.data.repository

import com.example.ffridge.data.local.dao.RecipeDao
import com.example.ffridge.data.local.entity.RecipeEntity
import com.example.ffridge.data.model.Recipe
import com.example.ffridge.data.model.RecipeDifficulty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RecipeRepository(
    private val recipeDao: RecipeDao
) {

    fun getAllRecipes(): Flow<List<Recipe>> {
        return recipeDao.getAllRecipes()
            .map { entities -> entities.map { it.toRecipe() } }
    }

    fun getFavoriteRecipes(): Flow<List<Recipe>> {
        return recipeDao.getFavoriteRecipes()
            .map { entities -> entities.map { it.toRecipe() } }
    }

    suspend fun insertRecipe(recipe: Recipe) {
        recipeDao.insertRecipe(recipe.toEntity())
    }

    suspend fun deleteRecipe(recipe: Recipe) {
        recipeDao.deleteRecipe(recipe.toEntity())
    }

    suspend fun toggleFavorite(recipeId: String) {
        val recipe = recipeDao.getRecipeById(recipeId)
        recipe?.let {
            val updated = it.copy(isFavorite = !it.isFavorite)
            recipeDao.updateRecipe(updated)
        }
    }

    fun getRecipesByDifficulty(difficulty: RecipeDifficulty): Flow<List<Recipe>> {
        return recipeDao.getRecipesByDifficulty(difficulty)
            .map { entities -> entities.map { it.toRecipe() } }
    }

    fun getRecipesByMaxTime(maxTime: Int): Flow<List<Recipe>> {
        return recipeDao.getAllRecipes()
            .map { entities ->
                entities
                    .filter { it.cookingTime <= maxTime }
                    .map { it.toRecipe() }
            }
    }

    fun getQuickRecipes(): Flow<List<Recipe>> {
        return getRecipesByMaxTime(30)
    }

    fun searchRecipes(query: String): Flow<List<Recipe>> {
        return recipeDao.getAllRecipes()
            .map { entities ->
                entities
                    .filter {
                        it.title.contains(query, ignoreCase = true) ||
                                it.description.contains(query, ignoreCase = true)
                    }
                    .map { it.toRecipe() }
            }
    }
}

// Extension functions
private fun RecipeEntity.toRecipe() = Recipe(
    id = id,
    title = title,
    description = description,
    ingredients = ingredients,
    instructions = instructions,
    cookingTime = cookingTime,
    difficulty = RecipeDifficulty.valueOf(difficulty),
    imageUrl = imageUrl,
    createdAt = createdAt,
    isFavorite = isFavorite
)

private fun Recipe.toEntity() = RecipeEntity(
    id = id,
    title = title,
    description = description,
    ingredients = ingredients,
    instructions = instructions,
    cookingTime = cookingTime,
    difficulty = difficulty.name,
    imageUrl = imageUrl,
    createdAt = createdAt,
    isFavorite = isFavorite
)
