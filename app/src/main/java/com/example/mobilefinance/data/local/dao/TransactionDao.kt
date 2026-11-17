package com.example.mobilefinance.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mobilefinance.data.local.entity.Transaction
import com.example.mobilefinance.data.local.entity.TransactionType
import kotlinx.coroutines.flow.Flow

data class MonthlySpending(
    val month: String,
    val totalSpent: Double
)

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<Transaction>)

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE transactionId = :id")
    suspend fun getTransactionById(id: Long): Transaction?

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun count(): Int

    @Query("SELECT * FROM transactions ORDER BY date DESC, time DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC, time DESC")
    fun getAllTransactionsForUser(userId: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC, time DESC LIMIT :limit")
    fun getRecentTransactions(userId: Long, limit: Int = 4): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND category = :category ORDER BY date DESC, time DESC")
    fun getTransactionsByCategory(userId: Long, category: String): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%') ORDER BY date DESC, time DESC")
    fun searchTransactions(userId: Long, query: String): Flow<List<Transaction>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE userId = :userId AND type = :type")
    fun getTotalByType(userId: Long, type: TransactionType): Flow<Double>

    @Query("SELECT COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE -amount END), 0.0) FROM transactions WHERE userId = :userId")
    fun getTotalBalance(userId: Long): Flow<Double>

    @Query("SELECT SUBSTR(date, 1, 7) AS month, SUM(amount) AS totalSpent FROM transactions WHERE userId = :userId AND type = 'EXPENSE' AND date LIKE :yearPrefix GROUP BY month ORDER BY month ASC")
    fun getMonthlyExpenses(userId: Long, yearPrefix: String): Flow<List<MonthlySpending>>
}