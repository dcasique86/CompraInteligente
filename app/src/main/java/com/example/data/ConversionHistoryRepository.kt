package com.example.data

import kotlinx.coroutines.flow.Flow

class ConversionHistoryRepository(private val dao: ConversionHistoryDao) {

    val allHistory: Flow<List<ConversionHistory>> = dao.getAllHistory()

    suspend fun insert(history: ConversionHistory) {
        dao.insertHistory(history)
    }

    suspend fun deleteById(id: Long) {
        dao.deleteHistoryById(id)
    }

    suspend fun clearAll() {
        dao.clearHistory()
    }
}
