package com.kushan.vaultpark.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.kushan.vaultpark.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

/**
 * Firebase Authentication Repository
 * Handles user authentication with Firebase Auth
 *
 * Setup instructions:
 * 1. Go to Firebase Console (https://console.firebase.google.com/)
 * 2. Create a new project or select existing
 * 3. Enable Firebase Authentication > Email/Password
 * 4. Download google-services.json and place in app/ folder
 * 5. Create test users in Firebase Console:
 *    - john.driver@vaultpark.com / Driver123!
 *    - sarah.vip@vaultpark.com / Driver123!
 *    - guard1@vaultpark.com / Guard123!
 *    - guard2@vaultpark.com / Guard123!
 */
class AuthRepository(private val auth: FirebaseAuth = FirebaseAuth.getInstance()) {

    /**
     * Sign in user with email and password
     */
    suspend fun signIn(email: String, password: String): Result<String> = try {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: throw Exception("User ID is null")
        Result.success(uid)
    } catch (e: FirebaseAuthException) {
        Result.failure(handleAuthException(e))
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Sign up new user with email and password
     */
    suspend fun signUp(email: String, password: String): Result<String> = try {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: throw Exception("User ID is null")
        Result.success(uid)
    } catch (e: FirebaseAuthException) {
        Result.failure(handleAuthException(e))
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Sign out current user
     */
    suspend fun signOut(): Result<Unit> = try {
        auth.signOut()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Get current user ID
     */
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    /**
     * Get current user email
     */
    fun getCurrentUserEmail(): String? = auth.currentUser?.email

    /**
     * Observe authentication state changes
     */
    fun observeAuthState(): Flow<Boolean> = flow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            // This will emit true/false based on auth state
        }
        auth.addAuthStateListener(listener)
        
        while (true) {
            emit(auth.currentUser != null)
            kotlinx.coroutines.delay(500)
        }
    }

    /**
     * Check if user is authenticated
     */
    fun isAuthenticated(): Boolean = auth.currentUser != null

    /**
     * Handle Firebase Auth exceptions with user-friendly messages
     */
    private fun handleAuthException(e: FirebaseAuthException): Exception {
        return when (e.errorCode) {
            "ERROR_INVALID_EMAIL" -> Exception("Invalid email format")
            "ERROR_WRONG_PASSWORD" -> Exception("Invalid email or password")
            "ERROR_USER_NOT_FOUND" -> Exception("User not found")
            "ERROR_USER_DISABLED" -> Exception("User account is disabled")
            "ERROR_TOO_MANY_REQUESTS" -> Exception("Too many login attempts. Try again later")
            "ERROR_OPERATION_NOT_ALLOWED" -> Exception("Email/Password accounts are not enabled")
            else -> Exception(e.message ?: "Authentication failed")
        }
    }
}
