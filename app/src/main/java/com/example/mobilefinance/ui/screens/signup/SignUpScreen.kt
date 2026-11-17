package com.example.mobilefinance.ui.screens.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
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
import com.example.mobilefinance.ui.theme.InkHint
import com.example.mobilefinance.ui.theme.MobileFinanceTheme

@Composable
fun SignUpRoute(
    viewModel: SignUpViewModel,
    onSignUpSuccess: (email: String) -> Unit,
    onLogIn: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSignedUp) {
        if (state.isSignedUp) {
            viewModel.onSignUpHandled()
            onSignUpSuccess(state.email.trim())
        }
    }

    SignUpScreen(
        state = state,
        onFullNameChange = viewModel::onFullNameChange,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onSignUpClick = viewModel::signUp,
        onLogIn = onLogIn
    )
}

@Composable
fun SignUpScreen(
    state: SignUpUiState,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSignUpClick: () -> Unit,
    onLogIn: () -> Unit
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
            firstLine = "Create",
            secondLine = "account",
            subtitle = "Start tracking spending, set budgets and build your savings."
        )

        Spacer(Modifier.height(32.dp))

        AuthCard {
            FormErrorBanner(state.formError, Modifier.padding(bottom = 16.dp))

            AuthTextField(
                label = "Full name",
                value = state.fullName,
                onValueChange = onFullNameChange,
                leadingIcon = Icons.Outlined.Person,
                placeholder = "Your name",
                error = state.fullNameError,
                isValid = state.isFullNameValid
            )

            Spacer(Modifier.height(20.dp))

            AuthTextField(
                label = "Email",
                value = state.email,
                onValueChange = onEmailChange,
                leadingIcon = Icons.Outlined.Email,
                placeholder = "you@example.com",
                error = state.emailError,
                isValid = state.isEmailValid,
                keyboardType = KeyboardType.Email
            )

            Spacer(Modifier.height(20.dp))

            AuthPasswordField(
                label = "Password",
                value = state.password,
                onValueChange = onPasswordChange,
                leadingIcon = Icons.Outlined.Lock,
                placeholder = "Create a password",
                error = state.passwordError,
                imeAction = ImeAction.Next
            )
            if (state.passwordError == null) {
                Text(
                    text = "At least $MIN_PASSWORD_LENGTH characters with letters and numbers",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkHint,
                    modifier = Modifier.padding(start = 4.dp, top = 6.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            AuthPasswordField(
                label = "Confirm password",
                value = state.confirmPassword,
                onValueChange = onConfirmPasswordChange,
                leadingIcon = Icons.Outlined.Lock,
                placeholder = "Repeat your password",
                error = state.confirmPasswordError,
                imeAction = ImeAction.Done,
                onImeAction = {
                    focusManager.clearFocus()
                    onSignUpClick()
                }
            )

            Spacer(Modifier.height(28.dp))

            PrimaryButton(
                text = "Create Account",
                onClick = {
                    focusManager.clearFocus()
                    onSignUpClick()
                },
                isLoading = state.isLoading
            )
        }

        Spacer(Modifier.height(28.dp))

        AuthFooterLink(
            prompt = "Already have an account?",
            action = "Log in",
            onClick = onLogIn
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SignUpScreenPreview() {
    MobileFinanceTheme {
        SignUpScreen(
            state = SignUpUiState(fullName = "Maya Chen", email = "maya.chen@mail.com"),
            onFullNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onSignUpClick = {},
            onLogIn = {}
        )
    }
}
