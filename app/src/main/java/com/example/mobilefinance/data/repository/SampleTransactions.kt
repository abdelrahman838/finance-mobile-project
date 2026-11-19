package com.example.mobilefinance.data.repository

import com.example.mobilefinance.data.model.TransactionItem
import com.example.mobilefinance.data.model.TransactionType

/** Hardcoded transactions for the preview and the tests. */
fun sampleTransactions(now: Long = System.currentTimeMillis()): List<TransactionItem> {
    val hour = 60 * 60 * 1000L
    val day = 24 * hour
    return listOf(
        TransactionItem(1, "Whole Foods Market", 84.20, TransactionType.EXPENSE, "Groceries", dateMillis = now - 2 * hour),
        TransactionItem(2, "Uber Trip Downtown", 18.50, TransactionType.EXPENSE, "Transport", dateMillis = now - 5 * hour),
        TransactionItem(3, "Salary - Linear", 3200.00, TransactionType.INCOME, "Income", dateMillis = now - day),
        TransactionItem(4, "Electric Bill", 62.00, TransactionType.EXPENSE, "Bills", dateMillis = now - day),
        TransactionItem(5, "Cinema - Dune 2", 24.00, TransactionType.EXPENSE, "Entertainment", dateMillis = now - day - hour),
        TransactionItem(6, "H&M - Fall Fit", 129.90, TransactionType.EXPENSE, "Shopping", dateMillis = now - 3 * day),
        TransactionItem(7, "Blue Bottle Coffee", 6.40, TransactionType.EXPENSE, "Food", dateMillis = now - 4 * day)
    )
}
