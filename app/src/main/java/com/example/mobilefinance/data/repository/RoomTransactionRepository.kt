package com.example.mobilefinance.data.repository

import com.example.mobilefinance.data.local.dao.TransactionDao
import com.example.mobilefinance.data.local.dao.UserDao
import com.example.mobilefinance.data.local.entity.Transaction
import com.example.mobilefinance.data.mapper.RoomTransactionMapper
import com.example.mobilefinance.data.mapper.TransactionMapper
import com.example.mobilefinance.data.model.TransactionItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomTransactionRepository(
    private val transactionDao: TransactionDao,
    @Suppress("unused") private val userDao: UserDao, // kept so callers don't have to change
    private val mapper: TransactionMapper<Transaction> = RoomTransactionMapper
) : TransactionRepository {

    override fun observeTransactions(): Flow<List<TransactionItem>> =
        transactionDao.getAllTransactions().map { list -> list.map(mapper::map) }

    override fun observeTransactionsForUser(userId: Int): Flow<List<TransactionItem>> =
        transactionDao.getAllTransactionsForUser(userId.toLong()).map { list -> list.map(mapper::map) }
}
