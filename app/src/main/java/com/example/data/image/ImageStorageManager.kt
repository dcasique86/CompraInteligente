package com.example.data.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.example.data.sync.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

class ImageStorageManager(private val context: Context) {

    private val photosDir: File by lazy {
        val dir = File(context.filesDir, "product_photos")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        dir
    }

    private val thumbnailDir: File by lazy {
        val dir = File(context.filesDir, "product_thumbnails")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        dir
    }

    fun generateUniqueFilename(extension: String = "jpg"): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())
        val uuid = UUID.randomUUID().toString().substring(0, 8)
        return "${timestamp}_${uuid}.$extension"
    }

    @Throws(IOException::class)
    fun saveImageFromUri(
        sourceUri: Uri,
        targetWidth: Int = 1920,
        targetHeight: Int = 1920,
        quality: Int = 85
    ): ImageSaveResult {
        val inputStream = context.contentResolver.openInputStream(sourceUri)
            ?: throw IOException("Could not open input stream for URI: $sourceUri")

        try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)

            val originalWidth = options.outWidth
            val originalHeight = options.outHeight

            val rotation = getImageRotation(sourceUri)

            val sampleSize = calculateInSampleSize(options, targetWidth, targetHeight)

            inputStream.close()

            val fullStream = context.contentResolver.openInputStream(sourceUri)
                ?: throw IOException("Could not reopen input stream")

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inMutable = true
            }
            var bitmap = BitmapFactory.decodeStream(fullStream, null, decodeOptions)
                ?: throw IOException("Failed to decode bitmap")
            fullStream.close()

            if (rotation != 0) {
                bitmap = rotateBitmap(bitmap, rotation.toFloat())
            }

            val finalDimensions = if (bitmap.width > targetWidth || bitmap.height > targetHeight) {
                calculateScaledDimension(bitmap.width, bitmap.height, targetWidth, targetHeight)
            } else {
                Pair(bitmap.width, bitmap.height)
            }

            if (bitmap.width != finalDimensions.first || bitmap.height != finalDimensions.second) {
                bitmap = Bitmap.createScaledBitmap(bitmap, finalDimensions.first, finalDimensions.second, true)
            }

            val filename = generateUniqueFilename()
            val file = File(photosDir, filename)

            val outputStream = FileOutputStream(file)
            val compressed = bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputStream.flush()
            outputStream.close()

            if (!compressed) {
                file.delete()
                throw IOException("Failed to compress bitmap")
            }

            val fileSize = file.length()
            val imageHash = calculateHash(file)

            val thumbnailPath = createThumbnail(bitmap, filename)?.absolutePath

            return ImageSaveResult(
                localPath = file.absolutePath,
                thumbnailPath = thumbnailPath,
                fileSize = fileSize,
                width = bitmap.width,
                height = bitmap.height,
                imageHash = imageHash
            )
        } finally {
            inputStream?.close()
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        var inSampleSize = 1
        if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
            val halfHeight = options.outHeight / 2
            val halfWidth = options.outWidth / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun calculateScaledDimension(originalWidth: Int, originalHeight: Int, maxWidth: Int, maxHeight: Int): Pair<Int, Int> {
        val widthRatio = maxWidth.toDouble() / originalWidth
        val heightRatio = maxHeight.toDouble() / originalHeight
        val ratio = minOf(widthRatio, heightRatio)
        return Pair((originalWidth * ratio).toInt(), (originalHeight * ratio).toInt())
    }

    private fun getImageRotation(uri: Uri): Int {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            inputStream?.use { stream ->
                ExifInterface(stream).let { exif ->
                    val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                    when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270
                        else -> 0
                    }
                }
            } ?: 0
        } catch (e: Exception) {
            Timber.w(e, "Could not read EXIF orientation")
            0
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = android.graphics.Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun createThumbnail(bitmap: Bitmap, originalFilename: String): File? {
        return try {
            val thumbSize = 256
            val thumbBitmap = Bitmap.createScaledBitmap(bitmap, thumbSize, thumbSize, true)
            val thumbFile = File(thumbnailDir, "thumb_$originalFilename")
            val outputStream = FileOutputStream(thumbFile)
            val compressed = thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            outputStream.flush()
            outputStream.close()
            if (compressed) thumbFile else null
        } catch (e: Exception) {
            Timber.w(e, "Failed to create thumbnail")
            null
        }
    }

    private fun calculateHash(file: File): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val buffer = ByteArray(8192)
            var inputStream: InputStream = java.io.FileInputStream(file)
            var bytesRead = inputStream.read(buffer)
            while (bytesRead != -1) {
                digest.update(buffer, 0, bytesRead)
                bytesRead = inputStream.read(buffer)
            }
            inputStream.close()
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Timber.w(e, "Failed to calculate hash")
            ""
        }
    }

    fun deleteImage(localPath: String): Boolean {
        return try {
            val file = File(localPath)
            val deleted = file.delete()
            if (deleted) {
                val thumbFile = File(thumbnailDir, "thumb_${file.name}")
                thumbFile.delete()
            }
            deleted
        } catch (e: Exception) {
            false
        }
    }

    fun getThumbnailPath(localPath: String): String? {
        val file = File(localPath)
        val thumbFile = File(thumbnailDir, "thumb_${file.name}")
        return if (thumbFile.exists()) thumbFile.absolutePath else null
    }

    fun getImageUri(localPath: String): Uri {
        val file = File(localPath)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    data class ImageSaveResult(
        val localPath: String,
        val thumbnailPath: String?,
        val fileSize: Long,
        val width: Int,
        val height: Int,
        val imageHash: String
    )
}