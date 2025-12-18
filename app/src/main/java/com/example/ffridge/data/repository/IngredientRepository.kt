package com.example.ffridge.data.repository

import com.example.ffridge.data.local.dao.IngredientDao
import com.example.ffridge.data.local.entity.IngredientEntity
import com.example.ffridge.data.model.Ingredient
import com.example.ffridge.data.model.IngredientCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class IngredientRepository(
    private val ingredientDao: IngredientDao
) {

    fun getAllIngredients(): Flow<List<Ingredient>> {
        return ingredientDao.getAllIngredients()
            .map { entities -> entities.map { it.toIngredient() } }
    }

    fun getIngredientsByCategory(category: IngredientCategory): Flow<List<Ingredient>> {
        return ingredientDao.getIngredientsByCategory(category)
            .map { entities -> entities.map { it.toIngredient() } }
    }

    suspend fun insertIngredient(ingredient: Ingredient) {
        ingredientDao.insertIngredient(ingredient.toEntity())
    }

    suspend fun updateIngredient(ingredient: Ingredient) {
        ingredientDao.updateIngredient(ingredient.toEntity())
    }

    suspend fun deleteIngredient(ingredient: Ingredient) {
        ingredientDao.deleteIngredient(ingredient.toEntity())
    }

    fun getExpiredIngredients(): Flow<List<Ingredient>> {
        val currentTime = System.currentTimeMillis()
        return ingredientDao.getExpiredIngredients(currentTime)
            .map { entities -> entities.map { it.toIngredient() } }
    }

    fun getExpiringSoonIngredients(): Flow<List<Ingredient>> {
        val currentTime = System.currentTimeMillis()
        val threeDaysLater = currentTime + (3 * 24 * 60 * 60 * 1000)
        return ingredientDao.getExpiredIngredients(threeDaysLater)
            .map { entities -> entities.map { it.toIngredient() } }
    }
}

// Extension functions
private fun IngredientEntity.toIngredient() = Ingredient(
    id = id,
    name = name,
    quantity = quantity,
    unit = unit,
    category = category,
    expiryDate = expiryDate,
    notes = notes,
    addedDate = addedDate,
    imageUrl = imageUrl
)

private fun Ingredient.toEntity() = IngredientEntity(
    id = id,
    name = name,
    quantity = quantity,
    unit = unit,
    category = category,
    expiryDate = expiryDate,
    notes = notes,
    addedDate = addedDate,
    imageUrl = imageUrl
)
