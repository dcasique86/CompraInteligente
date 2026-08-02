package com.example.domain.repository

import com.example.domain.model.ProductPhoto
import kotlinx.coroutines.flow.Flow

interface ImageRepository {
    fun getPhotosForProduct(productId: String): Flow<List<ProductPhoto>>

    suspend fun getPhotosForProductOnce(productId: String): List<ProductPhoto>

    suspend fun savePhoto(photo: ProductPhoto)

    suspend fun savePhotos(photos: List<ProductPhoto>)

    suspend fun updatePhoto(photo: ProductPhoto)

    suspend fun deletePhoto(photoId: String)

    suspend fun deletePhotosForProduct(productId: String)

    fun getPendingSyncPhotos(): Flow<List<ProductPhoto>>

    suspend fun getPendingSyncPhotosOnce(): List<ProductPhoto>

    suspend fun markAsSynced(photoId: String, dateSynced: Long, remoteUrl: String)

    suspend fun markSyncFailed(photoId: String, error: String)

    suspend fun getPendingSyncCount(): Int
}