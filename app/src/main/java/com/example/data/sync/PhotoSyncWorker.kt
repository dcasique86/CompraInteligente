package com.example.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.AppDatabase
import com.example.data.repository.ImageRepositoryImpl
import com.example.domain.model.ProductPhoto
import com.example.domain.repository.ImageRepository
import kotlinx.coroutines.delay
import timber.log.Timber

class PhotoSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val imageRepository: ImageRepository = ImageRepositoryImpl(AppDatabase.getDatabase(context).productPhotoDao())

    override suspend fun doWork(): Result {
        return try {
            val pendingPhotos = imageRepository.getPendingSyncPhotosOnce()
            if (pendingPhotos.isEmpty()) {
                Timber.d("No pending photos to sync")
                Result.success()
            } else {
                Timber.d("Found ${pendingPhotos.size} photos pending sync")
                syncPhotos(pendingPhotos)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error during photo sync")
            Result.retry()
        }
    }

    private suspend fun syncPhotos(photos: List<ProductPhoto>): Result {
        var allSuccess = true
        
        for (photo in photos) {
            val syncResult = syncSinglePhoto(photo)
            if (syncResult != Result.success()) {
                allSuccess = false
            }
            // Small delay between uploads to avoid overwhelming the server
            delay(100)
        }
        
        return if (allSuccess) Result.success() else Result.retry()
    }

    private suspend fun syncSinglePhoto(photo: ProductPhoto): Result {
        return try {
            // TODO: Implement actual upload to API
            // val remoteUrl = uploadToServer(photo.localPath)
            // imageRepository.markAsSynced(photo.id, System.currentTimeMillis(), remoteUrl)
            
            // For now, simulate success
            Timber.d("Would sync photo: ${photo.id} at ${photo.localPath}")
            imageRepository.markAsSynced(photo.id, System.currentTimeMillis(), "https://api.example.com/photos/${photo.id}")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync photo ${photo.id}")
            imageRepository.markSyncFailed(photo.id, e.message ?: "Unknown error")
            Result.retry()
        }
    }

    // Placeholder for actual upload implementation
    private suspend fun uploadToServer(localPath: String): String {
        // TODO: Implement actual multipart upload using Retrofit/OkHttp
        // This will be implemented when the REST API is ready
        delay(500) // Simulate network delay
        return "https://api.example.com/photos/uploaded_${System.currentTimeMillis()}.jpg"
    }
}