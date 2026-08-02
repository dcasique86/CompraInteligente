package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.image.ImageStorageManager
import com.example.domain.model.ProductCurrency
import com.example.domain.model.ProductOpportunity
import com.example.domain.model.ProductPhoto
import com.example.domain.repository.ImageRepository
import com.example.domain.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class ProductListViewModel(repository: ProductRepository) : ViewModel() {
    val products: StateFlow<List<ProductOpportunity>> = repository.observeProducts().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )
}

class NewProductViewModel(
    private val repository: ProductRepository,
    private val imageRepository: ImageRepository,
    private val imageStorageManager: ImageStorageManager
) : ViewModel() {

    private val _capturedPhotos = MutableStateFlow<List<ProductPhoto>>(emptyList())
    val capturedPhotos: StateFlow<List<ProductPhoto>> = _capturedPhotos

    fun onPhotoCaptured(uri: android.net.Uri) {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    imageStorageManager.saveImageFromUri(uri)
                }
                
                val photo = ProductPhoto(
                    productId = "", // Will be set when product is saved
                    localPath = result.localPath,
                    thumbnailPath = result.thumbnailPath,
                    fileSize = result.fileSize,
                    width = result.width,
                    height = result.height,
                    imageHash = result.imageHash
                )
                
                _capturedPhotos.value = _capturedPhotos.value + photo
            } catch (e: Exception) {
                // Handle error - could emit to a UI state
                e.printStackTrace()
            }
        }
    }

    fun removeCapturedPhoto(photoId: String) {
        _capturedPhotos.value = _capturedPhotos.value.filter { it.id != photoId }
    }

    fun clearCapturedPhotos() {
        _capturedPhotos.value = emptyList()
    }

    fun save(
        name: String,
        category: String,
        price: Double,
        currency: ProductCurrency,
        store: String,
        notes: String,
        onSaved: (String) -> Unit
    ) {
        viewModelScope.launch {
            val productId = UUID.randomUUID().toString()
            
            // Save photos first with productId
            val photosWithProductId = _capturedPhotos.value.map { photo ->
                ProductPhoto(
                    id = photo.id,
                    productId = productId,
                    localPath = photo.localPath,
                    thumbnailPath = photo.thumbnailPath,
                    dateCaptured = photo.dateCaptured,
                    synced = photo.synced,
                    dateSynced = photo.dateSynced,
                    remoteUrl = photo.remoteUrl,
                    fileSize = photo.fileSize,
                    width = photo.width,
                    height = photo.height,
                    imageHash = photo.imageHash,
                    syncRetryCount = photo.syncRetryCount,
                    lastSyncError = photo.lastSyncError
                )
            }
            
            if (photosWithProductId.isNotEmpty()) {
                imageRepository.savePhotos(photosWithProductId)
            }
            
            // Save product with photo URIs
            val photoUris = photosWithProductId.map { it.localPath }
            
            repository.addProduct(
                ProductOpportunity(
                    id = productId,
                    name = name.trim(),
                    category = category.trim(),
                    price = price,
                    currency = currency,
                    store = store.trim(),
                    notes = notes.trim(),
                    photoUris = photoUris
                )
            )
            
            clearCapturedPhotos()
            onSaved(productId)
        }
    }
}

class ProductViewModelFactory(
    private val repository: ProductRepository,
    private val imageRepository: ImageRepository,
    private val imageStorageManager: ImageStorageManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(ProductListViewModel::class.java) -> ProductListViewModel(repository) as T
        modelClass.isAssignableFrom(NewProductViewModel::class.java) -> NewProductViewModel(repository, imageRepository, imageStorageManager) as T
        else -> error("Unknown ViewModel: ${modelClass.name}")
    }
}