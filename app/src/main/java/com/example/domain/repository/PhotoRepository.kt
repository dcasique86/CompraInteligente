package com.example.domain.repository

import com.example.domain.model.ProductPhoto
import kotlinx.coroutines.flow.Flow

interface PhotoRepository {
    suspend fun savePhoto(photo: ProductPhoto)
    suspend fun getPhotosForProduct(productId: String): List<ProductPhoto>
    fun observePhotosForProduct(productId: String): Flow<List<ProductPhoto>>
    suspend fun getPhotoById(photoId: String): ProductPhoto?
    suspend fun deletePhoto(photoId: String)
    suspend fun deletePhotosForProduct(productId: String)
    suspend fun getPendingSyncPhotos(): List<ProductPhoto>
    fun observePendingSyncPhotos(): Flow<List<ProductPhoto>>
    suspend fun markPhotoAsSynced(photoId: String, remoteUrl: String)
    suspend fun markPhotoAsPendingSync(photoId: String)
    suspend fun getPendingSyncCount(): Int
}