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

    override fun observeTransactionsForUser(userId: Int): Flow<List<TransactionItem>> =
        transactionDao.getForUser(userId).map { list -> list.map(mapper::map) }

    /** Puts rows for users in an empty db so screens aren't blank while testing. */
    suspend fun seedIfEmpty() {
        if (transactionDao.count() > 0) return

        val alexId = userDao.insert(User(name = "Alex")).toInt()
        val sarahId = userDao.insert(User(name = "Sarah")).toInt()

        transactionDao.insertAll(
            listOf(
                // User 1 (Alex) Transactions
                Transaction(userId = alexId, amount = 3200.00, description = "Salary"),
                Transaction(userId = alexId, amount = -84.20, description = "Whole Foods Market"),
                Transaction(userId = alexId, amount = -18.50, description = "Uber Trip Downtown"),
                Transaction(userId = alexId, amount = -62.00, description = "Electric Bill"),

                // User 2 (Sarah) Transactions
                Transaction(userId = sarahId, amount = 1500.00, description = "Freelance Project"),
                Transaction(userId = sarahId, amount = -129.90, description = "H&M - Fall Fit"),
                Transaction(userId = sarahId, amount = -24.00, description = "Cinema - Dune 2"),
                Transaction(userId = sarahId, amount = -6.40, description = "Blue Bottle Coffee")
            )
        )
    }
}
