package com.example.mobilefinance.data.mapper

import com.example.financeroom.Transaction
import com.example.mobilefinance.data.model.TransactionItem
import com.example.mobilefinance.data.model.TransactionType
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
 * Mapper for the Room entity (id, userId, amount, description, date, time, category).
 *
 *  - negative amount = expense, positive = income (the table has no type column)
 *  - blank category falls back to "Other"
 *  - date + time are strings, we try a few common formats and use null if none fit
 */
object RoomTransactionMapper : TransactionMapper<Transaction> {
    override fun map(source: Transaction): TransactionItem = TransactionItem(
        id = source.id.toLong(),
        title = source.description.ifBlank { "Untitled" },
        amount = abs(source.amount),
        type = if (source.amount < 0) TransactionType.EXPENSE else TransactionType.INCOME,
        category = source.category.ifBlank { TransactionItem.DEFAULT_CATEGORY },
        dateMillis = parseDateTime(source.date, source.time)
    )

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
