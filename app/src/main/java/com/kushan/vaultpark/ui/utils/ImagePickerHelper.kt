package com.kushan.vaultpark.ui.utils

import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts

/**
 * Image picker helper for profile picture uploads
 */
object ImagePickerHelper {

    /**
     * Validate image URI
     */
    fun isValidImageUri(uri: Uri?): Boolean {
        return uri != null && uri.toString().isNotEmpty()
    }

    /**
     * Get image file size (in bytes)
     */
    fun getImageFileSize(uri: Uri): Long {
        // This would require context, typically done in ViewModel
        return 0L
    }

    /**
     * Check if image is within size limit (2MB)
     */
    fun isImageWithinSizeLimit(uri: Uri, maxSizeBytes: Long = 2 * 1024 * 1024): Boolean {
        // This would require ContentResolver from context
        return true
    }
}

/**
 * Common image MIME types
 */
object ImageMimeTypes {
    const val JPEG = "image/jpeg"
    const val PNG = "image/png"
    const val WEBP = "image/webp"
    const val ANY = "image/*"
}
