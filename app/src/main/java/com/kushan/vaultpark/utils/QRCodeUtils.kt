package com.kushan.vaultpark.utils

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.util.UUID
import kotlin.math.abs

object QRCodeUtils {
    
    /**
     * Generate a secure QR code string with format: PARKELITE|[userId]|[timestamp]|[securityHash]
     */
    fun generateQRCodeString(userId: String, timestamp: Long = System.currentTimeMillis()): String {
        val hash = generateSecurityHash(userId, timestamp)
        return "PARKELITE|$userId|$timestamp|$hash"
    }
    
    /**
     * Generate security hash based on userId and timestamp
     */
    private fun generateSecurityHash(userId: String, timestamp: Long): String {
        val combined = "$userId:$timestamp"
        var hash = 0
        for (c in combined) {
            hash = ((hash shl 5) - hash) + c.code
            hash = hash and hash // Convert to 32-bit integer
        }
        return abs(hash).toString(16).padStart(8, '0')
    }
    
    /**
     * Convert QR code string to bitmap
     */
    fun generateQRCodeBitmap(
        content: String,
        size: Int = 512,
        errorCorrectionLevel: String = "H"
    ): Bitmap? {
        return try {
            val hints = mapOf(
                EncodeHintType.ERROR_CORRECTION to com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.valueOf(errorCorrectionLevel),
                EncodeHintType.MARGIN to 1
            )
            
            val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Validate QR code format
     */
    fun validateQRCodeFormat(qrCode: String): Boolean {
        val parts = qrCode.split("|")
        return parts.size == 4 && parts[0] == "PARKELITE"
    }
    
    /**
     * Extract user ID from QR code string
     */
    fun extractUserIdFromQR(qrCode: String): String? {
        return if (validateQRCodeFormat(qrCode)) {
            qrCode.split("|")[1]
        } else {
            null
        }
    }
    
    /**
     * Extract timestamp from QR code string
     */
    fun extractTimestampFromQR(qrCode: String): Long? {
        return if (validateQRCodeFormat(qrCode)) {
            qrCode.split("|")[2].toLongOrNull()
        } else {
            null
        }
    }
}
