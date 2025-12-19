package com.example.ffridge.ui.screens.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ffridge.data.model.Ingredient
import com.example.ffridge.data.repository.RepositoryProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class InventoryUiState(
    val ingredients: List<Ingredient> = emptyList(),
    val filteredIngredients: List<Ingredient> = emptyList(),
    val isLoading: Boolean = true,
    val selectedCategory: String = "All",
    val searchQuery: String = "",
    val sortOption: SortOption = SortOption.DATE_ADDED_DESC,
    val expiringCount: Int = 0,
    val expiredCount: Int = 0
)

enum class SortOption(val displayName: String) {
    NAME_ASC("Name (A-Z)"),
    NAME_DESC("Name (Z-A)"),
    DATE_ADDED_ASC("Oldest First"),
    DATE_ADDED_DESC("Newest First"),
    EXPIRY_ASC("Expiring Soon"),
    EXPIRY_DESC("Expiring Last"),
    QUANTITY_ASC("Quantity (Low to High)"),
    QUANTITY_DESC("Quantity (High to Low)")
}

class InventoryViewModel : ViewModel() {
    private val ingredientRepository = RepositoryProvider.getIngredientRepository()

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    init {
        loadIngredients()
    }

    private fun loadIngredients() {
        viewModelScope.launch {
            ingredientRepository.getAllIngredients().collect { ingredients ->
                _uiState.update { currentState ->
                    val expiringCount = ingredients.count { ingredient ->
                        ingredient.expiryDate?.let { expiryDate ->
                            val daysUntilExpiry = TimeUnit.MILLISECONDS.toDays(
                                expiryDate - System.currentTimeMillis()
                            )
                            daysUntilExpiry in 0..3
                        } ?: false
                    }

                    val expiredCount = ingredients.count { ingredient ->
                        ingredient.expiryDate?.let { expiryDate ->
                            expiryDate < System.currentTimeMillis()
                        } ?: false
                    }

                    currentState.copy(
                        ingredients = ingredients,
                        filteredIngredients = filterAndSortIngredients(
                            ingredients,
                            currentState.selectedCategory,
                            currentState.searchQuery,
                            currentState.sortOption
                        ),
                        isLoading = false,
                        expiringCount = expiringCount,
                        expiredCount = expiredCount
                    )
                }
            }
        }
    }

    fun selectCategory(category: String) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedCategory = category,
                filteredIngredients = filterAndSortIngredients(
                    currentState.ingredients,
                    category,
                    currentState.searchQuery,
                    currentState.sortOption
                )
            )
        }
    }

    fun searchIngredients(query: String) {
        _uiState.update { currentState ->
            currentState.copy(
                searchQuery = query,
                filteredIngredients = filterAndSortIngredients(
                    currentState.ingredients,
                    currentState.selectedCategory,
                    query,
                    currentState.sortOption
                )
            )
        }
    }

    fun setSortOption(sortOption: SortOption) {
        _uiState.update { currentState ->
            currentState.copy(
                sortOption = sortOption,
                filteredIngredients = filterAndSortIngredients(
                    currentState.ingredients,
                    currentState.selectedCategory,
                    currentState.searchQuery,
                    sortOption
                )
            )
        }
    }

    private fun filterAndSortIngredients(
        ingredients: List<Ingredient>,
        category: String,
        query: String,
        sortOption: SortOption
    ): List<Ingredient> {
        var filtered = ingredients

        // Filter by category
        if (category != "All") {
            filtered = filtered.filter { it.category == category }
        }

        // Filter by search query
        if (query.isNotEmpty()) {
            filtered = filtered.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.category.contains(query, ignoreCase = true)
            }
        }

        // Sort
        return when (sortOption) {
            SortOption.NAME_ASC -> filtered.sortedBy { it.name.lowercase() }
            SortOption.NAME_DESC -> filtered.sortedByDescending { it.name.lowercase() }
            SortOption.DATE_ADDED_ASC -> filtered.sortedBy { it.addedDate }
            SortOption.DATE_ADDED_DESC -> filtered.sortedByDescending { it.addedDate }
            SortOption.EXPIRY_ASC -> filtered.sortedBy { it.expiryDate ?: Long.MAX_VALUE }
            SortOption.EXPIRY_DESC -> filtered.sortedByDescending { it.expiryDate ?: Long.MIN_VALUE }
            SortOption.QUANTITY_ASC -> filtered.sortedBy { it.quantity.toDoubleOrNull() ?: 0.0 }
            SortOption.QUANTITY_DESC -> filtered.sortedByDescending { it.quantity.toDoubleOrNull() ?: 0.0 }
        }
    }

    fun deleteIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            ingredientRepository.deleteIngredient(ingredient)
        }
    }
}
