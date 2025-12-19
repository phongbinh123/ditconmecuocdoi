package com.example.ffridge.data.remote

import com.example.ffridge.BuildConfig
import com.example.ffridge.data.model.Recipe
import com.example.ffridge.data.model.RecipeDifficulty
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import org.json.JSONArray
import java.util.UUID

class GeminiService {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.7f
            topK = 40
            topP = 0.95f
            maxOutputTokens = 2048
        }
    )

    suspend fun sendMessage(
        message: String,
        conversationHistory: List<com.example.ffridge.data.model.ChatMessage> = emptyList()
    ): String {
        return try {
            val systemPrompt = """
                You are a professional Sous Chef AI assistant for a smart kitchen app.
                Help users with food storage, cooking times, substitutions, and recipes.
                Be friendly, concise, and helpful. Keep responses under 200 words.
            """.trimIndent()

            val fullMessage = if (conversationHistory.isEmpty()) {
                "$systemPrompt\n\nUser: $message"
            } else {
                message
            }

            val response = generativeModel.generateContent(fullMessage)
            response.text ?: "I'm sorry, I couldn't generate a response. Please try again."
        } catch (e: Exception) {
            throw Exception("Failed to get AI response: ${e.message}")
        }
    }

    suspend fun generateRecipes(prompt: String): List<Recipe> {
        return try {
            val response = generativeModel.generateContent(prompt)
            val text = response.text ?: throw Exception("Empty response")
            parseRecipes(text)
        } catch (e: Exception) {
            // Return an empty list in case of an error
            emptyList()
        }
    }

    private fun parseRecipes(text: String): List<Recipe> {
        val recipes = mutableListOf<Recipe>()
        // Clean the text to make it a valid JSON array
        val jsonArrayStr = text.trim().removePrefix("```json").removeSuffix("```").trim()

        try {
            val jsonArray = JSONArray(jsonArrayStr)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val recipe = Recipe(
                    id = UUID.randomUUID().toString(),
                    title = jsonObject.getString("title"),
                    description = jsonObject.getString("description"),
                    ingredients = jsonObject.getJSONArray("ingredients").let { arr -> List(arr.length()) { arr.getString(it) } },
                    instructions = jsonObject.getJSONArray("instructions").let { arr -> List(arr.length()) { arr.getString(it) } },
                    cookingTime = jsonObject.getInt("cookingTime"),
                    difficulty = RecipeDifficulty.valueOf(jsonObject.getString("difficulty").uppercase()),
                    imageUrl = jsonObject.optString("imageUrl", null),
                    createdAt = System.currentTimeMillis(),
                    isFavorite = false
                )
                recipes.add(recipe)
            }
        } catch (e: Exception) {
            // Handle JSON parsing errors
        }

        return recipes
    }
}
