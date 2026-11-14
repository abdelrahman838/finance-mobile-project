package com.example.mobilefinance.data.repository

import com.example.mobilefinance.data.model.TransactionItem
import kotlinx.coroutines.flow.Flow

/**
 * What the screens talk to. They don't care if it's Room, a fake list or an API.
 */
interface TransactionRepository {
    fun observeTransactions(): Flow<List<TransactionItem>>
}
