package com.example.ffridge.domain.usecase

import com.example.ffridge.data.model.Ingredient
import com.example.ffridge.data.model.Recipe
import com.example.ffridge.data.remote.GeminiService

class GenerateRecipesUseCase(private val geminiService: GeminiService) {

    suspend operator fun invoke(ingredients: List<Ingredient>): Result<List<Recipe>> {
        return try {
            val prompt = createRecipePrompt(ingredients)
            val recipes = geminiService.generateRecipes(prompt)
            Result.success(recipes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun createRecipePrompt(ingredients: List<Ingredient>): String {
        val ingredientList = ingredients.joinToString(", ") { it.name }
        return """
        Generate 5 unique recipes based on the following ingredients: $ingredientList.

        For each recipe, provide the following information in JSON format:
        - title: The name of the recipe.
        - description: A short, appealing description of the dish.
        - ingredients: A list of all ingredients required for the recipe.
        - instructions: A step-by-step guide on how to prepare the meal.
        - cookingTime: The total time required to cook, in minutes.
        - difficulty: The difficulty level (e.g., Easy, Medium, Hard).
        - imageUrl: A placeholder image URL for the recipe.
        """
    }
}
