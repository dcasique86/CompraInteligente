package com.example.data.local

import com.example.domain.model.ProductCurrency
import com.example.domain.model.ProductOpportunity
import com.example.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Temporary local source for milestone one. Replace with FirestoreProductRepository when ready. */
class InMemoryProductRepository : ProductRepository {
    private val products = MutableStateFlow(seedProducts)

    override fun observeProducts(): Flow<List<ProductOpportunity>> = products.asStateFlow()

    override suspend fun addProduct(product: ProductOpportunity) {
        products.update { current -> listOf(product) + current }
    }

    private companion object {
        val seedProducts = listOf(
            ProductOpportunity(
                name = "Cargador iPhone 20W Original",
                category = "Cargadores",
                price = 32.0,
                currency = ProductCurrency.USD,
                store = "Tienda de referencia",
                notes = "Precio de ejemplo; agrega tu propia observación."
            ),
            ProductOpportunity(
                name = "Cable USB-C",
                category = "Accesorios",
                price = 12.0,
                currency = ProductCurrency.USD,
                store = "Tienda de referencia"
            )
        )
    }
}
