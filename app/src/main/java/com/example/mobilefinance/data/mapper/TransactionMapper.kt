package com.example.mobilefinance.data.mapper

import com.example.mobilefinance.data.local.entity.Transaction
import com.example.mobilefinance.data.local.entity.TransactionCategory
import com.example.mobilefinance.data.model.TransactionItem
import com.example.mobilefinance.data.model.TransactionType as TransactionItemType
import com.example.mobilefinance.data.local.entity.TransactionType as EntityTransactionType
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs

/**
 * Turns whatever the data source returns into a [TransactionItem].
 * Swap the implementation if the entity changes and the rest of the app stays the same.
 */
fun interface TransactionMapper<T> {
    fun map(source: T): TransactionItem
}

/**
 * Mapper for the Room entity (transactionId, userId, type, amount, category, title, description, date, time).
 */
object RoomTransactionMapper : TransactionMapper<Transaction> {
    override fun map(source: Transaction): TransactionItem = TransactionItem(
        id = source.transactionId,
        title = source.title.ifBlank { source.description.ifBlank { "Untitled" } },
        amount = abs(source.amount),
        type = if (source.type == EntityTransactionType.EXPENSE) TransactionItemType.EXPENSE else TransactionItemType.INCOME,
        category = source.category.toDisplayName(),
        dateMillis = parseDateTime(source.date, source.time)
    )

    private fun TransactionCategory.toDisplayName(): String = when (this) {
        TransactionCategory.FOOD -> "Food"
        TransactionCategory.TRANSPORT -> "Transport"
        TransactionCategory.BILLS -> "Bills"
        TransactionCategory.SHOPPING -> "Shopping"
        TransactionCategory.FUN -> "Fun"
        TransactionCategory.HEALTH -> "Health"
        TransactionCategory.TRAVEL -> "Travel"
        TransactionCategory.MORE -> "More"
    }

    private val dateFormats = listOf("yyyy-MM-dd", "dd/MM/yyyy", "MMM d, yyyy")
    private val timeFormats = listOf("HH:mm", "h:mm a", "HH:mm:ss")

    /** "2026-10-23" + "14:40" -> millis. Time is optional, date isn't. */
    fun parseDateTime(date: String, time: String): Long? {
        val d = date.trim()
        val t = time.trim()
        if (d.isEmpty()) return null

        if (t.isNotEmpty()) {
            for (df in dateFormats) for (tf in timeFormats) {
                tryParse("$d $t", "$df $tf")?.let { return it }
            }
        }
        for (df in dateFormats) {
            tryParse(d, df)?.let { return it }
        }
        return null
    }

    private fun tryParse(text: String, pattern: String): Long? = try {
        SimpleDateFormat(pattern, Locale.US).apply { isLenient = false }.parse(text)?.time
    } catch (e: ParseException) {
        null
    }
}