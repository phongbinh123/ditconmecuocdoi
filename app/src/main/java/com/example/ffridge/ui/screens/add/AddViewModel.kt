package com.example.ffridge.ui.screens.add

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ffridge.data.model.Ingredient
import com.example.ffridge.data.repository.IngredientRepository
import com.example.ffridge.data.repository.RepositoryProvider
import com.example.ffridge.domain.usecase.AddIngredientUseCase
import com.example.ffridge.domain.usecase.UpdateIngredientUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class AddUiState(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val quantity: String = "",
    val unit: String = "pcs",
    val category: String = "OTHER",
    val expiryDate: Long? = null,
    val notes: String = "",
    val calories: String = "",  // NEW: Added calories
    val imageUri: Uri? = null,  // NEW: Added image URI
    val isEditMode: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val showSuccess: Boolean = false
)

class AddViewModel : ViewModel() {

    private val ingredientRepository: IngredientRepository =
        RepositoryProvider.getIngredientRepository()

    private val addIngredientUseCase = AddIngredientUseCase(ingredientRepository)
    private val updateIngredientUseCase = UpdateIngredientUseCase(ingredientRepository)

    private val _uiState = MutableStateFlow(AddUiState())
    val uiState: StateFlow<AddUiState> = _uiState.asStateFlow()

    fun loadIngredient(ingredient: Ingredient) {
        _uiState.update {
            AddUiState(
                id = ingredient.id,
                name = ingredient.name,
                quantity = ingredient.quantity,
                unit = ingredient.unit,
                category = ingredient.category,
                expiryDate = ingredient.expiryDate,
                notes = ingredient.notes ?: "",
                calories = ingredient.calories?.toString() ?: "",  // NEW
                imageUri = ingredient.imageUrl?.let { Uri.parse(it) },  // NEW
                isEditMode = true
            )
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name, error = null) }
    }

    fun updateQuantity(quantity: String) {
        _uiState.update { it.copy(quantity = quantity, error = null) }
    }

    fun updateUnit(unit: String) {
        _uiState.update { it.copy(unit = unit) }
    }

    fun updateCategory(category: String) {
        _uiState.update { it.copy(category = category) }
    }

    fun updateExpiryDate(date: Long?) {
        _uiState.update { it.copy(expiryDate = date) }
    }

    fun updateNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    // NEW: Update calories
    fun updateCalories(calories: String) {
        _uiState.update { it.copy(calories = calories, error = null) }
    }

    // NEW: Update image
    fun updateImage(uri: Uri?) {
        _uiState.update { it.copy(imageUri = uri) }
    }

    fun saveIngredient(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value

            // Validation
            if (state.name.isBlank()) {
                _uiState.update { it.copy(error = "Please enter ingredient name") }
                return@launch
            }
            if (state.quantity.isBlank()) {
                _uiState.update { it.copy(error = "Please enter quantity") }
                return@launch
            }

            _uiState.update { it.copy(isSaving = true, error = null) }

            val ingredient = Ingredient(
                id = state.id,
                name = state.name,
                quantity = state.quantity,
                unit = state.unit,
                category = state.category,
                expiryDate = state.expiryDate,
                addedDate = System.currentTimeMillis(),
                notes = state.notes.takeIf { it.isNotBlank() },
                imageUrl = state.imageUri?.toString(),  // NEW: Save image URL
                calories = state.calories.toIntOrNull()  // NEW: Save calories
            )

            val result = if (state.isEditMode) {
                updateIngredientUseCase(ingredient)
            } else {
                addIngredientUseCase(ingredient)
            }

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            showSuccess = true
                        )
                    }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = error.message
                        )
                    }
                }
            )
        }
    }

    fun resetForm() {
        _uiState.value = AddUiState()
    }
}
