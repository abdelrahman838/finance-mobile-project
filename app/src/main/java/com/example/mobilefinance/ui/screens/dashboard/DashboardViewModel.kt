package com.example.mobilefinance.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.util.Calendar

data class DashboardUiState(
    val userName: String = "",
    val greeting: String = "Good morning,",
    val totalBalance: Double = 0.0,
    val income: Double = 0.0,
    val expenses: Double = 0.0,
    /** Change of this month's balance vs last month, in percent. Null when there is nothing to compare. */
    val changeVsLastMonthPercent: Double? = null,
    /** Expenses per month of the current year, index 0 = January. */
    val monthlySpending: List<Double> = List(12) { 0.0 },
    val currentMonth: Int = 0,
    val selectedMonth: Int = 0,
    val monthlyBudget: Double? = null,
    val recentTransactions: List<DashboardTransaction> = emptyList(),
    val isLoading: Boolean = true
) {
    val selectedMonthSpending: Double get() = monthlySpending.getOrElse(selectedMonth) { 0.0 }
}

class DashboardViewModel(repository: DashboardRepository) : ViewModel() {

    private val selectedMonth = MutableStateFlow<Int?>(null)

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.observeUserName(),
        repository.observeTransactions(),
        repository.observeMonthlyBudget(),
        selectedMonth
    ) { name, transactions, budget, selected ->
        buildState(name, transactions, budget, selected)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    fun onMonthSelected(month: Int) {
        selectedMonth.update { month }
    }

    private fun buildState(
        name: String,
        transactions: List<DashboardTransaction>,
        budget: Double?,
        selected: Int?
    ): DashboardUiState {
        val now = Calendar.getInstance()
        val year = now.get(Calendar.YEAR)
        val month = now.get(Calendar.MONTH)
        val lastMonthCal = (now.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
        val lastYear = lastMonthCal.get(Calendar.YEAR)
        val lastMonth = lastMonthCal.get(Calendar.MONTH)

        val cal = Calendar.getInstance()
        var income = 0.0
        var expenses = 0.0
        var lastIncome = 0.0
        var lastExpenses = 0.0
        val monthlySpending = DoubleArray(12)

        transactions.forEach { t ->
            cal.timeInMillis = t.timestampMillis
            val tYear = cal.get(Calendar.YEAR)
            val tMonth = cal.get(Calendar.MONTH)
            val isIncome = t.kind == DashboardTransactionKind.INCOME

            if (tYear == year && !isIncome) monthlySpending[tMonth] += t.amount
            if (tYear == year && tMonth == month) {
                if (isIncome) income += t.amount else expenses += t.amount
            } else if (tYear == lastYear && tMonth == lastMonth) {
                if (isIncome) lastIncome += t.amount else lastExpenses += t.amount
            }
        }

        val balance = income - expenses
        val lastBalance = lastIncome - lastExpenses
        val change = if (lastBalance != 0.0) (balance - lastBalance) / kotlin.math.abs(lastBalance) * 100 else null

        return DashboardUiState(
            userName = name,
            greeting = greetingFor(now.get(Calendar.HOUR_OF_DAY)),
            totalBalance = balance,
            income = income,
            expenses = expenses,
            changeVsLastMonthPercent = change,
            monthlySpending = monthlySpending.toList(),
            currentMonth = month,
            selectedMonth = selected ?: month,
            monthlyBudget = budget,
            recentTransactions = transactions
                .sortedByDescending { it.timestampMillis }
                .take(RECENT_TRANSACTIONS_COUNT),
            isLoading = false
        )
    }

    private fun greetingFor(hour: Int): String = when (hour) {
        in 5..11 -> "Good morning,"
        in 12..16 -> "Good afternoon,"
        else -> "Good evening,"
    }

    companion object {
        private const val RECENT_TRANSACTIONS_COUNT = 4

        fun factory(repository: DashboardRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { DashboardViewModel(repository) }
        }
    }
}
