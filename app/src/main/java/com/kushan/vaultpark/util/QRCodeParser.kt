package com.kushan.vaultpark.util

import android.util.Log
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

data class ParsedQRCode(
    val userId: String,
    val timestamp: Long,
    val vehicleNumber: String,
    val hash: String,
    val isValid: Boolean = true,
    val validationError: String? = null
)

object QRCodeParser {
    private const val TAG = "QRCodeParser"
    private const val PREFIX = "VAULTPARK"
    private const val EXPIRATION_MINUTES = 2L
    
    fun parseQRCode(qrString: String): ParsedQRCode {
        return try {
            val parts = qrString.split("|")
            
            if (parts.size != 5) {
                return ParsedQRCode(
                    userId = "",
                    timestamp = 0,
                    vehicleNumber = "",
                    hash = "",
                    isValid = false,
                    validationError = "Invalid QR code format. Expected 5 parts, got ${parts.size}"
                )
            }
            
            if (parts[0] != PREFIX) {
                return ParsedQRCode(
                    userId = "",
                    timestamp = 0,
                    vehicleNumber = "",
                    hash = "",
                    isValid = false,
                    validationError = "Invalid QR code prefix. Expected '$PREFIX', got '${parts[0]}'"
                )
            }
            
            val userId = parts[1]
            val timestamp = parts[2].toLongOrNull() ?: run {
                return ParsedQRCode(
                    userId = "",
                    timestamp = 0,
                    vehicleNumber = "",
                    hash = "",
                    isValid = false,
                    validationError = "Invalid timestamp format: '${parts[2]}'"
                )
            }
            val vehicleNumber = parts[3]
            val providedHash = parts[4]
            
            if (userId.isBlank()) {
                return ParsedQRCode(
                    userId = "",
                    timestamp = 0,
                    vehicleNumber = "",
                    hash = "",
                    isValid = false,
                    validationError = "User ID is empty"
                )
            }
            
            if (vehicleNumber.isBlank()) {
                return ParsedQRCode(
                    userId = userId,
                    timestamp = timestamp,
                    vehicleNumber = "",
                    hash = providedHash,
                    isValid = false,
                    validationError = "Vehicle number is empty"
                )
            }
            
            // Verify hash
            val dataToHash = "$PREFIX|$userId|$timestamp|$vehicleNumber"
            val calculatedHash = generateHash(dataToHash)
            
            if (calculatedHash != providedHash) {
                return ParsedQRCode(
                    userId = userId,
                    timestamp = timestamp,
                    vehicleNumber = vehicleNumber,
                    hash = providedHash,
                    isValid = false,
                    validationError = "Invalid QR code hash. Possible tampering detected"
                )
            }
            
            ParsedQRCode(
                userId = userId,
                timestamp = timestamp,
                vehicleNumber = vehicleNumber,
                hash = providedHash,
                isValid = true
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing QR code", e)
            ParsedQRCode(
                userId = "",
                timestamp = 0,
                vehicleNumber = "",
                hash = "",
                isValid = false,
                validationError = "Error parsing QR code: ${e.message}"
            )
        }
    }
    
    fun isExpired(parsedQRCode: ParsedQRCode): Boolean {
        if (!parsedQRCode.isValid) return true
        
        val currentTime = System.currentTimeMillis()
        val ageMs = currentTime - parsedQRCode.timestamp
        val ageMinutes = TimeUnit.MILLISECONDS.toMinutes(ageMs)
        
        return ageMinutes > EXPIRATION_MINUTES
    }
    
    fun generateHash(data: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(data.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
    
    fun validateFormat(qrString: String): Pair<Boolean, String?> {
        val parsed = parseQRCode(qrString)
        if (!parsed.isValid) {
            return Pair(false, parsed.validationError)
        }
        
        if (isExpired(parsed)) {
            return Pair(false, "QR code expired. Please request a new one")
        }
        
        return Pair(true, null)
    }
}
