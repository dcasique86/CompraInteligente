package com.example.domain.repository

import com.example.domain.model.ProductOpportunity
import kotlinx.coroutines.flow.Flow

/**
 * Contract used by the UI. A Firestore implementation can replace the local implementation
 * without changing screens or view models.
 */
interface ProductRepository {
    fun observeProducts(): Flow<List<ProductOpportunity>>
    suspend fun addProduct(product: ProductOpportunity)
}
