package com.example.mobilefinance.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.financeroom.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY id DESC")
    fun getAll(): Flow<List<Transaction>>

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun count(): Int

    @Insert
    suspend fun insertAll(transactions: List<Transaction>)
}
