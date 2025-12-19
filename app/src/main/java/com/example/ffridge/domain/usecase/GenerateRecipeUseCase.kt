package com.example.ffridge.domain.usecase

import com.example.ffridge.data.model.Ingredient
import com.example.ffridge.data.model.Recipe
import com.example.ffridge.data.remote.GeminiService

class GenerateRecipesUseCase(private val geminiService: GeminiService) {

    suspend operator fun invoke(ingredients: List<Ingredient>): Result<List<Recipe>> {
        return try {
            val prompt = createRecipePrompt(ingredients)
            val recipes = geminiService.generateRecipes(prompt)

            if (recipes.isEmpty()) {
                Result.failure(Exception("Failed to generate recipes. Please try again."))
            } else {
                Result.success(recipes)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error generating recipes: ${e.message}"))
        }
    }

    private fun createRecipePrompt(ingredients: List<Ingredient>): String {
        val ingredientList = ingredients.joinToString(", ") { it.name }

        return """
Generate exactly 5 unique, delicious recipes using these ingredients: $ingredientList

IMPORTANT: Respond ONLY with a valid JSON array. No markdown, no code blocks, just the JSON array.

Format each recipe exactly like this:
[
  {
    "title": "Recipe Name",
    "description": "A 2-sentence appetizing description",
    "ingredients": ["ingredient 1", "ingredient 2", "ingredient 3"],
    "instructions": ["Step 1", "Step 2", "Step 3"],
    "cookingTime": 30,
    "difficulty": "EASY",
    "imageUrl": "https://images.unsplash.com/photo-recipe-related?w=800"
  }
]

Rules:
1. Use real Unsplash image URLs related to the dish (search for the recipe name)
2. Each recipe must use at least 2 of the provided ingredients
3. Include common pantry items if needed (oil, salt, pepper, etc.)
4. Keep instructions clear and numbered
5. Difficulty must be: EASY, MEDIUM, or HARD
6. Cooking time in minutes (realistic estimate)
7. Make recipes diverse (different cuisines and cooking methods)

Return ONLY the JSON array, nothing else.
        """.trimIndent()
    }
}
