package com.example.mobilefinance.ui.screens.signup

import android.database.sqlite.SQLiteConstraintException
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mobilefinance.data.local.dao.UserDao
import com.example.mobilefinance.data.local.entity.User
import com.example.mobilefinance.ui.screens.login.PasswordHasher
import com.example.mobilefinance.ui.screens.login.normalizeEmail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val MIN_PASSWORD_LENGTH = 8

data class SignUpUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val fullNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val formError: String? = null,
    val isLoading: Boolean = false,
    val isSignedUp: Boolean = false
) {
    val isEmailValid: Boolean
        get() = Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val isFullNameValid: Boolean
        get() = fullName.trim().length >= 2
}

class SignUpViewModel(private val userDao: UserDao) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun onFullNameChange(value: String) {
        _uiState.update { it.copy(fullName = value, fullNameError = null, formError = null) }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null, formError = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, formError = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value, confirmPasswordError = null, formError = null) }
    }

    fun signUp() {
        val s = _uiState.value
        if (s.isLoading) return

        val fullNameError = when {
            s.fullName.isBlank() -> "Enter your full name"
            !s.isFullNameValid -> "Name is too short"
            else -> null
        }
        val emailError = when {
            s.email.isBlank() -> "Enter your email"
            !s.isEmailValid -> "Enter a valid email address"
            else -> null
        }
        val passwordError = when {
            s.password.length < MIN_PASSWORD_LENGTH -> "Use at least $MIN_PASSWORD_LENGTH characters"
            s.password.none { it.isDigit() } || s.password.none { it.isLetter() } ->
                "Use both letters and numbers"
            else -> null
        }
        val confirmError = when {
            s.confirmPassword.isEmpty() -> "Confirm your password"
            s.confirmPassword != s.password -> "Passwords don't match"
            else -> null
        }

        if (listOf(fullNameError, emailError, passwordError, confirmError).any { it != null }) {
            _uiState.update {
                it.copy(
                    fullNameError = fullNameError,
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmError
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, formError = null) }
        viewModelScope.launch {
            val username = normalizeEmail(s.email)
            try {
                if (userDao.isUsernameTaken(username)) {
                    _uiState.update {
                        it.copy(isLoading = false, emailError = "An account with this email already exists")
                    }
                    return@launch
                }
                userDao.registerUser(
                    User(
                        name = s.fullName.trim(),
                        username = username,
                        hashedPassword = PasswordHasher.hash(s.password)
                    )
                )
            } catch (e: SQLiteConstraintException) {
                _uiState.update {
                    it.copy(isLoading = false, emailError = "An account with this email already exists")
                }
                return@launch
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, formError = "Something went wrong. Please try again.") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = false, isSignedUp = true) }
        }
    }

    fun onSignUpHandled() {
        _uiState.update { it.copy(isSignedUp = false) }
    }

    companion object {
        fun provideFactory(userDao: UserDao): ViewModelProvider.Factory = viewModelFactory {
            initializer { SignUpViewModel(userDao) }
        }
    }
}
