package com.example.ffridge.data.remote

import com.example.ffridge.BuildConfig
import com.example.ffridge.data.model.Recipe
import com.example.ffridge.data.model.RecipeDifficulty
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
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
        // Note: systemInstruction is not available in this version
        // Use it in prompts instead
    )

    /**
     * Send chat message with system prompt
     */
    suspend fun sendMessage(
        message: String,
        conversationHistory: List<com.example.ffridge.data.model.ChatMessage> = emptyList()
    ): String {
        return try {
            // Add system instruction to first message
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

    /**
     * Generate recipe from ingredients
     */
    suspend fun generateRecipe(ingredients: List<String>): Recipe {
        val ingredientList = ingredients.joinToString(", ")

        val prompt = """
            Create a delicious recipe using PRIMARILY these ingredients: $ingredientList
            You can assume basic pantry items like: salt, pepper, oil, butter, garlic, onions.
            
            Return the recipe in this EXACT format:
            
            TITLE: [Creative recipe name]
            DESCRIPTION: [1-2 sentence description]
            INGREDIENTS: [ingredient 1], [ingredient 2], [ingredient 3], etc.
            INSTRUCTIONS: [Step 1] | [Step 2] | [Step 3] | etc.
            COOKING_TIME: [total time in minutes as a number]
            DIFFICULTY: [EASY or MEDIUM or HARD]
            
            Make it realistic, include exact measurements, and clear numbered instructions.
        """.trimIndent()

        return try {
            val response = generativeModel.generateContent(prompt)
            val text = response.text ?: throw Exception("Empty response")
            parseRecipe(text, ingredients)
        } catch (e: Exception) {
            // Fallback recipe
            Recipe(
                id = UUID.randomUUID().toString(),
                title = "Simple ${ingredients.firstOrNull() ?: "Ingredient"} Dish",
                description = "A quick recipe using your available ingredients",
                ingredients = ingredients.map { "1 cup $it" },
                instructions = listOf(
                    "Prepare all ingredients",
                    "Cook according to standard methods",
                    "Season to taste",
                    "Serve hot"
                ),
                cookingTime = 30,
                difficulty = RecipeDifficulty.EASY,
                imageUrl = null,
                createdAt = System.currentTimeMillis(),
                isFavorite = false
            )
        }
    }

    private fun parseRecipe(text: String, fallbackIngredients: List<String>): Recipe {
        val lines = text.lines().filter { it.isNotBlank() }
        var title = "Generated Recipe"
        var description = "A delicious recipe created just for you"
        var ingredients = listOf<String>()
        var instructions = listOf<String>()
        var cookingTime = 30
        var difficulty = RecipeDifficulty.MEDIUM

        lines.forEach { line ->
            when {
                line.startsWith("TITLE:", ignoreCase = true) -> {
                    title = line.substringAfter(":").trim()
                }
                line.startsWith("DESCRIPTION:", ignoreCase = true) -> {
                    description = line.substringAfter(":").trim()
                }
                line.startsWith("INGREDIENTS:", ignoreCase = true) -> {
                    val ingredientText = line.substringAfter(":").trim()
                    ingredients = ingredientText.split(",")
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                }
                line.startsWith("INSTRUCTIONS:", ignoreCase = true) -> {
                    val instructionText = line.substringAfter(":").trim()
                    instructions = instructionText.split("|")
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                        .map { it.replaceFirst(Regex("^\\d+\\.?\\s*"), "") }
                }
                line.startsWith("COOKING_TIME:", ignoreCase = true) -> {
                    val timeText = line.substringAfter(":").trim()
                    cookingTime = timeText.filter { it.isDigit() }.toIntOrNull() ?: 30
                }
                line.startsWith("DIFFICULTY:", ignoreCase = true) -> {
                    val diffText = line.substringAfter(":").trim().uppercase()
                    difficulty = try {
                        RecipeDifficulty.valueOf(diffText)
                    } catch (e: Exception) {
                        RecipeDifficulty.MEDIUM
                    }
                }
            }
        }

        if (ingredients.isEmpty()) {
            ingredients = fallbackIngredients.map { "1 cup $it" }
        }

        if (instructions.isEmpty()) {
            instructions = listOf(
                "Prepare and clean all ingredients",
                "Heat a pan over medium heat",
                "Cook ingredients until done",
                "Season to taste and serve"
            )
        }

        return Recipe(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            ingredients = ingredients,
            instructions = instructions,
            cookingTime = cookingTime,
            difficulty = difficulty,
            imageUrl = null,
            createdAt = System.currentTimeMillis(),
            isFavorite = false
        )
    }
}
