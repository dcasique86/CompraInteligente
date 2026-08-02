package com.example.data.repository

import com.example.data.local.ProductPhotoDao
import com.example.domain.model.ProductPhoto
import com.example.domain.repository.ImageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ImageRepositoryImpl(private val photoDao: ProductPhotoDao) : ImageRepository {

    override fun getPhotosForProduct(productId: String): Flow<List<ProductPhoto>> {
        return photoDao.getPhotosForProduct(productId)
    }

    override suspend fun getPhotosForProductOnce(productId: String): List<ProductPhoto> {
        return photoDao.getPhotosForProductOnce(productId)
    }

    override suspend fun savePhoto(photo: ProductPhoto) {
        photoDao.insertPhoto(photo)
    }

    override suspend fun savePhotos(photos: List<ProductPhoto>) {
        photoDao.insertPhotos(photos)
    }

    override suspend fun updatePhoto(photo: ProductPhoto) {
        photoDao.updatePhoto(photo)
    }

    override suspend fun deletePhoto(photoId: String) {
        photoDao.deletePhoto(photoId)
    }

    override suspend fun deletePhotosForProduct(productId: String) {
        photoDao.deletePhotosForProduct(productId)
    }

    override fun getPendingSyncPhotos(): Flow<List<ProductPhoto>> {
        return photoDao.getPendingSyncPhotos()
    }

    override suspend fun getPendingSyncPhotosOnce(): List<ProductPhoto> {
        return photoDao.getPendingSyncPhotosOnce()
    }

    override suspend fun markAsSynced(photoId: String, dateSynced: Long, remoteUrl: String) {
        photoDao.markAsSynced(photoId, dateSynced, remoteUrl)
    }

    override suspend fun markSyncFailed(photoId: String, error: String) {
        photoDao.markSyncFailed(photoId, error)
    }

    override suspend fun getPendingSyncCount(): Int {
        return photoDao.getPendingSyncCount()
    }
}