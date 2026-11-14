package com.example.mobilefinance.data.mapper

import com.example.financeroom.Transaction
import com.example.mobilefinance.data.model.TransactionItem
import com.example.mobilefinance.data.model.TransactionType
import kotlin.math.abs

/**
 * Turns whatever the data source returns into a [TransactionItem].
 * Swap the implementation if the entity changes and the rest of the app stays the same.
 */
fun interface TransactionMapper<T> {
    fun map(source: T): TransactionItem
}

/**
 * Mapper for the current Room entity (id, userId, amount, description).
 *
 * The table doesn't have category / date / type columns yet, so:
 *  - negative amount = expense, positive = income
 *  - category falls back to "Other"
 *  - date is null
 * When those columns get added just fill them in here.
 */
object RoomTransactionMapper : TransactionMapper<Transaction> {
    override fun map(source: Transaction): TransactionItem = TransactionItem(
        id = source.id.toLong(),
        title = source.description.ifBlank { "Untitled" },
        amount = abs(source.amount),
        type = if (source.amount < 0) TransactionType.EXPENSE else TransactionType.INCOME
    )
}
