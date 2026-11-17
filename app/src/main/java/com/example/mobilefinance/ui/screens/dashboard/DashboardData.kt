package com.example.mobilefinance.ui.screens.dashboard

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.Calendar

enum class DashboardTransactionKind { INCOME, EXPENSE }

/**
 * The dashboard's view of a transaction. [amount] is always positive; [kind] decides the sign.
 */
data class DashboardTransaction(
    val id: Long,
    val title: String,
    val category: String,
    val amount: Double,
    val kind: DashboardTransactionKind,
    val timestampMillis: Long
)

/**
 * Everything the dashboard reads. [RoomDashboardRepository] reads it from the database,
 * [SampleDashboardRepository] returns fixed data. Pass one to [DashboardViewModel.factory].
 */
interface DashboardRepository {
    fun observeUserName(): Flow<String>
    fun observeTransactions(): Flow<List<DashboardTransaction>>

    /** Monthly spending budget, or null if the user has none. */
    fun observeMonthlyBudget(): Flow<Double?> = flowOf(null)
}

/** Static sample data, for previews and quick testing. */
class SampleDashboardRepository : DashboardRepository {

    override fun observeUserName(): Flow<String> = flowOf("Maya Chen")

    override fun observeMonthlyBudget(): Flow<Double?> = flowOf(2500.0)

    override fun observeTransactions(): Flow<List<DashboardTransaction>> = flowOf(sampleTransactions())

    private fun sampleTransactions(): List<DashboardTransaction> {
        val now = Calendar.getInstance()
        fun at(daysAgo: Int, hour: Int, minute: Int): Long = (now.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, -daysAgo)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }.timeInMillis

        fun monthsAgo(months: Int): Long = (now.clone() as Calendar).apply {
            add(Calendar.MONTH, -months)
            set(Calendar.DAY_OF_MONTH, 1)
        }.timeInMillis

        var id = 0L
        val history = (1..now.get(Calendar.MONTH)).map { m ->
            DashboardTransaction(
                id = ++id,
                title = "Monthly spending",
                category = "Shopping",
                amount = listOf(1200.0, 1650.0, 1050.0, 1800.0, 1400.0, 2250.0, 1700.0, 1940.0, 1350.0, 1850.0, 1600.0)[m % 11],
                kind = DashboardTransactionKind.EXPENSE,
                timestampMillis = monthsAgo(m)
            )
        }

        return history + listOf(
            DashboardTransaction(++id, "Whole Foods Market", "Groceries", 84.20, DashboardTransactionKind.EXPENSE, at(0, 14, 40)),
            DashboardTransaction(++id, "Salary — Linear", "Income", 3200.0, DashboardTransactionKind.INCOME, at(0, 9, 0)),
            DashboardTransaction(++id, "Uber Trip", "Transport", 18.50, DashboardTransactionKind.EXPENSE, at(1, 19, 12)),
            DashboardTransaction(++id, "Electric Bill", "Bills", 62.00, DashboardTransactionKind.EXPENSE, at(1, 8, 30)),
            DashboardTransaction(++id, "Freelance", "Income", 1620.0, DashboardTransactionKind.INCOME, at(3, 11, 0)),
            DashboardTransaction(++id, "Rent", "Bills", 1500.0, DashboardTransactionKind.EXPENSE, at(4, 9, 0)),
            DashboardTransaction(++id, "Dinner", "Food", 645.30, DashboardTransactionKind.EXPENSE, at(5, 20, 15))
        )
    }
}
