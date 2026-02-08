package com.kushan.vaultpark.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.kushan.vaultpark.model.User
import com.kushan.vaultpark.model.UserRole
import com.kushan.vaultpark.model.UserPreferences
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    /**
     * Get all users (Drivers and Security) from Firestore
     */
    suspend fun getAllUsers(): Result<List<User>> = try {
        val snapshot = firestore
            .collection("users")
            // Removed role filter to get both DRIVER and SECURITY
            // Removed orderBy to prevent missing index errors until composite index is created
            .get()
            .await()

        val users = snapshot.toObjects(User::class.java).map { it.copy(id = it.id) }
        // specific sort client side
        val sortedUsers = users.sortedByDescending { it.createdAt }
        Result.success(sortedUsers)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Create a new user account (Driver or Security) in Firebase Auth and Firestore
     */
    suspend fun createUserAccount(
        fullName: String,
        email: String,
        phone: String,
        vehicleNumber: String,
        membershipType: String,
        password: String,
        role: UserRole
    ): Result<String> = try {
        // Create user in Firebase Auth
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        val userId = authResult.user?.uid
        
        if (userId != null) {
            // Update display name
            val profileUpdate = UserProfileChangeRequest.Builder()
                .setDisplayName(fullName)
                .build()
            authResult.user?.updateProfile(profileUpdate)?.await()

            // Create user document in Firestore
            val newUser = User(
                id = userId,
                name = fullName,
                email = email,
                phone = phone,
                role = role,
                vehicleNumber = if (role == UserRole.DRIVER) vehicleNumber else "",
                membershipType = if (role == UserRole.DRIVER) membershipType else "",
                preferences = UserPreferences(userId = userId),
                isActive = true
            )

            firestore.collection("users").document(userId).set(newUser).await()
            Result.success(userId)
        } else {
            Result.failure(Exception("Failed to create user account - user ID is null"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Update driver details in Firestore
     */
    suspend fun updateDriverDetails(
        userId: String,
        fullName: String,
        phone: String,
        vehicleNumber: String,
        membershipType: String
    ): Result<Unit> = try {
        val updates = mapOf(
            "name" to fullName,
            "phone" to phone,
            "vehicleNumber" to vehicleNumber,
            "membershipType" to membershipType
        )

        firestore.collection("users").document(userId).update(updates).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Activate a driver account
     * Note: Firebase Auth admin operations require Admin SDK on server side
     * For client-side, we update the Firestore document and handle login logic
     */
    suspend fun activateDriver(userId: String): Result<Unit> = try {
        // Update active status in Firestore
        firestore.collection("users")
            .document(userId)
            .update("isActive", true)
            .await()
        
        // Note: Firebase Auth admin operations would require Admin SDK
        // For now, we handle activation via Firestore status
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Deactivate a driver account
     * Note: Firebase Auth admin operations require Admin SDK on server side
     * For client-side, we update the Firestore document and handle login logic
     */
    suspend fun deactivateDriver(userId: String): Result<Unit> = try {
        // Update active status in Firestore
        firestore.collection("users")
            .document(userId)
            .update("isActive", false)
            .await()
        
        // Note: Firebase Auth admin operations would require Admin SDK
        // For now, we handle deactivation via Firestore status
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Send password reset email to driver
     */
    suspend fun resetDriverPassword(email: String): Result<Unit> = try {
        auth.sendPasswordResetEmail(email).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Delete driver account from Firebase Auth and Firestore
     * Note: Admin SDK required for full deletion from client side
     */
    suspend fun deleteDriverAccount(userId: String, email: String): Result<Unit> = try {
        // Delete from Firestore
        firestore.collection("users").document(userId).delete().await()
        
        // Note: Firebase Auth deletion from client side requires user to be authenticated
        // For admin operations, Admin SDK would be required
        // We'll delete the Firestore record and handle Auth deletion via admin functions
        println("User document deleted from Firestore. Auth deletion requires Admin SDK.")
        
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Get user by ID
     */
    suspend fun getUserById(userId: String): Result<User> = try {
        val document = firestore.collection("users").document(userId).get().await()
        val user = document.toObject(User::class.java)?.copy(id = document.id)
        if (user != null) {
            Result.success(user)
        } else {
            Result.failure(Exception("User not found"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Search users by query (name, email, vehicle number)
     */
    suspend fun searchUsers(query: String): Result<List<User>> = try {
        val lowercaseQuery = query.lowercase()
        
        // Get all users and filter client-side
        val snapshot = firestore.collection("users")
            // Removed role filter
            .get()
            .await()

        val users = snapshot.toObjects(User::class.java)
            .map { it.copy(id = it.id) }
            .filter { user ->
                user.name.lowercase().contains(lowercaseQuery) ||
                user.email.lowercase().contains(lowercaseQuery) ||
                user.vehicleNumber.lowercase().contains(lowercaseQuery)
            }

        Result.success(users)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Get drivers by membership type
     */
    suspend fun getDriversByMembership(membershipType: String): Result<List<User>> = try {
        val snapshot = firestore.collection("users")
            .whereEqualTo("role", UserRole.DRIVER.name)
            .whereEqualTo("membershipType", membershipType)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()

        val drivers = snapshot.toObjects(User::class.java).map { it.copy(id = it.id) }
        Result.success(drivers)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Get drivers by active status
     */
    suspend fun getDriversByStatus(isActive: Boolean): Result<List<User>> = try {
        val snapshot = firestore.collection("users")
            .whereEqualTo("role", UserRole.DRIVER.name)
            .whereEqualTo("isActive", isActive)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()

        val drivers = snapshot.toObjects(User::class.java).map { it.copy(id = it.id) }
        Result.success(drivers)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Get driver statistics
     */
    suspend fun getDriverStatistics(): Result<DriverStatistics> = try {
        val snapshot = firestore.collection("users")
            .whereEqualTo("role", UserRole.DRIVER.name)
            .get()
            .await()

        val drivers = snapshot.toObjects(User::class.java)
        
        val statistics = DriverStatistics(
            totalDrivers = drivers.size,
            activeDrivers = drivers.count { it.isActive ?: true },
            goldMembers = drivers.count { it.membershipType.equals("Gold", ignoreCase = true) },
            platinumMembers = drivers.count { it.membershipType.equals("Platinum", ignoreCase = true) }
        )

        Result.success(statistics)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * Data class for driver statistics
 */
data class DriverStatistics(
    val totalDrivers: Int = 0,
    val activeDrivers: Int = 0,
    val goldMembers: Int = 0,
    val platinumMembers: Int = 0
)