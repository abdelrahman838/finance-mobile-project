package com.example.mobilefinance

import com.example.financeroom.Transaction
import com.example.mobilefinance.data.mapper.RoomTransactionMapper
import com.example.mobilefinance.data.model.TransactionItem
import com.example.mobilefinance.data.model.TransactionType
import com.example.mobilefinance.data.repository.sampleTransactions
import com.example.mobilefinance.ui.screens.transactions.DateRange
import com.example.mobilefinance.ui.screens.transactions.SortOrder
import com.example.mobilefinance.ui.screens.transactions.TransactionFilter
import com.example.mobilefinance.ui.screens.transactions.applyFilter
import com.example.mobilefinance.ui.screens.transactions.groupForDisplay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionFiltersTest {

    // noon today so "2 hours ago" is always still today
    private val now = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.HOUR_OF_DAY, 12)
        set(java.util.Calendar.MINUTE, 0)
    }.timeInMillis
    private val items = sampleTransactions(now)

    @Test
    fun search_matches_title_ignoring_case() {
        val result = items.applyFilter(TransactionFilter(query = "uber"), now)
        assertEquals(listOf("Uber Trip Downtown"), result.map { it.title })
    }

    @Test
    fun category_filter_only_keeps_that_category() {
        val result = items.applyFilter(TransactionFilter(category = "Bills"), now)
        assertTrue(result.isNotEmpty())
        assertTrue(result.all { it.category == "Bills" })
    }

    @Test
    fun highest_sort_puts_salary_first() {
        val result = items.applyFilter(TransactionFilter(sort = SortOrder.HIGHEST), now)
        assertEquals(3200.0, result.first().amount, 0.0)
    }

    @Test
    fun undated_items_only_show_in_all_time() {
        val undated = TransactionItem(99, "No date", 5.0, TransactionType.EXPENSE)
        val list = items + undated
        assertTrue(list.applyFilter(TransactionFilter(), now).contains(undated))
        assertTrue(!list.applyFilter(TransactionFilter(dateRange = DateRange.THIS_YEAR), now).contains(undated))
    }

    @Test
    fun groups_start_with_today() {
        val groups = items.applyFilter(TransactionFilter(), now).groupForDisplay(SortOrder.NEWEST, now)
        assertEquals("Today", groups.first().label)
        assertEquals(-102.70, groups.first().total, 0.001)
    }

    @Test
    fun room_mapper_uses_sign_for_type() {
        val expense = RoomTransactionMapper.map(
            Transaction(id = 1, userId = 1, amount = -10.0, description = "Coffee", date = "2026-10-23", time = "14:40", category = "Food")
        )
        val income = RoomTransactionMapper.map(
            Transaction(id = 2, userId = 1, amount = 50.0, description = "Gift", date = "", time = "", category = "")
        )
        assertEquals(TransactionType.EXPENSE, expense.type)
        assertEquals(10.0, expense.amount, 0.0)
        assertEquals("Food", expense.category)
        assertEquals(TransactionType.INCOME, income.type)
        assertEquals(TransactionItem.DEFAULT_CATEGORY, income.category)
        assertEquals(null, income.dateMillis)
    }

    @Test
    fun room_mapper_parses_date_and_time() {
        val millis = RoomTransactionMapper.parseDateTime("2026-10-23", "14:40")!!
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = millis }
        assertEquals(2026, cal.get(java.util.Calendar.YEAR))
        assertEquals(java.util.Calendar.OCTOBER, cal.get(java.util.Calendar.MONTH))
        assertEquals(23, cal.get(java.util.Calendar.DAY_OF_MONTH))
        assertEquals(14, cal.get(java.util.Calendar.HOUR_OF_DAY))
        // other formats work too, junk gives null
        assertTrue(RoomTransactionMapper.parseDateTime("Oct 23, 2026", "2:40 PM") != null)
        assertEquals(null, RoomTransactionMapper.parseDateTime("not a date", ""))
    }
}
