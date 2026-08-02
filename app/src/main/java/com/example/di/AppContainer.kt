package com.example.di

import android.content.Context
import com.example.data.AppDatabase
import com.example.data.image.ImageStorageManager
import com.example.data.local.InMemoryProductRepository
import com.example.data.repository.ImageRepositoryImpl
import com.example.data.sync.PhotoSyncService
import com.example.domain.repository.ImageRepository
import com.example.domain.repository.ProductRepository
import com.example.domain.repository.PhotoRepository

object AppContainer {
    @Volatile
    private var _database: AppDatabase? = null
    @Volatile
    private var _imageStorageManager: ImageStorageManager? = null
    @Volatile
    private var _imageRepository: ImageRepository? = null
    @Volatile
    private var _photoSyncService: PhotoSyncService? = null

    fun provideDatabase(context: Context): AppDatabase {
        return _database ?: synchronized(this) {
            _database ?: AppDatabase.getDatabase(context.applicationContext).also { _database = it }
        }
    }

    fun provideImageStorageManager(context: Context): ImageStorageManager {
        return _imageStorageManager ?: synchronized(this) {
            _imageStorageManager ?: ImageStorageManager(context.applicationContext).also { _imageStorageManager = it }
        }
    }

    fun provideImageRepository(context: Context): ImageRepository {
        return _imageRepository ?: synchronized(this) {
            _imageRepository ?: ImageRepositoryImpl(provideDatabase(context).productPhotoDao()).also { _imageRepository = it }
        }
    }

    fun providePhotoSyncService(context: Context): PhotoSyncService {
        return _photoSyncService ?: synchronized(this) {
            _photoSyncService ?: PhotoSyncService(context.applicationContext).also { _photoSyncService = it }
        }
    }

    val productRepository: ProductRepository = InMemoryProductRepository()
}