package com.example.mobilefinance.ui.screens.editTransaction

import androidx.lifecycle.ViewModel
import com.example.mobilefinance.TransactionCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class EditTransactionUiState(
    val transactionId: Int = 0,
    val merchantName: String = "Whole Foods\nMarket",
    val isExpense: Boolean = true,
    val amount: String = "84.20",
    val selectedCategory: TransactionCategory = TransactionCategory.SHOPPING,
    val dateTime: String = "Oct 23, 2026 · 2:40 PM",
    val description: String = "Weekly groceries + oat milk",

    // Dialog & Picker States
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val tempDateMillis: Long? = null,

    // Error & Feedback States
    val amountError: String? = null,
    val merchantError: String? = null,
    val formError: String? = null,

    // Operation / Navigation Flags
    val isLoading: Boolean = false,
    val isUpdated: Boolean = false,
    val isDeleted: Boolean = false
) {
    val displayAmount: String
        get() = amount.ifEmpty { "0.00" }

    val sign: String
        get() = if (isExpense) "-" else "+"

    val summaryAmountText: String
        get() = "$sign$$displayAmount"

    val typeLabel: String
        get() = if (isExpense) "Expense" else "Income"

    val subtitle: String
        get() = "$typeLabel · ${selectedCategory.label}"

    val isAmountValid: Boolean
        get() = (amount.toDoubleOrNull() ?: 0.0) > 0.0
}

class EditTransactionScreenViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(EditTransactionUiState())
    val uiState: StateFlow<EditTransactionUiState> = _uiState.asStateFlow()

    fun onMerchantChange(name: String) {
        _uiState.update { it.copy(merchantName = name, merchantError = null, formError = null) }
    }

    fun onTypeChanged(isExpense: Boolean) {
        _uiState.update { it.copy(isExpense = isExpense) }
    }

    fun onAmountChange(value: String) {
        // Allows digits and at most one decimal point
        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*$"))) {
            _uiState.update { it.copy(amount = value, amountError = null, formError = null) }
        }
    }

    fun onCategorySelected(category: TransactionCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onDateTimeChange(value: String) {
        _uiState.update { it.copy(dateTime = value) }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun onClearDescription() {
        _uiState.update { it.copy(description = "") }
    }

    // Date & Time Picker Dialog Controls

    fun onOpenDatePicker() {
        _uiState.update { it.copy(showDatePicker = true) }
    }

    fun onDismissDatePicker() {
        _uiState.update { it.copy(showDatePicker = false) }
    }

    fun onDatePicked(dateMillis: Long?) {
        _uiState.update {
            it.copy(
                tempDateMillis = dateMillis,
                showDatePicker = false,
                showTimePicker = true
            )
        }
    }

    fun onDismissTimePicker() {
        _uiState.update { it.copy(showTimePicker = false) }
    }

    fun onTimePicked(hour: Int, minute: Int) {
        val dateStr = _uiState.value.tempDateMillis?.let { millis ->
            val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            formatter.format(Date(millis))
        } ?: ""

        val isPm = hour >= 12
        val displayHour = if (hour % 12 == 0) 12 else hour % 12
        val displayMinute = String.format(Locale.getDefault(), "%02d", minute)
        val amPm = if (isPm) "PM" else "AM"
        val formattedDateTime = if (dateStr.isNotEmpty()) {
            "$dateStr · $displayHour:$displayMinute $amPm"
        } else {
            "$displayHour:$displayMinute $amPm"
        }

        _uiState.update {
            it.copy(
                dateTime = formattedDateTime,
                showTimePicker = false,
                tempDateMillis = null
            )
        }
    }

    // Delete Confirmation Dialog Controls

    fun onShowDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteConfirmation = true) }
    }

    fun onDismissDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteConfirmation = false) }
    }

    // Actions

    fun updateTransaction() {
        val state = _uiState.value
        if (state.isLoading) return

        val amountVal = state.amount.toDoubleOrNull()
        val amountError = when {
            state.amount.isBlank() -> "Enter an amount"
            amountVal == null || amountVal <= 0.0 -> "Enter an amount greater than 0"
            else -> null
        }
        val merchantError = if (state.merchantName.isBlank()) "Merchant name cannot be empty" else null

        if (amountError != null || merchantError != null) {
            _uiState.update {
                it.copy(
                    amountError = amountError,
                    merchantError = merchantError
                )
            }
            return
        }

        _uiState.update { it.copy(isUpdated = true) }
    }

    fun deleteTransaction() {
        val state = _uiState.value
        if (state.isLoading) return

        _uiState.update {
            it.copy(
                showDeleteConfirmation = false,
                isDeleted = true
            )
        }
    }

    fun onUpdateHandled() {
        _uiState.update { it.copy(isUpdated = false) }
    }

    fun onDeleteHandled() {
        _uiState.update { it.copy(isDeleted = false) }
    }

    fun loadTransaction(
        id: Int = 0,
        merchant: String,
        amount: Double,
        isExpense: Boolean,
        category: TransactionCategory,
        dateTime: String,
        description: String
    ) {
        _uiState.update {
            it.copy(
                transactionId = id,
                merchantName = merchant,
                amount = String.format(Locale.US, "%.2f", amount),
                isExpense = isExpense,
                selectedCategory = category,
                dateTime = dateTime,
                description = description
            )
        }
    }
}
