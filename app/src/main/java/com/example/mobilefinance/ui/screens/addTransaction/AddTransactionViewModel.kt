package com.example.mobilefinance.ui.screens.addTransaction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.mobilefinance.data.local.dao.TransactionDao
import com.example.mobilefinance.data.local.entity.Transaction
import com.example.mobilefinance.data.local.entity.TransactionCategory as EntityCategory
import com.example.mobilefinance.data.local.entity.TransactionType as EntityType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class AddTransactionUiState(
    val userId: Long = 1L,
    val merchantName: String = "",
    val isExpense: Boolean = true,
    val amount: String = "",
    val selectedCategory: TransactionCategory = TransactionCategory.FOOD,
    val dateTime: String = "",
    val description: String = "",
    val rawDate: String = "",
    val rawTime: String = "",

    // Dialog & Picker States
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false,
    val tempDateMillis: Long? = null,

    // Error & Feedback States
    val amountError: String? = null,
    val merchantError: String? = null,
    val formError: String? = null,

    // Operation / Navigation Flags
    val isLoading: Boolean = false,
    val isSaved: Boolean = false
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

class AddTransactionViewModel(
    private val savedStateHandle: SavedStateHandle = SavedStateHandle(),
    private val transactionDao: TransactionDao? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    init {
        val userId = savedStateHandle.get<Long>("userId")
            ?: savedStateHandle.get<Int>("userId")?.toLong()
            ?: savedStateHandle.get<String>("userId")?.toLongOrNull()
            ?: 1L

        _uiState.update { it.copy(userId = userId) }
        initDefaultDateTime()
    }

    fun setUserId(userId: Long) {
        _uiState.update { it.copy(userId = userId) }
    }

    private fun initDefaultDateTime() {
        val now = Date()
        val displayDateFormatter = SimpleDateFormat("MMM dd, yyyy · h:mm a", Locale.getDefault())
        val roomDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val roomTimeFormatter = SimpleDateFormat("HH:mm", Locale.US)

        _uiState.update {
            it.copy(
                dateTime = displayDateFormatter.format(now),
                rawDate = roomDateFormatter.format(now),
                rawTime = roomTimeFormatter.format(now)
            )
        }
    }

    fun onMerchantChange(name: String) {
        _uiState.update { it.copy(merchantName = name, merchantError = null, formError = null) }
    }

    fun onTypeChanged(isExpense: Boolean) {
        _uiState.update { it.copy(isExpense = isExpense) }
    }

    fun onAmountChange(value: String) {
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
        val dateMillis = _uiState.value.tempDateMillis ?: System.currentTimeMillis()

        val displayDateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val displayDate = displayDateFormatter.format(Date(dateMillis))

        val roomDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val roomDate = roomDateFormatter.format(Date(dateMillis))

        val isPm = hour >= 12
        val displayHour = if (hour % 12 == 0) 12 else hour % 12
        val displayMinute = String.format(Locale.getDefault(), "%02d", minute)
        val amPm = if (isPm) "PM" else "AM"
        val formattedDateTime = "$displayDate · $displayHour:$displayMinute $amPm"
        val roomTime = String.format(Locale.US, "%02d:%02d", hour, minute)

        _uiState.update {
            it.copy(
                dateTime = formattedDateTime,
                rawDate = roomDate,
                rawTime = roomTime,
                showTimePicker = false,
                tempDateMillis = null
            )
        }
    }

    fun saveTransaction() {
        val state = _uiState.value
        if (state.isLoading) return

        val amountVal = state.amount.toDoubleOrNull()
        val amountError = when {
            state.amount.isBlank() -> "Enter an amount"
            amountVal == null || amountVal <= 0.0 -> "Enter an amount greater than 0"
            else -> null
        }
        val merchantError = if (state.merchantName.isBlank()) "Merchant / Title cannot be empty" else null

        if (amountError != null || merchantError != null) {
            _uiState.update {
                it.copy(
                    amountError = amountError,
                    merchantError = merchantError
                )
            }
            return
        }

        val validAmount = amountVal ?: return
        val dao = transactionDao

        if (dao != null) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, formError = null) }
                try {
                    val (extractedDate, extractedTime) = if (state.rawDate.isNotEmpty() && state.rawTime.isNotEmpty()) {
                        Pair(state.rawDate, state.rawTime)
                    } else {
                        splitDisplayDateTime(state.dateTime)
                    }

                    val entityType = if (state.isExpense) EntityType.EXPENSE else EntityType.INCOME
                    val entityCategory = mapUiCategoryToEntity(state.selectedCategory)

                    val newTransaction = Transaction(
                        userId = state.userId,
                        type = entityType,
                        amount = validAmount,
                        category = entityCategory,
                        title = state.merchantName.trim(),
                        description = state.description.trim(),
                        date = extractedDate,
                        time = extractedTime
                    )

                    dao.insertTransaction(newTransaction)
                    _uiState.update { it.copy(isLoading = false, isSaved = true) }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            formError = e.message ?: "Failed to save transaction"
                        )
                    }
                }
            }
        } else {
            // Standalone / mock fallback
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    fun onSaveHandled() {
        _uiState.update { it.copy(isSaved = false) }
    }

    private fun mapUiCategoryToEntity(uiCat: TransactionCategory): EntityCategory {
        return EntityCategory.entries.find { it.name.equals(uiCat.name, ignoreCase = true) }
            ?: EntityCategory.FOOD
    }

    private fun splitDisplayDateTime(displayStr: String): Pair<String, String> {
        return try {
            val parser = SimpleDateFormat("MMM dd, yyyy · h:mm a", Locale.getDefault())
            val dateObj = parser.parse(displayStr.trim()) ?: Date()
            val dateOut = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(dateObj)
            val timeOut = SimpleDateFormat("HH:mm", Locale.US).format(dateObj)
            Pair(dateOut, timeOut)
        } catch (e: Exception) {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val nowTime = SimpleDateFormat("HH:mm", Locale.US).format(Date())
            Pair(today, nowTime)
        }
    }

    companion object {
        fun provideFactory(dao: TransactionDao): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                    val savedStateHandle = extras.createSavedStateHandle()
                    return AddTransactionViewModel(savedStateHandle, dao) as T
                }
            }
    }
}

typealias AddTransactionScreenViewModel = AddTransactionViewModel
