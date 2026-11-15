package com.example.mobilefinance.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mobilefinance.ui.screens.login.LoginRoute
import com.example.mobilefinance.ui.screens.login.LoginViewModel
import com.example.mobilefinance.ui.screens.signup.SignUpRoute
import com.example.mobilefinance.ui.screens.signup.SignUpViewModel

object Routes {
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"
    const val HOME = "home"
}

private const val KEY_SIGNED_UP_EMAIL = "signed_up_email"

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.LOGIN) {

        composable(Routes.LOGIN) { entry ->
            val loginViewModel: LoginViewModel = viewModel()

            val signedUpEmail = entry.savedStateHandle.get<String>(KEY_SIGNED_UP_EMAIL)
            LaunchedEffect(signedUpEmail) {
                if (signedUpEmail != null) {
                    loginViewModel.prefillEmail(signedUpEmail)
                    entry.savedStateHandle.remove<String>(KEY_SIGNED_UP_EMAIL)
                }
            }

            LoginRoute(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onCreateAccount = { navController.navigate(Routes.SIGN_UP) },
                onForgotPassword = {}
            )
        }

        composable(Routes.SIGN_UP) {
            val signUpViewModel: SignUpViewModel = viewModel()

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
    }
}
