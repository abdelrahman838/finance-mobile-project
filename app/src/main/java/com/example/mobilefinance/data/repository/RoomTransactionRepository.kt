package com.example.mobilefinance.data.repository

import com.example.financeroom.Transaction
import com.example.financeroom.User
import com.example.mobilefinance.data.local.dao.TransactionDao
import com.example.mobilefinance.data.local.dao.UserDao
import com.example.mobilefinance.data.mapper.RoomTransactionMapper
import com.example.mobilefinance.data.mapper.TransactionMapper
import com.example.mobilefinance.data.model.TransactionItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomTransactionRepository(
    private val transactionDao: TransactionDao,
    private val userDao: UserDao,
    private val mapper: TransactionMapper<Transaction> = RoomTransactionMapper
) : TransactionRepository {

    override fun observeTransactions(): Flow<List<TransactionItem>> =
        transactionDao.getAll().map { list -> list.map(mapper::map) }

    /** Puts a few rows in an empty db so the screen isn't blank while testing. */
    suspend fun seedIfEmpty() {
        if (transactionDao.count() > 0) return
        val userId = userDao.insert(User(name = "Demo")).toInt()
        transactionDao.insertAll(
            listOf(
                Transaction(userId = userId, amount = -6.40, description = "Blue Bottle Coffee"),
                Transaction(userId = userId, amount = -129.90, description = "H&M - Fall Fit"),
                Transaction(userId = userId, amount = -24.00, description = "Cinema - Dune 2"),
                Transaction(userId = userId, amount = -62.00, description = "Electric Bill"),
                Transaction(userId = userId, amount = 3200.00, description = "Salary"),
                Transaction(userId = userId, amount = -18.50, description = "Uber Trip Downtown"),
                Transaction(userId = userId, amount = -84.20, description = "Whole Foods Market")
            )
        )
    }
}
