package org.delcom.pam_p5_ifs23051.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.InputStream

object ImageCompressHelper {

    private const val MAX_WIDTH = 1080
    private const val MAX_HEIGHT = 1080
    private const val QUALITY = 80 // 80% JPEG quality

    /**
     * Compress an image from Uri and return a MultipartBody.Part ready for upload.
     * - Resizes to max 1080x1080 (maintaining aspect ratio)
     * - Fixes EXIF rotation
     * - Compresses to JPEG at 80% quality
     */
    fun uriToCompressedMultipart(
        context: Context,
        uri: Uri,
        partName: String
    ): MultipartBody.Part {
        val compressedBytes = compressImage(context, uri)
        val requestBody = compressedBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, "image.jpg", requestBody)
    }

    private fun compressImage(context: Context, uri: Uri): ByteArray {
        val inputStream: InputStream = context.contentResolver.openInputStream(uri)
            ?: error("Cannot open URI: $uri")

        // Decode bounds first to avoid OOM
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeStream(inputStream, null, options)
        inputStream.close()

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT)
        options.inJustDecodeBounds = false

        val rawStream: InputStream = context.contentResolver.openInputStream(uri)
            ?: error("Cannot open URI: $uri")
        var bitmap = BitmapFactory.decodeStream(rawStream, null, options)
            ?: error("Failed to decode bitmap")
        rawStream.close()

        // Fix rotation using EXIF
        bitmap = fixRotation(context, uri, bitmap)

        // Scale down if still too large
        bitmap = scaleBitmap(bitmap, MAX_WIDTH, MAX_HEIGHT)

        // Compress to JPEG bytes
        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, output)
        bitmap.recycle()

        return output.toByteArray()
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height, width) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun fixRotation(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            val stream = context.contentResolver.openInputStream(uri) ?: return bitmap
            val exif = ExifInterface(stream)
            stream.close()
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            val rotation = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> return bitmap
            }
            val matrix = Matrix().apply { postRotate(rotation) }
            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (rotated != bitmap) bitmap.recycle()
            rotated
        } catch (e: Exception) {
            bitmap
        }
    }

    private fun scaleBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxWidth && height <= maxHeight) return bitmap

        val scale = minOf(maxWidth.toFloat() / width, maxHeight.toFloat() / height)
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        val scaled = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        if (scaled != bitmap) bitmap.recycle()
        return scaled
    }
}