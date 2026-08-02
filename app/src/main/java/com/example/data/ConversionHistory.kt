package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversion_history")
data class ConversionHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sourceCurrency: String,
    val sourceAmount: Double,
    val usdAmount: Double,
    val copAmount: Double,
    val vesAmount: Double,
    val copRate: Double,
    val vesRate: Double,
    val timestamp: Long = System.currentTimeMillis()
)
