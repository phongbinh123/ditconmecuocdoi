package com.example.ffridge.ui.screens.recipes

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
            _uiState.update { it.copy(isLoading = true) }

            getRecipesUseCase()
                .catch { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message)
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
                .collect { ingredients ->
                    _uiState.update { it.copy(availableIngredients = ingredients) }
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
            recipeRepository.toggleFavorite(recipeId)
        }
    }

    fun generateRecipes() {
        viewModelScope.launch {
            val ingredients = _uiState.value.availableIngredients
            if (ingredients.isEmpty()) {
                _uiState.update { it.copy(error = "No ingredients available") }
                return@launch
            }

            _uiState.update { it.copy(isGenerating = true, error = null) }

            generateRecipesUseCase(ingredients).fold(
                onSuccess = { recipes ->
                    viewModelScope.launch {
                        recipes.forEach { recipe ->
                            recipeRepository.insertRecipe(recipe)
                        }
                        _uiState.update { it.copy(isGenerating = false) }
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            error = error.message
                        )
                    }
                }
            )
        }
    }

    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch {
            recipeRepository.deleteRecipe(recipe)
        }
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
                val ingredientNames = ingredients.map { it.name.lowercase() }
                filtered.filter { recipe ->
                    recipe.ingredients.any { recipeIngredient ->
                        ingredientNames.any {
                            recipeIngredient.lowercase().contains(it)
                        }
                    }
                }
            }
        }

        // Apply search
        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
            }
        }

        return filtered
    }
}
