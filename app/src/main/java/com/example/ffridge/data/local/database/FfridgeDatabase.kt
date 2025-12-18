package com.example.ffridge.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.ffridge.data.local.dao.ChatMessageDao
import com.example.ffridge.data.local.dao.IngredientDao
import com.example.ffridge.data.local.dao.RecipeDao
import com.example.ffridge.data.local.entity.ChatMessageEntity
import com.example.ffridge.data.local.entity.IngredientEntity
import com.example.ffridge.data.local.entity.RecipeEntity

@Database(
    entities = [
        IngredientEntity::class,
        RecipeEntity::class,
        ChatMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FfridgeDatabase : RoomDatabase() {

    abstract fun ingredientDao(): IngredientDao
    abstract fun recipeDao(): RecipeDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        @Volatile
        private var INSTANCE: FfridgeDatabase? = null

        fun getDatabase(context: Context): FfridgeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FfridgeDatabase::class.java,
                    "ffridge_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
