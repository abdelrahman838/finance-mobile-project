package com.example.mobilefinance.ui.screens.transactions

import com.example.mobilefinance.data.model.TransactionItem
import com.example.mobilefinance.util.DAY_MILLIS
import com.example.mobilefinance.util.formatMonthYear
import com.example.mobilefinance.util.startOfDay
import com.example.mobilefinance.util.startOfMonth
import com.example.mobilefinance.util.startOfYear

enum class DateRange(val label: String) {
    ALL_TIME("All time"),
    TODAY("Today"),
    THIS_WEEK("Last 7 days"),
    THIS_MONTH("This month"),
    THIS_YEAR("This year")
}

enum class SortOrder(val label: String) {
    NEWEST("Newest first"),
    OLDEST("Oldest first"),
    HIGHEST("Highest amount"),
    LOWEST("Lowest amount")
}

data class TransactionFilter(
    val query: String = "",
    val category: String? = null, // null = all
    val dateRange: DateRange = DateRange.ALL_TIME,
    val sort: SortOrder = SortOrder.NEWEST
)

data class TransactionGroup(
    val label: String,
    val items: List<TransactionItem>
) {
    val total: Double get() = items.sumOf { it.signedAmount }
}

fun List<TransactionItem>.applyFilter(
    filter: TransactionFilter,
    now: Long = System.currentTimeMillis()
): List<TransactionItem> {
    val q = filter.query.trim()
    val from = filter.dateRange.startMillis(now)

    val filtered = filter { item ->
        val matchesQuery = q.isEmpty() ||
            item.title.contains(q, ignoreCase = true) ||
            item.note.contains(q, ignoreCase = true) ||
            item.category.contains(q, ignoreCase = true)
        val matchesCategory = filter.category == null ||
            item.category.equals(filter.category, ignoreCase = true)
        val matchesDate = from == null || (item.dateMillis != null && item.dateMillis >= from)
        matchesQuery && matchesCategory && matchesDate
    }

    // fall back to id when there's no date, newer rows have bigger ids
    return when (filter.sort) {
        SortOrder.NEWEST -> filtered.sortedWith(compareByDescending<TransactionItem> { it.dateMillis ?: Long.MIN_VALUE }.thenByDescending { it.id })
        SortOrder.OLDEST -> filtered.sortedWith(compareBy<TransactionItem> { it.dateMillis ?: Long.MAX_VALUE }.thenBy { it.id })
        SortOrder.HIGHEST -> filtered.sortedByDescending { it.amount }
        SortOrder.LOWEST -> filtered.sortedBy { it.amount }
    }
}

private fun DateRange.startMillis(now: Long): Long? = when (this) {
    DateRange.ALL_TIME -> null
    DateRange.TODAY -> startOfDay(now)
    DateRange.THIS_WEEK -> startOfDay(now) - 6 * DAY_MILLIS
    DateRange.THIS_MONTH -> startOfMonth(now)
    DateRange.THIS_YEAR -> startOfYear(now)
}

fun List<TransactionItem>.groupForDisplay(
    sort: SortOrder,
    now: Long = System.currentTimeMillis()
): List<TransactionGroup> {
    if (isEmpty()) return emptyList()
    if (sort == SortOrder.HIGHEST || sort == SortOrder.LOWEST) {
        return listOf(TransactionGroup("All results", this))
    }

    val today = startOfDay(now)
    val yesterday = today - DAY_MILLIS
    val weekStart = today - 6 * DAY_MILLIS

    fun labelFor(item: TransactionItem): String {
        val date = item.dateMillis ?: return "No date"
        return when {
            date >= today -> "Today"
            date >= yesterday -> "Yesterday"
            date >= weekStart -> "This week"
            else -> formatMonthYear(date)
        }
    }

    // keeps the order of the list, so groups come out in the right order too
    return groupBy(::labelFor).map { (label, items) -> TransactionGroup(label, items) }
}
