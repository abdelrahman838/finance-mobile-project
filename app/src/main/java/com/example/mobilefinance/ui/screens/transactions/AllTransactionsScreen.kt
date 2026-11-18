package com.example.mobilefinance.ui.screens.transactions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mobilefinance.data.repository.sampleTransactions
import com.example.mobilefinance.ui.theme.Green700
import com.example.mobilefinance.ui.theme.Outline
import com.example.mobilefinance.ui.theme.Ink
import com.example.mobilefinance.ui.theme.InkMuted
import com.example.mobilefinance.ui.theme.Background

/** Hooks the screen up to its ViewModel. */
@Composable
fun AllTransactionsScreen(
    viewModel: AllTransactionsViewModel,
    onTransactionClick: (Long) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AllTransactionsContent(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onCategorySelected = viewModel::onCategorySelected,
        onDateRangeSelected = viewModel::onDateRangeSelected,
        onSortSelected = viewModel::onSortSelected,
        onResetFilters = viewModel::clearFilters,
        onTransactionClick = onTransactionClick
    )
}

@Composable
fun AllTransactionsContent(
    state: AllTransactionsUiState,
    onQueryChange: (String) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onDateRangeSelected: (DateRange) -> Unit,
    onSortSelected: (SortOrder) -> Unit,
    onResetFilters: () -> Unit,
    onTransactionClick: (Long) -> Unit
) {
    val filter = state.filter
    val hasActiveFilters = filter != TransactionFilter()

    Scaffold(containerColor = Background) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 32.dp)
        ) {
            item {
                Header(
                    shownCount = state.shownCount,
                    rangeLabel = filter.dateRange.label,
                    hasActiveFilters = hasActiveFilters,
                    onResetFilters = onResetFilters
                )
                Spacer(Modifier.height(20.dp))
                TransactionSearchBar(query = filter.query, onQueryChange = onQueryChange)
                Spacer(Modifier.height(16.dp))
            }

            item {
                CategoryChips(
                    categories = state.categories,
                    selected = filter.category,
                    onSelected = onCategorySelected
                )
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DateRangeDropdown(
                        selected = filter.dateRange,
                        onSelected = onDateRangeSelected,
                        modifier = Modifier.weight(1f)
                    )
                    SortDropdown(
                        selected = filter.sort,
                        onSelected = onSortSelected,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            when {
                state.isLoading -> item { LoadingState() }
                state.groups.isEmpty() -> item {
                    EmptyState(
                        hasData = state.totalCount > 0,
                        onResetFilters = onResetFilters
                    )
                }
                else -> state.groups.forEach { group ->
                    item(key = "header_${group.label}") {
                        GroupHeader(label = group.label, total = group.total)
                    }
                    itemsIndexed(group.items, key = { _, t -> t.id }) { index, transaction ->
                        TransactionRow(
                            item = transaction,
                            shape = rowShape(index, group.items.size),
                            showDivider = index < group.items.lastIndex,
                            onClick = { onTransactionClick(transaction.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(
    shownCount: Int,
    rangeLabel: String,
    hasActiveFilters: Boolean,
    onResetFilters: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("Transactions", color = Ink, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            val recordsText = if (shownCount == 1) "1 record" else "$shownCount records"
            Text("$recordsText · $rangeLabel", color = InkMuted, fontSize = 15.sp)
        }
        Box {
            Surface(
                onClick = onResetFilters,
                shape = CircleShape,
                color = Color.White,
                border = BorderStroke(1.dp, Outline),
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Tune, contentDescription = "Reset filters", tint = Ink)
                }
            }
            // little dot so you can tell some filter is on
            if (hasActiveFilters) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(10.dp)
                        .background(Green700, CircleShape)
                )
            }
        }
    }
}

@Composable
private fun CategoryChips(
    categories: List<String>,
    selected: String?,
    onSelected: (String?) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            FilterChipPill(text = "All", selected = selected == null, onClick = { onSelected(null) })
        }
        items(categories) { category ->
            FilterChipPill(
                text = category,
                selected = category == selected,
                onClick = { onSelected(if (category == selected) null else category) }
            )
        }
    }
}

@Composable
private fun DateRangeDropdown(
    selected: DateRange,
    onSelected: (DateRange) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        DropdownPill(
            text = selected.label,
            icon = Icons.Outlined.CalendarMonth,
            iconTint = Green700,
            trailing = Icons.Filled.KeyboardArrowDown,
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DateRange.entries.forEach { range ->
                DropdownMenuItem(
                    text = {
                        Text(
                            range.label,
                            fontWeight = if (range == selected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onSelected(range)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SortDropdown(
    selected: SortOrder,
    onSelected: (SortOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        DropdownPill(
            text = selected.label,
            icon = Icons.Outlined.SwapVert,
            iconTint = Ink,
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SortOrder.entries.forEach { order ->
                DropdownMenuItem(
                    text = {
                        Text(
                            order.label,
                            fontWeight = if (order == selected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onSelected(order)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp)
    ) {
        CircularProgressIndicator(color = Green700)
    }
}

@Composable
private fun EmptyState(hasData: Boolean, onResetFilters: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 56.dp)
    ) {
        Icon(
            Icons.Outlined.ReceiptLong,
            contentDescription = null,
            tint = InkMuted,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = if (hasData) "Nothing matches your filters" else "No transactions yet",
            color = Ink,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (hasData) "Try a different search or date range" else "Add one and it will show up here",
            color = InkMuted,
            fontSize = 14.sp
        )
        if (hasData) {
            TextButton(onClick = onResetFilters) {
                Text("Clear filters", color = Green700, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/** Rounds only the outer corners so the rows of one group look like a single card. */
private fun rowShape(index: Int, size: Int): Shape {
    val r = 24.dp
    return when {
        size == 1 -> RoundedCornerShape(r)
        index == 0 -> RoundedCornerShape(topStart = r, topEnd = r)
        index == size - 1 -> RoundedCornerShape(bottomStart = r, bottomEnd = r)
        else -> RectangleShape
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AllTransactionsPreview() {
    val items = sampleTransactions()
    val filter = TransactionFilter()
    val shown = items.applyFilter(filter)
    AllTransactionsContent(
        state = AllTransactionsUiState(
            isLoading = false,
            filter = filter,
            categories = items.map { it.category }.distinct().sorted(),
            groups = shown.groupForDisplay(filter.sort),
            totalCount = items.size,
            shownCount = shown.size
        ),
        onQueryChange = {},
        onCategorySelected = {},
        onDateRangeSelected = {},
        onSortSelected = {},
        onResetFilters = {},
        onTransactionClick = {}
    )
}
