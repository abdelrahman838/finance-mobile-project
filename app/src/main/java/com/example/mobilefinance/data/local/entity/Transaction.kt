package com.example.mobilefinance.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val transactionId: Long = 0,
    val userId: Long,
    val type: TransactionType = TransactionType.EXPENSE, // Defaults to Expense
    val amount: Double,
    val category: TransactionCategory = TransactionCategory.FOOD, // Defaults to Food
    val title: String,
    val description: String = "",
    val date: String,
    val time: String
)