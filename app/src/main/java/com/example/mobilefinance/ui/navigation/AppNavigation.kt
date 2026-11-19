package com.example.mobilefinance.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mobilefinance.data.local.AppDatabase
import com.example.mobilefinance.data.repository.RoomTransactionRepository
import com.example.mobilefinance.ui.screens.AddTransactionRoute
import com.example.mobilefinance.ui.screens.AddTransactionScreenViewModel
import com.example.mobilefinance.ui.screens.editTransaction.EditTransactionRoute
import com.example.mobilefinance.ui.screens.editTransaction.EditTransactionScreenViewModel
import com.example.mobilefinance.ui.screens.dashboard.DashboardRoute
import com.example.mobilefinance.ui.screens.dashboard.DashboardViewModel
import com.example.mobilefinance.ui.screens.dashboard.RoomDashboardRepository
import com.example.mobilefinance.ui.screens.login.LoginRoute
import com.example.mobilefinance.ui.screens.login.LoginViewModel
import com.example.mobilefinance.ui.screens.signup.SignUpRoute
import com.example.mobilefinance.ui.screens.signup.SignUpViewModel
import com.example.mobilefinance.ui.screens.transactions.AllTransactionsScreen
import com.example.mobilefinance.ui.screens.transactions.AllTransactionsViewModel

object Routes {
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"
    const val HOME = "home"
    const val DASHBOARD_PATTERN = "dashboard/{userId}"

    fun dashboard(userId: Long): String = "dashboard/$userId"
    const val EDIT_TRANSACTION = "edit_transaction"
    const val EDIT_TRANSACTION_PATTERN = "edit_transaction?transactionId={transactionId}"

    fun editTransaction(transactionId: Long): String = "edit_transaction?transactionId=$transactionId"
    fun editTransaction(transactionId: Int): String = "edit_transaction?transactionId=$transactionId"
    const val ADD_TRANSACTION_PATTERN = "add_transaction/{userId}"
    fun addTransaction(userId: Long): String = "add_transaction/$userId"
    const val TRANSACTIONS_PATTERN = "transactions/{userId}"
    fun transactions(userId: Long): String = "transactions/$userId"
}

private const val KEY_SIGNED_UP_EMAIL = "signed_up_email"

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val database = remember(context) { AppDatabase.getDatabase(context) }

    // Seed test rows in Room so database has initial transactions if empty
    LaunchedEffect(Unit) {
        RoomTransactionRepository(database.transactionDao(), database.userDao()).seedIfEmpty()
    }

    NavHost(navController = navController, startDestination = Routes.LOGIN) {

        composable(Routes.LOGIN) { entry ->
            val loginViewModel: LoginViewModel = viewModel(
                factory = LoginViewModel.provideFactory(database.userDao())
            )

            val signedUpEmail = entry.savedStateHandle.get<String>(KEY_SIGNED_UP_EMAIL)
            LaunchedEffect(signedUpEmail) {
                if (signedUpEmail != null) {
                    loginViewModel.prefillEmail(signedUpEmail)
                    entry.savedStateHandle.remove<String>(KEY_SIGNED_UP_EMAIL)
                }
            }

            LoginRoute(
                viewModel = loginViewModel,
                onLoginSuccess = { userId ->
                    navController.navigate(Routes.dashboard(userId)) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onCreateAccount = { navController.navigate(Routes.SIGN_UP) },
                onForgotPassword = {}
            )
        }

        composable(Routes.SIGN_UP) {
            val signUpViewModel: SignUpViewModel = viewModel(
                factory = SignUpViewModel.provideFactory(database.userDao())
            )

            SignUpRoute(
                viewModel = signUpViewModel,
                onSignUpSuccess = { email ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(KEY_SIGNED_UP_EMAIL, email)
                    navController.popBackStack()
                },
                onLogIn = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.DASHBOARD_PATTERN,
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { entry ->
            val userId = entry.arguments?.getLong("userId") ?: RoomDashboardRepository.DEFAULT_USER_ID
            val dashboardViewModel: DashboardViewModel = viewModel(
                factory = DashboardViewModel.factory(RoomDashboardRepository.create(context, userId))
            )

            DashboardRoute(
                viewModel = dashboardViewModel,
                onAddTransaction = { navController.navigate(Routes.addTransaction(userId)) },
                onSeeAllTransactions = { navController.navigate(Routes.transactions(userId)) },
                onTransactionClick = { id -> navController.navigate(Routes.editTransaction(id)) },
                onSearchClick = {},
                onNotificationsClick = {},
                onTabSelected = {}
            )
        }

        composable(
            route = Routes.ADD_TRANSACTION_PATTERN,
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) {
            val addViewModel: AddTransactionScreenViewModel = viewModel(
                factory = AddTransactionScreenViewModel.provideFactory(database.transactionDao())
            )
            AddTransactionRoute(
                viewModel = addViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.TRANSACTIONS_PATTERN,
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { entry ->
            val userId = entry.arguments?.getLong("userId") ?: RoomDashboardRepository.DEFAULT_USER_ID
            val transactionsViewModel: AllTransactionsViewModel = viewModel(
                factory = AllTransactionsViewModel.Factory(
                    RoomTransactionRepository(database.transactionDao(), database.userDao()),
                    userId.toInt()
                )
            )
            AllTransactionsScreen(
                viewModel = transactionsViewModel,
                onTransactionClick = { id -> navController.navigate(Routes.editTransaction(id)) }
            )
        }

        composable(Routes.HOME) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Text("Home", style = MaterialTheme.typography.displaySmall)
            }
        }

        composable(
            route = Routes.EDIT_TRANSACTION_PATTERN,
            arguments = listOf(
                navArgument("transactionId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) {
            val editViewModel: EditTransactionScreenViewModel = viewModel(
                factory = EditTransactionScreenViewModel.provideFactory(database.transactionDao())
            )

            EditTransactionRoute(
                viewModel = editViewModel,
                onNavigateBack = {
                    val popped = navController.popBackStack()
                    if (!popped) {
                        navController.navigate(Routes.HOME)
                    }
                },
                onMoreClick = {}
            )
        }
    }
}
