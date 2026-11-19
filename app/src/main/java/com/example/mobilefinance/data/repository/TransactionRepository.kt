package com.example.mobilefinance.data.repository

import com.example.mobilefinance.data.model.TransactionItem
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun observeTransactions(): Flow<List<TransactionItem>>
    fun observeTransactionsForUser(userId: Int): Flow<List<TransactionItem>>
}
