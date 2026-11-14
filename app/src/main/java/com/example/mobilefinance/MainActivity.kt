package com.example.mobilefinance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobilefinance.data.local.AppDatabase
import com.example.mobilefinance.data.repository.RoomTransactionRepository
import com.example.mobilefinance.ui.screens.transactions.AllTransactionsScreen
import com.example.mobilefinance.ui.screens.transactions.AllTransactionsViewModel
import com.example.mobilefinance.ui.theme.MobileFinanceTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getInstance(this)
        val transactionRepository = RoomTransactionRepository(db.transactionDao(), db.userDao())

        // TODO remove once the add transaction screen is done
        lifecycleScope.launch { transactionRepository.seedIfEmpty() }

        setContent {
            MobileFinanceTheme {
                // TODO move to the nav graph when we add navigation
                val vm: AllTransactionsViewModel = viewModel(
                    factory = AllTransactionsViewModel.Factory(transactionRepository)
                )
                AllTransactionsScreen(
                    viewModel = vm,
                    onTransactionClick = { /* open edit transaction screen */ }
                )
            }
        }
    }
}
