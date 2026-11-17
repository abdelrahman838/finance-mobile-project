package com.example.mobilefinance.ui.screens.dashboard

import android.content.Context
import com.example.mobilefinance.data.local.AppDatabase
import com.example.mobilefinance.data.local.dao.UserDao
import com.example.mobilefinance.data.model.TransactionItem
import com.example.mobilefinance.data.model.TransactionType
import com.example.mobilefinance.data.repository.TransactionRepository
import com.example.mobilefinance.data.repository.RoomTransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest

/**
 * Reads the dashboard data for one user from Room, through the shared [TransactionRepository].
 */
class RoomDashboardRepository(
    private val transactionRepository: TransactionRepository,
    private val userDao: UserDao,
    private val userId: Long
) : DashboardRepository {

    private val userTransactions: Flow<List<TransactionItem>> =
        transactionRepository.observeTransactionsForUser(userId.toInt())

    // UserDao has no Flow query, so look the name up again whenever the user's rows change.
    // This also picks up the name once the first-launch seed has inserted the user.
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeUserName(): Flow<String> =
        userTransactions
            .mapLatest { userDao.getUserById(userId)?.name.orEmpty() }
            .distinctUntilChanged()

    override fun observeTransactions(): Flow<List<DashboardTransaction>> =
        userTransactions.map { items -> items.mapNotNull { it.toDashboardTransaction() } }

    // Rows whose date can't be parsed can't be placed in a month, so they are skipped.
    private fun TransactionItem.toDashboardTransaction(): DashboardTransaction? {
        val millis = dateMillis ?: return null
        return DashboardTransaction(
            id = id,
            title = title,
            category = category,
            amount = amount,
            kind = if (type == TransactionType.INCOME) DashboardTransactionKind.INCOME else DashboardTransactionKind.EXPENSE,
            timestampMillis = millis
        )
    }

    companion object {
        /** There is no logged-in user yet; the other screens use user 1 as well. */
        const val DEFAULT_USER_ID = 1L

        fun create(context: Context, userId: Long = DEFAULT_USER_ID): RoomDashboardRepository {
            val database = AppDatabase.getDatabase(context)
            return RoomDashboardRepository(
                transactionRepository = RoomTransactionRepository(database.transactionDao(), database.userDao()),
                userDao = database.userDao(),
                userId = userId
            )
        }
    }
}
