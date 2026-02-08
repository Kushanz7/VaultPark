package com.kushan.vaultpark.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kushan.vaultpark.data.repository.AdminRepository
import com.kushan.vaultpark.model.User
import com.kushan.vaultpark.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminUserManagementUiState(
    val isLoading: Boolean = false,
    val allUsers: List<User> = emptyList(), // Renamed from allDrivers
    val filteredUsers: List<User> = emptyList(), // Renamed from filteredDrivers
    val searchQuery: String = "",
    val selectedFilter: UserFilter = UserFilter.ALL,
    val selectedUser: User? = null, // Renamed from selectedDriver
    val statistics: UserStatistics = UserStatistics(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showAddUserDialog: Boolean = false, // Renamed from showAddDriverDialog
    val showEditUserDialog: Boolean = false, // Renamed from showEditDriverDialog
    val showDeleteConfirmation: Boolean = false
)

enum class UserFilter {
    ALL, DRIVERS, SECURITY, ACTIVE, INACTIVE, GOLD, PLATINUM
}

data class UserStatistics(
    val totalUsers: Int = 0,
    val totalDrivers: Int = 0,
    val totalSecurity: Int = 0,
    val activeUsers: Int = 0,
    val goldMembers: Int = 0,
    val platinumMembers: Int = 0
)

class AdminUserManagementViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val adminRepository: AdminRepository = AdminRepository(auth, firestore)
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUserManagementUiState())
    val uiState: StateFlow<AdminUserManagementUiState> = _uiState.asStateFlow()

    init {
        fetchAllUsers()
    }

    fun fetchAllUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val result = adminRepository.getAllUsers()
                result.fold(
                    onSuccess = { users ->
                        _uiState.update { 
                            it.copy(
                                allUsers = users,
                                isLoading = false 
                            ) 
                        }
                        applyFiltersAndSearch()
                        calculateStatistics()
                    },
                    onFailure = { error ->
                        _uiState.update { 
                            it.copy(
                                errorMessage = "Failed to fetch users: ${error.message}",
                                isLoading = false 
                            ) 
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        errorMessage = "Error fetching users: ${e.message}",
                        isLoading = false 
                    ) 
                }
            }
        }
    }

    fun createNewUser(
        fullName: String,
        email: String,
        phone: String,
        vehicleNumber: String,
        membershipType: String,
        password: String,
        role: UserRole = UserRole.DRIVER
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val result = adminRepository.createUserAccount(
                    fullName = fullName,
                    email = email,
                    phone = phone,
                    vehicleNumber = vehicleNumber,
                    membershipType = membershipType,
                    password = password,
                    role = role
                )
                
                result.fold(
                    onSuccess = {
                        _uiState.update { 
                            it.copy(
                                successMessage = "User account created successfully",
                                showAddUserDialog = false,
                                isLoading = false
                            ) 
                        }
                        fetchAllUsers()
                    },
                    onFailure = { error ->
                        _uiState.update { 
                            it.copy(
                                errorMessage = "Failed to create user: ${error.message}",
                                isLoading = false 
                            ) 
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        errorMessage = "Error creating user: ${e.message}",
                        isLoading = false 
                    ) 
                }
            }
        }
    }

    fun updateDriver(
        userId: String,
        fullName: String,
        phone: String,
        vehicleNumber: String,
        membershipType: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val result = adminRepository.updateDriverDetails(
                    userId = userId,
                    fullName = fullName,
                    phone = phone,
                    vehicleNumber = vehicleNumber,
                    membershipType = membershipType
                )
                
                result.fold(
                    onSuccess = {
                        _uiState.update { 
                            it.copy(
                                successMessage = "User updated successfully",
                                showEditUserDialog = false,
                                isLoading = false
                            ) 
                        }
                        fetchAllUsers()
                    },
                    onFailure = { error ->
                        _uiState.update { 
                            it.copy(
                                errorMessage = "Failed to update user: ${error.message}",
                                isLoading = false 
                            ) 
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        errorMessage = "Error updating user: ${e.message}",
                        isLoading = false 
                    ) 
                }
            }
        }
    }

    fun activateDriver(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val result = adminRepository.activateDriver(userId)
                result.fold(
                    onSuccess = {
                        _uiState.update { 
                            it.copy(
                                successMessage = "User activated successfully",
                                isLoading = false
                            ) 
                        }
                        fetchAllUsers()
                    },
                    onFailure = { error ->
                        _uiState.update { 
                            it.copy(
                                errorMessage = "Failed to activate user: ${error.message}",
                                isLoading = false 
                            ) 
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        errorMessage = "Error activating user: ${e.message}",
                        isLoading = false 
                    ) 
                }
            }
        }
    }

    fun deactivateDriver(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val result = adminRepository.deactivateDriver(userId)
                result.fold(
                    onSuccess = {
                        _uiState.update { 
                            it.copy(
                                successMessage = "User deactivated successfully",
                                isLoading = false
                            ) 
                        }
                        fetchAllUsers()
                    },
                    onFailure = { error ->
                        _uiState.update { 
                            it.copy(
                                errorMessage = "Failed to deactivate user: ${error.message}",
                                isLoading = false 
                            ) 
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        errorMessage = "Error deactivating user: ${e.message}",
                        isLoading = false 
                    ) 
                }
            }
        }
    }

    fun resetDriverPassword(email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val result = adminRepository.resetDriverPassword(email)
                result.fold(
                    onSuccess = {
                        _uiState.update { 
                            it.copy(
                                successMessage = "Password reset email sent successfully",
                                isLoading = false
                            ) 
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { 
                            it.copy(
                                errorMessage = "Failed to send password reset: ${error.message}",
                                isLoading = false 
                            ) 
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        errorMessage = "Error resetting password: ${e.message}",
                        isLoading = false 
                    ) 
                }
            }
        }
    }

    fun deleteDriver(userId: String, email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val result = adminRepository.deleteDriverAccount(userId, email)
                result.fold(
                    onSuccess = {
                        _uiState.update { 
                            it.copy(
                                successMessage = "User account deleted successfully",
                                showDeleteConfirmation = false,
                                isLoading = false
                            ) 
                        }
                        fetchAllUsers()
                    },
                    onFailure = { error ->
                        _uiState.update { 
                            it.copy(
                                errorMessage = "Failed to delete user: ${error.message}",
                                isLoading = false 
                            ) 
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        errorMessage = "Error deleting user: ${e.message}",
                        isLoading = false 
                    ) 
                }
            }
        }
    }

    fun searchDrivers(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFiltersAndSearch()
    }

    fun setFilter(filter: UserFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
        applyFiltersAndSearch()
    }

    fun selectUser(user: User) {
        _uiState.update { it.copy(selectedUser = user) }
    }

    // Dialog state management
    fun showAddUserDialog() {
        _uiState.update { it.copy(showAddUserDialog = true) }
    }

    fun hideAddUserDialog() {
        _uiState.update { it.copy(showAddUserDialog = false) }
    }

    fun showEditUserDialog(user: User) {
        _uiState.update { it.copy(selectedUser = user, showEditUserDialog = true) }
    }

    fun hideEditUserDialog() {
        _uiState.update { it.copy(showEditUserDialog = false, selectedUser = null) }
    }

    fun showDeleteConfirmation(user: User) {
        _uiState.update { it.copy(selectedUser = user, showDeleteConfirmation = true) }
    }

    fun hideDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteConfirmation = false, selectedUser = null) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    private fun applyFiltersAndSearch() {
        val currentState = _uiState.value
        val query = currentState.searchQuery.lowercase().trim()
        val filter = currentState.selectedFilter
        
        var filtered = currentState.allUsers

        // Apply search filter
        if (query.isNotEmpty()) {
            filtered = filtered.filter { user ->
                user.name.lowercase().contains(query) ||
                user.email.lowercase().contains(query) ||
                user.vehicleNumber.lowercase().contains(query)
            }
        }

        // Apply status/membership/role filter
        filtered = when (filter) {
            UserFilter.ALL -> filtered
            UserFilter.DRIVERS -> filtered.filter { it.role == com.kushan.vaultpark.model.UserRole.DRIVER }
            UserFilter.SECURITY -> filtered.filter { it.role == com.kushan.vaultpark.model.UserRole.SECURITY }
            UserFilter.ACTIVE -> filtered.filter { it.isActive == true }
            UserFilter.INACTIVE -> filtered.filter { it.isActive != true }
            UserFilter.GOLD -> filtered.filter { it.membershipType.equals("Gold", ignoreCase = true) }
            UserFilter.PLATINUM -> filtered.filter { it.membershipType.equals("Platinum", ignoreCase = true) }
        }

        _uiState.update { it.copy(filteredUsers = filtered) }
    }

    private fun calculateStatistics() {
        val users = _uiState.value.allUsers
        val stats = UserStatistics(
            totalUsers = users.size,
            totalDrivers = users.count { it.role == com.kushan.vaultpark.model.UserRole.DRIVER },
            totalSecurity = users.count { it.role == com.kushan.vaultpark.model.UserRole.SECURITY },
            activeUsers = users.count { it.isActive == true },
            goldMembers = users.count { it.membershipType.equals("Gold", ignoreCase = true) },
            platinumMembers = users.count { it.membershipType.equals("Platinum", ignoreCase = true) }
        )
        _uiState.update { it.copy(statistics = stats) }
    }
}