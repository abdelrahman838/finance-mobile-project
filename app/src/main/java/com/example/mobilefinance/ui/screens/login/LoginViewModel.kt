package com.example.mobilefinance.ui.screens.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mobilefinance.data.local.dao.UserDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val formError: String? = null,
    val isLoading: Boolean = false,
    val loggedInUserId: Long? = null
) {
    val isEmailValid: Boolean
        get() = Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
}

class LoginViewModel(private val userDao: UserDao) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null, formError = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, formError = null) }
    }

    fun prefillEmail(email: String) {
        if (_uiState.value.email.isEmpty()) _uiState.update { it.copy(email = email) }
    }

    fun login() {
        val state = _uiState.value
        if (state.isLoading) return

        val emailError = when {
            state.email.isBlank() -> "Enter your email"
            !state.isEmailValid -> "Enter a valid email address"
            else -> null
        }
        val passwordError = if (state.password.isEmpty()) "Enter your password" else null

        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        _uiState.update { it.copy(isLoading = true, formError = null) }
        viewModelScope.launch {
            val user = try {
                userDao.login(normalizeEmail(state.email), PasswordHasher.hash(state.password))
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, formError = "Something went wrong. Please try again.") }
                return@launch
            }

            _uiState.update {
                if (user == null) {
                    it.copy(isLoading = false, formError = "Incorrect email or password")
                } else {
                    it.copy(isLoading = false, loggedInUserId = user.userId)
                }
            }
        }
    }

    fun onLoginHandled() {
        _uiState.update { it.copy(loggedInUserId = null, password = "") }
    }

    companion object {
        fun provideFactory(userDao: UserDao): ViewModelProvider.Factory = viewModelFactory {
            initializer { LoginViewModel(userDao) }
        }
    }
}
