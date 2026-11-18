package com.example.mobilefinance.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mobilefinance.ui.components.AuthCard
import com.example.mobilefinance.ui.components.AuthFooterLink
import com.example.mobilefinance.ui.components.AuthHeadline
import com.example.mobilefinance.ui.components.AuthPasswordField
import com.example.mobilefinance.ui.components.AuthTextField
import com.example.mobilefinance.ui.components.FormErrorBanner
import com.example.mobilefinance.ui.components.PrimaryButton
import com.example.mobilefinance.ui.components.clickableNoRipple
import com.example.mobilefinance.ui.theme.Green700
import com.example.mobilefinance.ui.theme.MobileFinanceTheme

@Composable
fun LoginRoute(
    viewModel: LoginViewModel,
    onLoginSuccess: (userId: Long) -> Unit,
    onCreateAccount: () -> Unit,
    onForgotPassword: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.loggedInUserId) {
        val userId = state.loggedInUserId
        if (userId != null) {
            viewModel.onLoginHandled()
            onLoginSuccess(userId)
        }
    }

    LoginScreen(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLoginClick = viewModel::login,
        onForgotPassword = onForgotPassword,
        onCreateAccount = onCreateAccount
    )
}

@Composable
fun LoginScreen(
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onForgotPassword: () -> Unit,
    onCreateAccount: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Spacer(Modifier.height(24.dp))

        AuthHeadline(
            firstLine = "Welcome",
            secondLine = "back",
            subtitle = "Log in to track spending, stay on budget and grow your savings."
        )

        Spacer(Modifier.height(32.dp))

        AuthCard {
            FormErrorBanner(state.formError, Modifier.padding(bottom = 16.dp))

            AuthTextField(
                label = "Email",
                value = state.email,
                onValueChange = onEmailChange,
                leadingIcon = Icons.Outlined.Email,
                placeholder = "you@example.com",
                error = state.emailError,
                isValid = state.isEmailValid,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )

            Spacer(Modifier.height(20.dp))

            AuthPasswordField(
                label = "Password",
                value = state.password,
                onValueChange = onPasswordChange,
                leadingIcon = Icons.Outlined.Lock,
                placeholder = "Enter your password",
                error = state.passwordError,
                imeAction = ImeAction.Done,
                onImeAction = {
                    focusManager.clearFocus()
                    onLoginClick()
                }
            )

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "Forgot Password?",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Green700,
                    modifier = Modifier
                        .clickableNoRipple(onForgotPassword)
                        .padding(vertical = 14.dp)
                )
            }

            PrimaryButton(
                text = "Log In",
                onClick = {
                    focusManager.clearFocus()
                    onLoginClick()
                },
                isLoading = state.isLoading
            )
        }

        Spacer(Modifier.height(28.dp))

        AuthFooterLink(
            prompt = "New here?",
            action = "Create account",
            onClick = onCreateAccount
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenPreview() {
    MobileFinanceTheme {
        LoginScreen(
            state = LoginUiState(email = "maya.chen@mail.com", password = "secret123"),
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onForgotPassword = {},
            onCreateAccount = {}
        )
    }
}
