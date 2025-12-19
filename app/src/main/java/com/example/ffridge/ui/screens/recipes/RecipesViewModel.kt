package com.example.ffridge.ui.screens.recipes

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ffridge.data.model.Ingredient
import com.example.ffridge.data.model.Recipe
import com.example.ffridge.data.model.RecipeDifficulty
import com.example.ffridge.data.remote.GeminiService
import com.example.ffridge.data.repository.IngredientRepository
import com.example.ffridge.data.repository.RecipeRepository
import com.example.ffridge.data.repository.RepositoryProvider
import com.example.ffridge.domain.usecase.GenerateRecipesUseCase
import com.example.ffridge.domain.usecase.GetRecipesUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RecipesUiState(
    val recipes: List<Recipe> = emptyList(),
    val filteredRecipes: List<Recipe> = emptyList(),
    val availableIngredients: List<Ingredient> = emptyList(),
    val selectedFilter: RecipeFilter = RecipeFilter.All,
    val searchQuery: String = "",
    val isGenerating: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class RecipeFilter {
    All, Favorites, Quick, Easy, WithAvailableIngredients
}

class RecipesViewModel : ViewModel() {

    private val recipeRepository: RecipeRepository =
        RepositoryProvider.getRecipeRepository()
    private val ingredientRepository: IngredientRepository =
        RepositoryProvider.getIngredientRepository()
    private val geminiService = GeminiService()

    private val getRecipesUseCase = GetRecipesUseCase(recipeRepository)
    private val generateRecipesUseCase = GenerateRecipesUseCase(geminiService)

    private val _uiState = MutableStateFlow(RecipesUiState())
    val uiState: StateFlow<RecipesUiState> = _uiState.asStateFlow()

    init {
        loadRecipes()
        loadAvailableIngredients()
    }

    private fun loadRecipes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getRecipesUseCase()
                .catch { e ->
                    Log.e("RecipesViewModel", "Error loading recipes: ${e.message}", e)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to load recipes: ${e.message}"
                        )
                    }
                }
                .collect { recipes ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            recipes = recipes,
                            filteredRecipes = filterRecipes(
                                recipes,
                                currentState.selectedFilter,
                                currentState.searchQuery,
                                currentState.availableIngredients
                            ),
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    private fun loadAvailableIngredients() {
        viewModelScope.launch {
            ingredientRepository.getAllIngredients()
                .catch { e ->
                    Log.e("RecipesViewModel", "Error loading ingredients: ${e.message}", e)
                }
                .collect { ingredients ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            availableIngredients = ingredients,
                            filteredRecipes = filterRecipes(
                                currentState.recipes,
                                currentState.selectedFilter,
                                currentState.searchQuery,
                                ingredients
                            )
                        )
                    }
                }
        }
    }

    fun selectFilter(filter: RecipeFilter) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedFilter = filter,
                filteredRecipes = filterRecipes(
                    currentState.recipes,
                    filter,
                    currentState.searchQuery,
                    currentState.availableIngredients
                )
            )
        }
    }

    fun searchRecipes(query: String) {
        _uiState.update { currentState ->
            currentState.copy(
                searchQuery = query,
                filteredRecipes = filterRecipes(
                    currentState.recipes,
                    currentState.selectedFilter,
                    query,
                    currentState.availableIngredients
                )
            )
        }
    }

    fun toggleFavorite(recipeId: String) {
        viewModelScope.launch {
            try {
                recipeRepository.toggleFavorite(recipeId)
                Log.d("RecipesViewModel", "Toggled favorite for recipe: $recipeId")
            } catch (e: Exception) {
                Log.e("RecipesViewModel", "Error toggling favorite: ${e.message}", e)
                _uiState.update {
                    it.copy(error = "Failed to update favorite: ${e.message}")
                }
            }
        }
    }

    fun generateRecipes() {
        viewModelScope.launch {
            val ingredients = _uiState.value.availableIngredients

            if (ingredients.isEmpty()) {
                _uiState.update {
                    it.copy(error = "No ingredients available. Please add ingredients first.")
                }
                return@launch
            }

            Log.d("RecipesViewModel", "Starting recipe generation with ${ingredients.size} ingredients")
            _uiState.update { it.copy(isGenerating = true, error = null) }

            try {
                generateRecipesUseCase(ingredients).fold(
                    onSuccess = { recipes ->
                        Log.d("RecipesViewModel", "Successfully generated ${recipes.size} recipes")

                        // Insert recipes into database
                        recipes.forEach { recipe ->
                            try {
                                recipeRepository.insertRecipe(recipe)
                                Log.d("RecipesViewModel", "Inserted recipe: ${recipe.title}")
                            } catch (e: Exception) {
                                Log.e("RecipesViewModel", "Error inserting recipe: ${e.message}", e)
                            }
                        }

                        _uiState.update {
                            it.copy(
                                isGenerating = false,
                                error = null
                            )
                        }
                    },
                    onFailure = { error ->
                        Log.e("RecipesViewModel", "Recipe generation failed: ${error.message}", error)
                        _uiState.update {
                            it.copy(
                                isGenerating = false,
                                error = "Failed to generate recipes: ${error.message}"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e("RecipesViewModel", "Unexpected error during recipe generation: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        error = "Unexpected error: ${e.message}"
                    )
                }
            }
        }
    }

    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch {
            try {
                recipeRepository.deleteRecipe(recipe)
                Log.d("RecipesViewModel", "Deleted recipe: ${recipe.title}")
            } catch (e: Exception) {
                Log.e("RecipesViewModel", "Error deleting recipe: ${e.message}", e)
                _uiState.update {
                    it.copy(error = "Failed to delete recipe: ${e.message}")
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun filterRecipes(
        recipes: List<Recipe>,
        filter: RecipeFilter,
        query: String,
        ingredients: List<Ingredient>
    ): List<Recipe> {
        var filtered = recipes

        // Apply filter
        filtered = when (filter) {
            RecipeFilter.All -> filtered
            RecipeFilter.Favorites -> filtered.filter { it.isFavorite }
            RecipeFilter.Quick -> filtered.filter { it.cookingTime <= 30 }
            RecipeFilter.Easy -> filtered.filter { it.difficulty == RecipeDifficulty.EASY }
            RecipeFilter.WithAvailableIngredients -> {
                if (ingredients.isEmpty()) {
                    filtered
                } else {
                    val ingredientNames = ingredients.map { it.name.lowercase().trim() }
                    filtered.filter { recipe ->
                        recipe.ingredients.any { recipeIngredient ->
                            val recipeIngredientLower = recipeIngredient.lowercase().trim()
                            ingredientNames.any { ingredientName ->
                                recipeIngredientLower.contains(ingredientName) ||
                                        ingredientName.contains(recipeIngredientLower)
                            }
                        }
                    }
                }
            }
        }

        // Apply search
        if (query.isNotBlank()) {
            val queryLower = query.lowercase().trim()
            filtered = filtered.filter {
                it.title.lowercase().contains(queryLower) ||
                        it.description.lowercase().contains(queryLower) ||
                        it.ingredients.any { ingredient ->
                            ingredient.lowercase().contains(queryLower)
                        }
            }
        }

        return filtered
    }
}
