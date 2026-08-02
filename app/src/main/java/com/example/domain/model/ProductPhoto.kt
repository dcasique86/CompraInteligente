package com.example.domain.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "product_photos",
    indices = [
        Index(value = ["productId"]),
        Index(value = ["synced"]),
        Index(value = ["dateCaptured"])
    ]
)
data class ProductPhoto(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val productId: String,
    val localPath: String,
    val thumbnailPath: String?,
    val dateCaptured: Long = System.currentTimeMillis(),
    var synced: Boolean = false,
    var dateSynced: Long? = null,
    var remoteUrl: String? = null,
    val fileSize: Long = 0,
    val width: Int = 0,
    val height: Int = 0,
    val imageHash: String = "",
    var syncRetryCount: Int = 0,
    var lastSyncError: String? = null
) {
    val isPendingSync: Boolean
        get() = !synced
}