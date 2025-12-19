package com.example.ffridge.data.remote

import android.util.Log
import com.example.ffridge.BuildConfig
import com.example.ffridge.data.model.Recipe
import com.example.ffridge.data.model.RecipeDifficulty
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import org.json.JSONArray
import org.json.JSONException
import java.util.UUID

class GeminiService {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.9f
            topK = 40
            topP = 0.95f
            maxOutputTokens = 8192
        }
    )

    private val chatModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.7f
            topK = 40
            topP = 0.95f
            maxOutputTokens = 2048
        }
    )

    suspend fun sendMessage(
        message: String
    ): String {
        return try {
            val systemPrompt = """
You are a professional Sous Chef AI assistant named Chef Bot.
Your role: Help users with cooking questions, food storage tips, substitutions, and recipe ideas.

Guidelines:
- Be friendly, warm, and encouraging
- Keep responses concise (under 150 words)
- Use cooking emojis when appropriate
- Provide practical, actionable advice
- If you don't know something, admit it honestly

User question: $message
            """.trimIndent()

            val response = chatModel.generateContent(systemPrompt)
            val text = response.text

            if (text.isNullOrBlank()) {
                "I apologize, but I couldn't generate a proper response. Could you please rephrase your question?"
            } else {
                text.trim()
            }
        } catch (e: Exception) {
            Log.e("GeminiService", "Chat error: ${e.message}", e)
            throw Exception("I'm having trouble connecting right now. Please check your internet connection and try again.")
        }
    }

    suspend fun generateRecipes(prompt: String): List<Recipe> {
        return try {
            Log.d("GeminiService", "Generating recipes with prompt...")

            val response = generativeModel.generateContent(prompt)
            val text = response.text

            if (text.isNullOrBlank()) {
                Log.e("GeminiService", "Empty response from Gemini")
                throw Exception("Empty response from AI")
            }

            Log.d("GeminiService", "Raw response: $text")

            val recipes = parseRecipes(text)
            Log.d("GeminiService", "Parsed ${recipes.size} recipes")

            recipes
        } catch (e: Exception) {
            Log.e("GeminiService", "Error generating recipes: ${e.message}", e)
            throw Exception("Failed to generate recipes: ${e.message}")
        }
    }

    private fun parseRecipes(text: String): List<Recipe> {
        val recipes = mutableListOf<Recipe>()

        var jsonText = text.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        val startIndex = jsonText.indexOf('[')
        val endIndex = jsonText.lastIndexOf(']')

        if (startIndex == -1 || endIndex == -1 || startIndex >= endIndex) {
            Log.e("GeminiService", "No valid JSON array found in response")
            throw Exception("Invalid JSON format in response")
        }

        jsonText = jsonText.substring(startIndex, endIndex + 1)

        try {
            val jsonArray = JSONArray(jsonText)
            Log.d("GeminiService", "Found ${jsonArray.length()} recipes in JSON")

            for (i in 0 until jsonArray.length()) {
                try {
                    val jsonObject = jsonArray.getJSONObject(i)

                    val title = jsonObject.optString("title", "Untitled Recipe")
                    val description = jsonObject.optString("description", "Delicious homemade dish")

                    val ingredientsArray = jsonObject.optJSONArray("ingredients")
                    val ingredients = mutableListOf<String>()
                    if (ingredientsArray != null) {
                        for (j in 0 until ingredientsArray.length()) {
                            ingredients.add(ingredientsArray.getString(j))
                        }
                    }

                    val instructionsArray = jsonObject.optJSONArray("instructions")
                    val instructions = mutableListOf<String>()
                    if (instructionsArray != null) {
                        for (j in 0 until instructionsArray.length()) {
                            instructions.add(instructionsArray.getString(j))
                        }
                    }

                    val cookingTime = jsonObject.optInt("cookingTime", 30)
                    val difficultyStr = jsonObject.optString("difficulty", "MEDIUM").uppercase()
                    val difficulty = try {
                        RecipeDifficulty.valueOf(difficultyStr)
                    } catch (_: IllegalArgumentException) {
                        RecipeDifficulty.MEDIUM
                    }

                    var imageUrl = jsonObject.optString("imageUrl", "")
                    if (imageUrl.isBlank() || !imageUrl.startsWith("http")) {
                        val searchQuery = title.replace(" ", "+")
                        imageUrl = "https://source.unsplash.com/800x600/?$searchQuery,food"
                    }

                    val recipe = Recipe(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        description = description,
                        ingredients = ingredients,
                        instructions = instructions,
                        cookingTime = cookingTime,
                        difficulty = difficulty,
                        imageUrl = imageUrl,
                        createdAt = System.currentTimeMillis(),
                        isFavorite = false
                    )

                    recipes.add(recipe)
                    Log.d("GeminiService", "Successfully parsed recipe: $title")

                } catch (e: JSONException) {
                    Log.e("GeminiService", "Error parsing recipe at index $i: ${e.message}")
                }
            }
        } catch (e: JSONException) {
            Log.e("GeminiService", "JSON parsing error: ${e.message}")
            Log.e("GeminiService", "Problematic JSON: $jsonText")
            throw Exception("Failed to parse recipes JSON: ${e.message}")
        }

        return recipes
    }
}
