package com.example.domain.model

import java.util.UUID

/** A product price spotted in Venezuela, ready to be compared with a Colombian cost later. */
data class ProductOpportunity(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String,
    val price: Double,
    val currency: ProductCurrency,
    val store: String,
    val notes: String = "",
    val photoUris: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
) {
    val primaryPhotoUri: String?
        get() = photoUris.firstOrNull()
}

enum class ProductCurrency(val label: String, val symbol: String) {
    USD("USD", "US$"),
    VES("Bolívares", "Bs.")
}
