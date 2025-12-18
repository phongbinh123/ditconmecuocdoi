package com.example.ffridge.data.local.dao

import androidx.room.*
import com.example.ffridge.data.local.entity.IngredientEntity
import com.example.ffridge.data.model.IngredientCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: IngredientEntity): Long

    @Update
    suspend fun updateIngredient(ingredient: IngredientEntity)

    @Delete
    suspend fun deleteIngredient(ingredient: IngredientEntity)

    @Query("SELECT * FROM ingredients ORDER BY expiryDate ASC")
    fun getAllIngredients(): Flow<List<IngredientEntity>>

    @Query("SELECT * FROM ingredients WHERE category = :category ORDER BY expiryDate ASC")
    fun getIngredientsByCategory(category: IngredientCategory): Flow<List<IngredientEntity>>

    @Query("SELECT * FROM ingredients WHERE id = :id")
    suspend fun getIngredientById(id: String): IngredientEntity?

    @Query("SELECT * FROM ingredients WHERE expiryDate <= :date ORDER BY expiryDate ASC")
    fun getExpiredIngredients(date: Long): Flow<List<IngredientEntity>>

    @Query("DELETE FROM ingredients WHERE expiryDate <= :date")
    suspend fun deleteExpiredIngredients(date: Long): Int

    @Query("SELECT COUNT(*) FROM ingredients")
    fun getIngredientCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM ingredients WHERE category = :category")
    fun getIngredientCountByCategory(category: IngredientCategory): Flow<Int>

    @Query("SELECT COUNT(*) FROM ingredients WHERE expiryDate <= :date")
    fun getExpiredIngredientCount(date: Long): Flow<Int>

    @Query("DELETE FROM ingredients")
    suspend fun deleteAllIngredients()
}
