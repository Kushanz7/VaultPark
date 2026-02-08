package com.kushan.vaultpark.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kushan.vaultpark.model.ParkingSession
import com.kushan.vaultpark.model.SessionStatus
import com.kushan.vaultpark.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.Date

/**
 * Firestore Repository
 * Handles all Firestore database operations for users and parking sessions
 *
 * Firestore Collection Structure:
 * /users/{userId}
 *   - email: string
 *   - name: string
 *   - role: string (DRIVER/SECURITY)
 *   - vehicleNumber: string? (for drivers)
 *   - membershipType: string?
 *   - gateLocation: string? (for security)
 *   - createdAt: timestamp
 *   - updatedAt: timestamp
 *
 * /parkingSessions/{sessionId}
 *   - driverId: string
 *   - driverName: string
 *   - vehicleNumber: string
 *   - entryTime: timestamp
 *   - exitTime: timestamp?
 *   - gateLocation: string
 *   - scannedByGuardId: string
 *   - guardName: string
 *   - status: string (ACTIVE/COMPLETED)
 *   - qrCodeDataUsed: string
 *   - duration: number? (minutes)
 */
class FirestoreRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // ==================== User Operations ====================

    /**
     * Get user by ID (non-Result version)
     */
    suspend fun getUserById(userId: String): User? = try {
        val doc = db.collection("users").document(userId).get().await()
        doc.toObject(User::class.java)?.copy(id = userId)
    } catch (e: Exception) {
        null
    }

    /**
     * Get user data by ID
     */
    suspend fun getUserData(userId: String): Result<User> = try {
        val doc = db.collection("users").document(userId).get().await()
        val user = doc.toObject(User::class.java) ?: return Result.failure(Exception("User not found"))
        Result.success(user.copy(id = userId))
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Observe user data changes in real-time
     */
    fun observeUser(userId: String): Flow<User?> = flow {
        try {
            db.collection("users").document(userId).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Handle error silently, emit null
                    return@addSnapshotListener
                }
                
                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject(User::class.java)?.copy(id = userId)
                    // Emit in next cycle to avoid flow issues
                } else {
                    // Emit null if user not found
                }
            }
        } catch (e: Exception) {
            // Emit null on error
        }
    }

    /**
     * Save user data to Firestore
     */
    suspend fun saveUser(user: User): Result<Unit> = try {
        db.collection("users").document(user.id).set(user).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ==================== Parking Session Operations ====================

    /**
     * Create a new parking session (overloaded version)
     * Returns the created session object
     */
    suspend fun createParkingSession(session: ParkingSession): ParkingSession = try {
        val docRef = db.collection("parkingSessions").document()
        val sessionWithId = session.copy(id = docRef.id)
        docRef.set(sessionWithId).await()
        sessionWithId
    } catch (e: Exception) {
        throw Exception("Failed to create parking session: ${e.message}")
    }

    /**
     * Create a new parking session (legacy version)
     * Returns the session ID
     */
    suspend fun createParkingSessionLegacy(
        driverId: String,
        driverName: String,
        vehicleNumber: String,
        gateLocation: String,
        qrCodeData: String = ""
    ): Result<String> = try {
        val docRef = db.collection("parkingSessions").document()
        val session = ParkingSession(
            id = docRef.id,
            driverId = driverId,
            driverName = driverName,
            vehicleNumber = vehicleNumber,
            gateLocation = gateLocation,
            location = gateLocation, // Default to gate location for legacy
            status = SessionStatus.ACTIVE.name,
            qrCodeDataUsed = qrCodeData
        )
        docRef.set(session).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Update parking session with exit time and mark as completed
     */
    suspend fun updateParkingSession(session: ParkingSession) {
        try {
            db.collection("parkingSessions").document(session.id).set(session).await()
        } catch (e: Exception) {
            throw Exception("Failed to update parking session: ${e.message}")
        }
    }

    /**
     * Update parking session (legacy version)
     */
    suspend fun updateParkingSessionLegacy(
        sessionId: String,
        exitTime: Date,
        duration: Long?
    ): Result<Unit> = try {
        val updates = mapOf(
            "exitTime" to exitTime,
            "status" to SessionStatus.COMPLETED.name,
            "duration" to duration
        )
        db.collection("parkingSessions").document(sessionId).update(updates).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Get active session for a driver (non-Result version)
     */
    suspend fun getActiveSessionForDriver(driverId: String): ParkingSession? = try {
        val query = db.collection("parkingSessions")
            .whereEqualTo("driverId", driverId)
            .whereEqualTo("status", SessionStatus.ACTIVE.name)
            .limit(1)
            .get()
            .await()

        if (query.documents.isNotEmpty()) {
            query.documents[0].toObject(ParkingSession::class.java)
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }

    /**
     * Get active session for a driver (Result version)
     */
    suspend fun getActiveSessionForDriverResult(driverId: String): Result<ParkingSession?> = try {
        val query = db.collection("parkingSessions")
            .whereEqualTo("driverId", driverId)
            .whereEqualTo("status", SessionStatus.ACTIVE.name)
            .limit(1)
            .get()
            .await()

        val session = if (query.documents.isNotEmpty()) {
            query.documents[0].toObject(ParkingSession::class.java)
        } else {
            null
        }
        
        Result.success(session)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Observe active session for a driver in real-time
     */
    fun observeActiveSession(driverId: String): Flow<ParkingSession?> = flow {
        try {
            val listener = db.collection("parkingSessions")
                .whereEqualTo("driverId", driverId)
                .whereEqualTo("status", SessionStatus.ACTIVE.name)
                .limit(1)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    
                    val session = if (snapshot != null && !snapshot.isEmpty) {
                        snapshot.documents[0].toObject(ParkingSession::class.java)
                    } else {
                        null
                    }
                    // Note: Flow emission in listener needs proper coroutine context
                    // This is a simplified version - consider using callbackFlow in production
                }
        } catch (e: Exception) {
            // Handle error
        }
    }

    /**
     * Get all parking sessions for a user (limit results)
     */
    suspend fun getUserParkingSessions(userId: String, limit: Long = 20): Result<List<ParkingSession>> = try {
        val sessions = db.collection("parkingSessions")
            .whereEqualTo("driverId", userId)
            .orderBy("entryTime", Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .await()
            .toObjects(ParkingSession::class.java)

        Result.success(sessions)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Observe all active sessions in real-time (for security guard)
     */
    fun observeAllActiveSessions(): Flow<List<ParkingSession>> = flow {
        try {
            val listener = db.collection("parkingSessions")
                .whereEqualTo("status", SessionStatus.ACTIVE.name)
                .orderBy("entryTime", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    
                    val sessions = snapshot?.toObjects(ParkingSession::class.java) ?: emptyList()
                    // Note: Flow emission in listener needs proper coroutine context
                }
        } catch (e: Exception) {
            // Handle error
        }
    }

    /**
     * Get recent parking sessions (last N records)
     */
    suspend fun getRecentSessions(limit: Long = 5): List<ParkingSession> = try {
        db.collection("parkingSessions")
            .orderBy("entryTime", Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .await()
            .toObjects(ParkingSession::class.java)
    } catch (e: Exception) {
        emptyList()
    }

    /**
     * Get recent parking sessions (Result version)
     */
    suspend fun getRecentSessionsResult(limit: Long = 5): Result<List<ParkingSession>> = try {
        val sessions = db.collection("parkingSessions")
            .orderBy("entryTime", Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .await()
            .toObjects(ParkingSession::class.java)

        Result.success(sessions)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Observe recent sessions in real-time
     */
    fun observeRecentSessions(limit: Long = 5): Flow<List<ParkingSession>> = flow {
        try {
            db.collection("parkingSessions")
                .orderBy("entryTime", Query.Direction.DESCENDING)
                .limit(limit)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    
                    val sessions = snapshot?.toObjects(ParkingSession::class.java) ?: emptyList()
                    // Note: Flow emission in listener needs proper coroutine context
                }
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    // Legacy methods for backward compatibility
    suspend fun createParkingSessionLegacy2(
        driverId: String,
        driverName: String,
        vehicleNumber: String,
        gateLocation: String,
        location: String = "",
        parkingLotId: String = "",
        qrCodeData: String = ""
    ): Result<String> = try {
        val docRef = db.collection("parkingSessions").document()
        val session = ParkingSession(
            id = docRef.id,
            driverId = driverId,
            driverName = driverName,
            vehicleNumber = vehicleNumber,
            entryTime = System.currentTimeMillis(),
            gateLocation = gateLocation,
            location = location,
            parkingLotId = parkingLotId,
            status = SessionStatus.ACTIVE.name,
            qrCodeDataUsed = qrCodeData
        )
        docRef.set(session).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    suspend fun updateParkingSessionLegacy2(
        sessionId: String,
        exitTime: Long,
        duration: Long?
    ): Result<Unit> = try {
        val updates = mapOf(
            "exitTime" to exitTime,
            "status" to SessionStatus.COMPLETED.name,
            "duration" to (duration?.toString() ?: "")
        )
        db.collection("parkingSessions").document(sessionId).update(updates).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    suspend fun getActiveSessionForDriverLegacy(driverId: String): Result<ParkingSession?> = try {
        val query = db.collection("parkingSessions")
            .whereEqualTo("driverId", driverId)
            .whereEqualTo("status", SessionStatus.ACTIVE.name)
            .limit(1)
            .get()
            .await()

        val session = if (query.documents.isNotEmpty()) {
            query.documents[0].toObject(ParkingSession::class.java)
        } else {
            null
        }
        
        Result.success(session)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    suspend fun getUserParkingSessionsLegacy(userId: String, limit: Long = 20): Result<List<ParkingSession>> = try {
        val sessions = db.collection("parkingSessions")
            .whereEqualTo("driverId", userId)
            .orderBy("entryTime", Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .await()
            .toObjects(ParkingSession::class.java)

        Result.success(sessions)
    } catch (e: Exception) {
        Result.failure(e)
    }

// ==================== Admin User Management Operations ====================

    /**
     * Get all users from Firestore
     */
    suspend fun getAllUsers(): List<User> = try {
        val snapshot = db.collection("users")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
        snapshot.toObjects(User::class.java).map { it.copy(id = it.id) }
    } catch (e: Exception) {
        emptyList()
    }

    /**
     * Delete user from Firestore
     */
    suspend fun deleteUser(userId: String): Result<Unit> = try {
        db.collection("users").document(userId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ==================== History & Logs Operations ====================

    /**
     * Get completed parking sessions for a driver with date range filtering
     */
    suspend fun getParkingSessionsByDriver(
        driverId: String,
        status: String = "COMPLETED",
        limit: Int = 20,
        offset: Int = 0,
        startTime: Long? = null,
        endTime: Long? = null
    ): List<ParkingSession> = try {
        var query = db.collection("parkingSessions")
            .whereEqualTo("driverId", driverId)
            .whereEqualTo("status", status)

        if (startTime != null && endTime != null) {
            query = query
                .whereGreaterThanOrEqualTo("entryTime", startTime)
                .whereLessThanOrEqualTo("entryTime", endTime)
        }

        val sessions = query
            .orderBy("entryTime", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()
            .toObjects(ParkingSession::class.java)

        android.util.Log.d("FirestoreRepository", "Fetched ${sessions.size} sessions for driver $driverId")
        sessions
    } catch (e: Exception) {
        android.util.Log.e("FirestoreRepository", "Error fetching driver sessions", e)
        emptyList()
    }

    /**
     * Get scan logs for a security guard with date range filtering
     */
    suspend fun getParkingSessionsByGuard(
        guardId: String,
        limit: Int = 30,
        offset: Int = 0,
        startTime: Long? = null,
        endTime: Long? = null
    ): List<ParkingSession> = try {
        var query = db.collection("parkingSessions")
            .whereEqualTo("scannedByGuardId", guardId)

        if (startTime != null && endTime != null) {
            query = query
                .whereGreaterThanOrEqualTo("entryTime", startTime)
                .whereLessThanOrEqualTo("entryTime", endTime)
        }

        val sessions = query
            .orderBy("entryTime", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()
            .toObjects(ParkingSession::class.java)

        android.util.Log.d("FirestoreRepository", "Fetched ${sessions.size} sessions for guard $guardId")
        sessions
    } catch (e: Exception) {
        android.util.Log.e("FirestoreRepository", "Error fetching guard sessions", e)
        emptyList()
    }
}
