package com.example.ffridge.ui.screens.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ffridge.data.model.Ingredient
import com.example.ffridge.data.repository.IngredientRepository
import com.example.ffridge.data.repository.RepositoryProvider
import com.example.ffridge.domain.usecase.CheckExpiryUseCase
import com.example.ffridge.domain.usecase.DeleteIngredientUseCase
import com.example.ffridge.domain.usecase.GetIngredientsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class InventoryUiState(
    val ingredients: List<Ingredient> = emptyList(),
    val filteredIngredients: List<Ingredient> = emptyList(),
    val selectedCategory: String = "All",
    val searchQuery: String = "",
    val expiringCount: Int = 0,
    val expiredCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

class InventoryViewModel : ViewModel() {

    private val ingredientRepository: IngredientRepository =
        RepositoryProvider.getIngredientRepository()

    private val getIngredientsUseCase = GetIngredientsUseCase(ingredientRepository)
    private val deleteIngredientUseCase = DeleteIngredientUseCase(ingredientRepository)
    private val checkExpiryUseCase = CheckExpiryUseCase(ingredientRepository)

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    init {
        loadIngredients()
        observeExpiringIngredients()
        observeExpiredIngredients()
    }

    private fun loadIngredients() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            getIngredientsUseCase()
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message
                        )
                    }
                }
                .collect { ingredients ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            ingredients = ingredients,
                            filteredIngredients = filterIngredients(
                                ingredients,
                                currentState.selectedCategory,
                                currentState.searchQuery
                            ),
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    private fun observeExpiringIngredients() {
        viewModelScope.launch {
            checkExpiryUseCase.getExpiringIngredients()
                .collect { expiring ->
                    _uiState.update { it.copy(expiringCount = expiring.size) }
                }
        }
    }

    private fun observeExpiredIngredients() {
        viewModelScope.launch {
            checkExpiryUseCase.getExpiredIngredients()
                .collect { expired ->
                    _uiState.update { it.copy(expiredCount = expired.size) }
                }
        }
    }

    fun selectCategory(category: String) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedCategory = category,
                filteredIngredients = filterIngredients(
                    currentState.ingredients,
                    category,
                    currentState.searchQuery
                )
            )
        }
    }

    fun searchIngredients(query: String) {
        _uiState.update { currentState ->
            currentState.copy(
                searchQuery = query,
                filteredIngredients = filterIngredients(
                    currentState.ingredients,
                    currentState.selectedCategory,
                    query
                )
            )
        }
    }

    fun deleteIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            deleteIngredientUseCase(ingredient)
        }
    }

    private fun filterIngredients(
        ingredients: List<Ingredient>,
        category: String,
        query: String
    ): List<Ingredient> {
        var filtered = ingredients

        // Filter by category
        if (category != "All") {
            filtered = filtered.filter { it.category == category }
        }

        // Filter by search query
        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }

        return filtered
    }

    fun getExpiryStatus(ingredient: Ingredient) =
        checkExpiryUseCase.checkExpiryStatus(ingredient)
}
