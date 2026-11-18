package com.example.mobilefinance.data.repository

import com.example.mobilefinance.data.local.dao.TransactionDao
import com.example.mobilefinance.data.local.dao.UserDao
import com.example.mobilefinance.data.local.entity.Transaction
import com.example.mobilefinance.data.local.entity.TransactionCategory
import com.example.mobilefinance.data.local.entity.TransactionType as EntityTransactionType
import com.example.mobilefinance.data.local.entity.User
import com.example.mobilefinance.data.mapper.RoomTransactionMapper
import com.example.mobilefinance.data.mapper.TransactionMapper
import com.example.mobilefinance.data.model.TransactionItem
import com.example.mobilefinance.util.DAY_MILLIS
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RoomTransactionRepository(
    private val transactionDao: TransactionDao,
    private val userDao: UserDao,
    private val mapper: TransactionMapper<Transaction> = RoomTransactionMapper
) : TransactionRepository {

    override fun observeTransactions(): Flow<List<TransactionItem>> =
        transactionDao.getAllTransactions().map { list -> list.map(mapper::map) }

    override fun observeTransactionsForUser(userId: Int): Flow<List<TransactionItem>> =
        transactionDao.getAllTransactionsForUser(userId.toLong()).map { list -> list.map(mapper::map) }

    /** Puts rows for users in an empty db so screens aren't blank while testing. */
    suspend fun seedIfEmpty() {
        if (transactionDao.count() > 0) return

        val alexId = userDao.registerUser(User(name = "Alex", username = "alex", hashedPassword = "pass_alex"))
        val sarahId = userDao.registerUser(User(name = "Sarah", username = "sarah", hashedPassword = "pass_sarah"))

        transactionDao.insertTransactions(
            listOf(
                // User 1 (Alex) Transactions
                Transaction(userId = alexId, type = EntityTransactionType.INCOME, amount = 3200.00, title = "Salary", description = "Salary", date = daysAgo(1), time = "09:00", category = TransactionCategory.MORE),
                Transaction(userId = alexId, type = EntityTransactionType.EXPENSE, amount = 84.20, title = "Whole Foods Market", description = "Groceries", date = daysAgo(0), time = "14:40", category = TransactionCategory.FOOD),
                Transaction(userId = alexId, type = EntityTransactionType.EXPENSE, amount = 18.50, title = "Uber Trip Downtown", description = "Transport", date = daysAgo(0), time = "11:15", category = TransactionCategory.TRANSPORT),
                Transaction(userId = alexId, type = EntityTransactionType.EXPENSE, amount = 62.00, title = "Electric Bill", description = "Bills", date = daysAgo(1), time = "18:30", category = TransactionCategory.BILLS),

                // User 2 (Sarah) Transactions
                Transaction(userId = sarahId, type = EntityTransactionType.INCOME, amount = 1500.00, title = "Freelance Project", description = "Freelance", date = daysAgo(2), time = "10:00", category = TransactionCategory.MORE),
                Transaction(userId = sarahId, type = EntityTransactionType.EXPENSE, amount = 129.90, title = "H&M - Fall Fit", description = "Shopping", date = daysAgo(3), time = "16:20", category = TransactionCategory.SHOPPING),
                Transaction(userId = sarahId, type = EntityTransactionType.EXPENSE, amount = 24.00, title = "Cinema - Dune 2", description = "Entertainment", date = daysAgo(1), time = "21:00", category = TransactionCategory.FUN),
                Transaction(userId = sarahId, type = EntityTransactionType.EXPENSE, amount = 6.40, title = "Blue Bottle Coffee", description = "Food", date = daysAgo(4), time = "08:10", category = TransactionCategory.FOOD)
            )
        )
    }

    private fun daysAgo(days: Int): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US)
            .format(Date(System.currentTimeMillis() - days * DAY_MILLIS))
}