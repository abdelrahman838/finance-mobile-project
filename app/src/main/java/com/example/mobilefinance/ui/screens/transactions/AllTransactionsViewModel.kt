package com.example.mobilefinance.ui.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mobilefinance.data.repository.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class AllTransactionsUiState(
    val isLoading: Boolean = true,
    val filter: TransactionFilter = TransactionFilter(),
    val categories: List<String> = emptyList(),
    val groups: List<TransactionGroup> = emptyList(),
    val totalCount: Int = 0,   // everything for this user in the db
    val shownCount: Int = 0    // after filters
)

class AllTransactionsViewModel(
    private val repository: TransactionRepository,
    private val defaultUserId: Int = 1
) : ViewModel() {

    private val filter = MutableStateFlow(TransactionFilter())
    private val userIdState = MutableStateFlow(defaultUserId)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<AllTransactionsUiState> =
        userIdState.flatMapLatest { userId ->
            combine(repository.observeTransactionsForUser(userId), filter) { all, f ->
                val shown = all.applyFilter(f)
                AllTransactionsUiState(
                    isLoading = false,
                    filter = f,
                    categories = all.map { it.category }.distinct().sorted(),
                    groups = shown.groupForDisplay(f.sort),
                    totalCount = all.size,
                    shownCount = shown.size
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AllTransactionsUiState()
        )

    fun setUserId(userId: Int) {
        userIdState.value = userId
    }

    fun onQueryChange(query: String) = filter.update { it.copy(query = query) }

    fun onCategorySelected(category: String?) = filter.update { it.copy(category = category) }

    fun onDateRangeSelected(range: DateRange) = filter.update { it.copy(dateRange = range) }

    fun onSortSelected(sort: SortOrder) = filter.update { it.copy(sort = sort) }

    fun clearFilters() = filter.update { TransactionFilter() }

    class Factory(
        private val repository: TransactionRepository,
        private val userId: Int = 1
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AllTransactionsViewModel(repository, userId) as T
    }
}
