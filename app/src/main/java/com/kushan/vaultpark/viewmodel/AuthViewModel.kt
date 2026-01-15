package com.kushan.vaultpark.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushan.vaultpark.data.repository.AuthRepository
import com.kushan.vaultpark.data.repository.FirestoreRepository
import com.kushan.vaultpark.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {
    
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    init {
        checkExistingSession()
    }
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                // Sign in with Firebase Auth
                val authResult = authRepository.signIn(email, password)
                
                if (authResult.isSuccess) {
                    val userId = authResult.getOrNull()
                    if (userId != null) {
                        // Fetch user data from Firestore
                        val userResult = firestoreRepository.getUserData(userId)
                        
                        if (userResult.isSuccess) {
                            val user = userResult.getOrNull()
                            if (user != null) {
                                _currentUser.value = user
                                _isAuthenticated.value = true
                            } else {
                                // User doesn't exist in Firestore, create one
                                createNewUserInFirestore(userId, email)
                            }
                        } else {
                            // User doesn't exist in Firestore, create one
                            createNewUserInFirestore(userId, email)
                        }
                    } else {
                        _errorMessage.value = "Authentication failed: User ID is null"
                    }
                } else {
                    _errorMessage.value = authResult.exceptionOrNull()?.message ?: "Login failed"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Login error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private suspend fun createNewUserInFirestore(userId: String, email: String) {
        try {
            // Determine user role based on email
            val role = when {
                email.contains("guard", ignoreCase = true) || email.contains("security", ignoreCase = true) -> "SECURITY"
                else -> "DRIVER"
            }
            
            // Extract name from email (e.g., "john.driver" -> "John Driver")
            val nameParts = email.split("@")[0].split(".")
            val name = nameParts.joinToString(" ") { it.capitalize() }
            
            // Create new user document
            val newUser = User(
                id = userId,
                name = name,
                email = email,
                phone = "",
                role = if (role == "SECURITY") com.kushan.vaultpark.model.UserRole.SECURITY else com.kushan.vaultpark.model.UserRole.DRIVER,
                vehicleNumber = if (role == "DRIVER") "" else "",
                membershipType = if (role == "DRIVER") "Standard" else ""
            )
            
            // Save to Firestore
            val saveResult = firestoreRepository.saveUser(newUser)
            
            if (saveResult.isSuccess) {
                _currentUser.value = newUser
                _isAuthenticated.value = true
            } else {
                _errorMessage.value = "Failed to create user profile: ${saveResult.exceptionOrNull()?.message}"
            }
        } catch (e: Exception) {
            _errorMessage.value = "Error creating user: ${e.message}"
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
                _currentUser.value = null
                _isAuthenticated.value = false
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Logout failed: ${e.message}"
            }
        }
    }
    
    private fun checkExistingSession() {
        viewModelScope.launch {
            try {
                val userId = authRepository.getCurrentUserId()
                
                if (userId != null) {
                    // Fetch user data from Firestore
                    val userResult = firestoreRepository.getUserData(userId)
                    
                    if (userResult.isSuccess) {
                        _currentUser.value = userResult.getOrNull()
                        _isAuthenticated.value = true
                    } else {
                        // Session invalid, logout
                        authRepository.signOut()
                        _isAuthenticated.value = false
                    }
                } else {
                    _isAuthenticated.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Session check failed: ${e.message}"
                _isAuthenticated.value = false
            }
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
}

