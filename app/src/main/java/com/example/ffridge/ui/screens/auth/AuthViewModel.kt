package com.example.ffridge.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLogin: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class AuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email, error = null) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password, error = null) }
    }

    fun updateConfirmPassword(password: String) {
        _uiState.update { it.copy(confirmPassword = password, error = null) }
    }

    fun toggleMode() {
        _uiState.update {
            it.copy(
                isLogin = !it.isLogin,
                error = null,
                confirmPassword = ""
            )
        }
    }

    fun authenticate() {
        val state = _uiState.value

        // Validation
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(error = "Please fill all fields") }
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            _uiState.update { it.copy(error = "Invalid email address") }
            return
        }

        if (state.password.length < 6) {
            _uiState.update { it.copy(error = "Password must be at least 6 characters") }
            return
        }

        if (!state.isLogin && state.password != state.confirmPassword) {
            _uiState.update { it.copy(error = "Passwords don't match") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Simulate authentication delay
            delay(1500)

            // Mock authentication - always succeed for demo
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isSuccess = true,
                    error = null
                )
            }
        }
    }

    fun resetPassword() {
        val email = _uiState.value.email

        if (email.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your email") }
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(error = "Invalid email address") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            delay(1000)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Password reset email sent! (Demo mode)"
                )
            }
        }
    }
}
