package com.example.ffridge.data.local.dao

import androidx.room.*
import com.example.ffridge.data.local.entity.RecipeEntity
import com.example.ffridge.data.model.RecipeDifficulty
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity): Long

    @Update
    suspend fun updateRecipe(recipe: RecipeEntity)

    @Delete
    suspend fun deleteRecipe(recipe: RecipeEntity)

    @Query("SELECT * FROM recipes ORDER BY createdAt DESC")
    fun getAllRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeById(id: String): RecipeEntity?

    @Query("SELECT * FROM recipes WHERE difficulty = :difficulty ORDER BY createdAt DESC")
    fun getRecipesByDifficulty(difficulty: RecipeDifficulty): Flow<List<RecipeEntity>>

    @Query("SELECT COUNT(*) FROM recipes")
    fun getRecipeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM recipes WHERE isFavorite = 1")
    fun getFavoriteRecipeCount(): Flow<Int>

    @Query("DELETE FROM recipes")
    suspend fun deleteAllRecipes()
}
