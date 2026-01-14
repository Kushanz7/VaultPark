package com.kushan.vaultpark.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserUiState(
    val userName: String = "John Doe",
    val email: String = "john.doe@example.com",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class UserViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    fun loadUserData() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        // Simulate loading user data
        _uiState.value = _uiState.value.copy(
            userName = "John Doe",
            email = "john.doe@example.com",
            isLoading = false
        )
    }

    fun updateUserName(newName: String) {
        _uiState.value = _uiState.value.copy(userName = newName)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
