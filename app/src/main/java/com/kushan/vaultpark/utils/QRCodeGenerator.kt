package com.kushan.vaultpark.utils

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import java.security.MessageDigest

/**
 * QR Code utility for generating parking session QR codes
 * Format: VAULTPARK|{userId}|{timestamp}|{vehicleNumber}|{hash}
 */
object QRCodeGenerator {
    
    private const val QR_CODE_SIZE = 512
    private const val QR_PREFIX = "VAULTPARK"
    
    /**
     * Generate QR code data string
     */
    fun generateQRData(
        userId: String,
        vehicleNumber: String
    ): String {
        val timestamp = System.currentTimeMillis()
        val data = "$QR_PREFIX|$userId|$timestamp|$vehicleNumber"
        val hash = generateHash(data)
        return "$data|$hash"
    }
    
    /**
     * Generate QR code bitmap
     */
    fun generateQRCode(qrData: String): Bitmap? {
        return try {
            val writer = MultiFormatWriter()
            val bitMatrix: BitMatrix = writer.encode(
                qrData,
                BarcodeFormat.QR_CODE,
                QR_CODE_SIZE,
                QR_CODE_SIZE
            )
            
            val bitmap = Bitmap.createBitmap(
                QR_CODE_SIZE,
                QR_CODE_SIZE,
                Bitmap.Config.RGB_565
            )
            
            for (x in 0 until QR_CODE_SIZE) {
                for (y in 0 until QR_CODE_SIZE) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) -0x1 else -0x1000000)
                }
            }
            
            bitmap
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Validate QR code data format
     */
    fun validateQRData(qrData: String): Boolean {
        val parts = qrData.split("|")
        return parts.size == 5 && parts[0] == QR_PREFIX
    }
    
    /**
     * Extract user ID from QR data
     */
    fun extractUserId(qrData: String): String? {
        val parts = qrData.split("|")
        return if (parts.size == 5) parts[1] else null
    }
    
    /**
     * Extract timestamp from QR data
     */
    fun extractTimestamp(qrData: String): Long? {
        val parts = qrData.split("|")
        return if (parts.size == 5) parts[2].toLongOrNull() else null
    }
    
    /**
     * Extract vehicle number from QR data
     */
    fun extractVehicleNumber(qrData: String): String? {
        val parts = qrData.split("|")
        return if (parts.size == 5) parts[3] else null
    }
    
    /**
     * Verify QR code hash
     */
    fun verifyQRHash(qrData: String): Boolean {
        val parts = qrData.split("|")
        if (parts.size != 5) return false
        
        val data = "${parts[0]}|${parts[1]}|${parts[2]}|${parts[3]}"
        val expectedHash = generateHash(data)
        return parts[4] == expectedHash
    }
    
    /**
     * Generate SHA256 hash
     */
    private fun generateHash(data: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(data.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }.take(16)
    }
}
