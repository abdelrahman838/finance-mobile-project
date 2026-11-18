package com.example.mobilefinance.ui.screens.transactions

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobilefinance.data.local.AppDatabase
import com.example.mobilefinance.data.repository.RoomTransactionRepository
import com.example.mobilefinance.ui.screens.editTransaction.EditTransactionRoute
import com.example.mobilefinance.ui.screens.editTransaction.EditTransactionScreenViewModel

/**
 * The all transactions screen, already hooked up to the Room database.
 *
 * To show it from the nav graph just add:
 *   composable("transactions") { AllTransactionsRoute() }
 *
 * Tapping a transaction opens the edit screen on top of the list.
 * When it's saved, deleted or you go back, you land on the list again
 * and the list updates by itself because it reads from Room.
 *
 * If the nav graph wants to handle the tap itself (e.g. navigate to the
 * edit route) it can pass [onTransactionClick] instead.
 */
@Composable
fun AllTransactionsRoute(
    userId: Int = DEFAULT_USER_ID,
    onTransactionClick: ((Long) -> Unit)? = null
) {
    val context = LocalContext.current
    val database = remember(context) { AppDatabase.getDatabase(context) }
    val repository = remember(database) {
        RoomTransactionRepository(database.transactionDao(), database.userDao())
    }

    val listViewModel: AllTransactionsViewModel = viewModel(
        factory = AllTransactionsViewModel.Factory(repository, userId)
    )

    // id of the transaction being edited, null = just the list
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }

    Box(Modifier.fillMaxSize()) {
        AllTransactionsScreen(
            viewModel = listViewModel,
            onTransactionClick = { id ->
                if (onTransactionClick != null) onTransactionClick(id) else editingId = id
            }
        )

        editingId?.let { id ->
            // fresh edit viewmodel for each transaction we open
            val editViewModel = remember(id) {
                EditTransactionScreenViewModel(
                    savedStateHandle = SavedStateHandle(mapOf("transactionId" to id)),
                    transactionDao = database.transactionDao()
                )
            }
            BackHandler { editingId = null }
            EditTransactionRoute(
                viewModel = editViewModel,
                onNavigateBack = { editingId = null }
            )
        }
    }
}

/** No real login session yet, the other screens use user 1 too. */
private const val DEFAULT_USER_ID = 1
