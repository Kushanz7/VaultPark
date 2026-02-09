package com.kushan.vaultpark.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.kushan.vaultpark.data.local.ThemePreferencesManager
import com.kushan.vaultpark.data.repository.FirestoreRepository
import com.kushan.vaultpark.model.NotificationData
import com.kushan.vaultpark.model.User
import com.kushan.vaultpark.model.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID

data class ProfileUiState(
    val user: User? = null,
    val preferences: UserPreferences? = null,
    val isEditMode: Boolean = false,
    val uploadProgress: Boolean = false,
    val fcmToken: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class ProfileViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _themeMode = MutableStateFlow("SYSTEM")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    init {
        loadUserProfile()
        loadUserPreferences()
        registerFCMToken()
        observeThemePreference()
    }

    private fun observeThemePreference() {
        viewModelScope.launch {
            ThemePreferencesManager.getThemeMode(getApplication()).collect { mode ->
                _themeMode.value = mode
            }
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            ThemePreferencesManager.saveThemeMode(getApplication(), mode)
            _themeMode.value = mode
        }
    }

    /**
     * Load user profile from Firestore
     */
    private fun loadUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                val user = firestoreRepository.getUserById(userId)
                _uiState.value = _uiState.value.copy(user = user)
            } catch (e: Exception) {
                _error.value = "Failed to load profile: ${e.message}"
                Log.e("ProfileViewModel", "Error loading profile", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load user preferences from Firestore
     */
    private fun loadUserPreferences() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                val doc = firestore.collection("userPreferences")
                    .document(userId)
                    .get()
                    .await()

                val preferences = if (doc.exists()) {
                    doc.toObject(UserPreferences::class.java)
                } else {
                    // Create default preferences
                    val defaultPrefs = UserPreferences(userId = userId)
                    firestore.collection("userPreferences")
                        .document(userId)
                        .set(defaultPrefs)
                        .await()
                    defaultPrefs
                }

                _uiState.value = _uiState.value.copy(preferences = preferences)
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error loading preferences", e)
            }
        }
    }

    /**
     * Update user profile information
     */
    fun updateUserProfile(
        name: String,
        phone: String,
        vehicleNumber: String? = null,
        gateLocation: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = auth.currentUser?.uid ?: return@launch

                val updates = mutableMapOf<String, Any?>(
                    "name" to name,
                    "phone" to phone
                )

                if (vehicleNumber != null) {
                    updates["vehicleNumber"] = vehicleNumber
                }
                if (gateLocation != null) {
                    updates["gateLocation"] = gateLocation
                }

                firestore.collection("users")
                    .document(userId)
                    .update(updates)
                    .await()

                // Reload user data
                loadUserProfile()
                _uiState.value = _uiState.value.copy(isEditMode = false)
                _successMessage.value = "Profile updated successfully"
            } catch (e: Exception) {
                _error.value = "Failed to update profile: ${e.message}"
                Log.e("ProfileViewModel", "Error updating profile", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update user preferences
     */
    fun updatePreferences(preferences: UserPreferences) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = auth.currentUser?.uid ?: return@launch

                firestore.collection("userPreferences")
                    .document(userId)
                    .set(preferences)
                    .await()

                _uiState.value = _uiState.value.copy(preferences = preferences)
                _successMessage.value = "Preferences updated"
            } catch (e: Exception) {
                _error.value = "Failed to update preferences: ${e.message}"
                Log.e("ProfileViewModel", "Error updating preferences", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Upload profile picture to Firebase Storage
     */
    fun uploadProfilePicture(imageUri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(uploadProgress = true)
            try {
                val userId = auth.currentUser?.uid ?: return@launch

                val storageRef = storage.reference
                    .child("profile_pictures/$userId.jpg")

                // Upload image
                storageRef.putFile(imageUri).await()

                // Get download URL
                val downloadUrl = storageRef.downloadUrl.await().toString()

                // Update user document
                firestore.collection("users")
                    .document(userId)
                    .update("profileImageUrl", downloadUrl)
                    .await()

                // Reload user data
                loadUserProfile()
                _successMessage.value = "Profile picture updated"
            } catch (e: Exception) {
                _error.value = "Failed to upload picture: ${e.message}"
                Log.e("ProfileViewModel", "Error uploading picture", e)
            } finally {
                _uiState.value = _uiState.value.copy(uploadProgress = false)
            }
        }
    }

    /**
     * Delete profile picture
     */
    fun deleteProfilePicture() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = auth.currentUser?.uid ?: return@launch

                // Delete from storage
                val storageRef = storage.reference
                    .child("profile_pictures/$userId.jpg")
                storageRef.delete().await()

                // Update user document
                firestore.collection("users")
                    .document(userId)
                    .update("profileImageUrl", null)
                    .await()

                // Reload user data
                loadUserProfile()
                _successMessage.value = "Profile picture removed"
            } catch (e: Exception) {
                _error.value = "Failed to delete picture: ${e.message}"
                Log.e("ProfileViewModel", "Error deleting picture", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Change user password
     */
    fun changePassword(oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = auth.currentUser ?: return@launch
                val email = user.email ?: return@launch

                // Re-authenticate with old password
                auth.signInWithEmailAndPassword(email, oldPassword).await()

                // Update password
                user.updatePassword(newPassword).await()

                _successMessage.value = "Password updated successfully"
                _error.value = null
            } catch (e: Exception) {
                _error.value = when {
                    e.message?.contains("wrong password", ignoreCase = true) == true -> 
                        "Current password is incorrect"
                    e.message?.contains("weak password", ignoreCase = true) == true ->
                        "New password is too weak. Use at least 8 characters"
                    else -> "Failed to change password: ${e.message}"
                }
                Log.e("ProfileViewModel", "Error changing password", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Register FCM token for push notifications
     */
    fun registerFCMToken() {
        viewModelScope.launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                val userId = auth.currentUser?.uid ?: return@launch

                // Save token to Firestore
                firestore.collection("users")
                    .document(userId)
                    .update("fcmToken", token)
                    .await()

                _uiState.value = _uiState.value.copy(fcmToken = token)
                Log.d("ProfileViewModel", "FCM Token registered: $token")
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error registering FCM token", e)
            }
        }
    }

    /**
     * Logout user
     */
    fun logout() {
        viewModelScope.launch {
            try {
                auth.signOut()
                _uiState.value = ProfileUiState()
            } catch (e: Exception) {
                _error.value = "Logout failed: ${e.message}"
                Log.e("ProfileViewModel", "Error logging out", e)
            }
        }
    }

    /**
     * Delete user account
     */
    fun deleteAccount() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                val user = auth.currentUser ?: return@launch

                // Delete user document from Firestore
                firestore.collection("users").document(userId).delete().await()

                // Delete preferences
                firestore.collection("userPreferences").document(userId).delete().await()

                // Delete auth account
                user.delete().await()

                _uiState.value = ProfileUiState()
                _successMessage.value = "Account deleted"
            } catch (e: Exception) {
                _error.value = "Failed to delete account: ${e.message}"
                Log.e("ProfileViewModel", "Error deleting account", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Toggle edit mode
     */
    fun toggleEditMode(enable: Boolean) {
        _uiState.value = _uiState.value.copy(isEditMode = enable)
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Clear success message
     */
    fun clearSuccess() {
        _successMessage.value = null
    }
}
