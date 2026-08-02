package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.domain.model.ProductPhoto
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductPhotoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: ProductPhoto)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<ProductPhoto>)

    @Update
    suspend fun updatePhoto(photo: ProductPhoto)

    @Query("SELECT * FROM product_photos WHERE productId = :productId ORDER BY dateCaptured DESC")
    fun getPhotosForProduct(productId: String): Flow<List<ProductPhoto>>

    @Query("SELECT * FROM product_photos WHERE productId = :productId ORDER BY dateCaptured DESC")
    suspend fun getPhotosForProductOnce(productId: String): List<ProductPhoto>

    @Query("SELECT * FROM product_photos WHERE id = :photoId")
    suspend fun getPhotoById(photoId: String): ProductPhoto?

    @Query("SELECT * FROM product_photos WHERE synced = 0 ORDER BY dateCaptured ASC")
    fun getPendingSyncPhotos(): Flow<List<ProductPhoto>>

    @Query("SELECT * FROM product_photos WHERE synced = 0 ORDER BY dateCaptured ASC")
    suspend fun getPendingSyncPhotosOnce(): List<ProductPhoto>

    @Query("DELETE FROM product_photos WHERE id = :photoId")
    suspend fun deletePhoto(photoId: String)

    @Query("DELETE FROM product_photos WHERE productId = :productId")
    suspend fun deletePhotosForProduct(productId: String)

    @Query("SELECT COUNT(*) FROM product_photos WHERE synced = 0")
    suspend fun getPendingSyncCount(): Int

    @Query("UPDATE product_photos SET synced = 1, dateSynced = :dateSynced, remoteUrl = :remoteUrl WHERE id = :photoId")
    suspend fun markAsSynced(photoId: String, dateSynced: Long, remoteUrl: String)

    @Query("UPDATE product_photos SET synced = 0, dateSynced = NULL, remoteUrl = NULL, syncRetryCount = syncRetryCount + 1, lastSyncError = :error WHERE id = :photoId")
    suspend fun markSyncFailed(photoId: String, error: String)

    @Query("UPDATE product_photos SET syncRetryCount = 0, lastSyncError = NULL WHERE id = :photoId")
    suspend fun resetSyncRetry(photoId: String)
}