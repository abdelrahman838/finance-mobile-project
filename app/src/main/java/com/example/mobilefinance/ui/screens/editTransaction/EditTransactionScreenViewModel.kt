package com.example.mobilefinance.ui.screens.editTransaction

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

data class EditTransactionUiState(
    val transactionId: Long = 0L,
    val userId: Long = 1L,
    val merchantName: String = "Whole Foods\nMarket",
    val isExpense: Boolean = true,
    val amount: String = "84.20",
    val selectedCategory: TransactionCategory = TransactionCategory.SHOPPING,
    val dateTime: String = "Oct 23, 2026 · 2:40 PM",
    val description: String = "Weekly groceries + oat milk",
    val rawDate: String = "",
    val rawTime: String = "",

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

// ViewModel for Edit Transaction screen.
class EditTransactionScreenViewModel(
    private val savedStateHandle: SavedStateHandle = SavedStateHandle(),
    private val transactionDao: TransactionDao? = null
) : ViewModel() {

    private var loadedTransaction: Transaction? = null

    private val _uiState = MutableStateFlow(EditTransactionUiState())
    val uiState: StateFlow<EditTransactionUiState> = _uiState.asStateFlow()

    init {
        // Read transactionId passed via Navigation argument (Long, Int, or String)
        val transactionId = savedStateHandle.get<Long>("transactionId")
            ?: savedStateHandle.get<Int>("transactionId")?.toLong()
            ?: savedStateHandle.get<String>("transactionId")?.toLongOrNull()

        if (transactionId != null && transactionId > 0L) {
            loadTransactionById(transactionId)
        }
    }

    //Loads transaction from Room
    fun loadTransactionById(id: Long) {
        val dao = transactionDao ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val tx = dao.getTransactionById(id)
            if (tx != null) {
                loadedTransaction = tx
                val uiCat = mapEntityCategoryToUi(tx.category)
                val formattedDateTime = formatToDisplayDateTime(tx.date, tx.time)
                _uiState.update {
                    it.copy(
                        transactionId = tx.transactionId,
                        userId = tx.userId,
                        merchantName = tx.title,
                        amount = String.format(Locale.US, "%.2f", tx.amount),
                        isExpense = tx.type == EntityType.EXPENSE,
                        selectedCategory = uiCat,
                        dateTime = formattedDateTime,
                        description = tx.description,
                        rawDate = tx.date,
                        rawTime = tx.time,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, formError = "Transaction not found") }
            }
        }
    }

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
        val dateMillis = _uiState.value.tempDateMillis ?: System.currentTimeMillis()

        // Format for display: MMM dd, yyyy
        val displayDateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val displayDate = displayDateFormatter.format(Date(dateMillis))

        // Format for Room: yyyy-MM-dd
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

        val validAmount = amountVal ?: return

        val dao = transactionDao
        if (dao != null && state.transactionId > 0L) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                val (extractedDate, extractedTime) = if (state.rawDate.isNotEmpty() && state.rawTime.isNotEmpty()) {
                    Pair(state.rawDate, state.rawTime)
                } else {
                    splitDisplayDateTime(state.dateTime)
                }

                val entityType = if (state.isExpense) EntityType.EXPENSE else EntityType.INCOME
                val entityCategory = mapUiCategoryToEntity(state.selectedCategory)

                val updated = loadedTransaction?.copy(
                    title = state.merchantName.trim(),
                    amount = validAmount,
                    type = entityType,
                    category = entityCategory,
                    description = state.description.trim(),
                    date = extractedDate,
                    time = extractedTime
                ) ?: Transaction(
                    transactionId = state.transactionId,
                    userId = state.userId,
                    type = entityType,
                    amount = validAmount,
                    category = entityCategory,
                    title = state.merchantName.trim(),
                    description = state.description.trim(),
                    date = extractedDate,
                    time = extractedTime
                )

                dao.updateTransaction(updated)
                _uiState.update { it.copy(isLoading = false, isUpdated = true) }
            }
        } else {
            // Standalone / mock fallback
            _uiState.update { it.copy(isUpdated = true) }
        }
    }

    fun deleteTransaction() {
        val state = _uiState.value
        if (state.isLoading) return

        val dao = transactionDao
        if (dao != null && state.transactionId > 0L) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                val toDelete = loadedTransaction ?: dao.getTransactionById(state.transactionId)
                if (toDelete != null) {
                    dao.deleteTransaction(toDelete)
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        showDeleteConfirmation = false,
                        isDeleted = true
                    )
                }
            }
        } else {
            // Standalone / mock fallback
            _uiState.update {
                it.copy(
                    showDeleteConfirmation = false,
                    isDeleted = true
                )
            }
        }
    }

    fun onUpdateHandled() {
        _uiState.update { it.copy(isUpdated = false) }
    }

    fun onDeleteHandled() {
        _uiState.update { it.copy(isDeleted = false) }
    }

    private fun mapEntityCategoryToUi(entityCat: EntityCategory): TransactionCategory {
        return TransactionCategory.entries.find { it.name.equals(entityCat.name, ignoreCase = true) }
            ?: TransactionCategory.MORE
    }

    private fun mapUiCategoryToEntity(uiCat: TransactionCategory): EntityCategory {
        return EntityCategory.entries.find { it.name.equals(uiCat.name, ignoreCase = true) }
            ?: EntityCategory.MORE
    }

    private fun formatToDisplayDateTime(date: String, time: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            val dateObj = parser.parse("${date.trim()} ${time.trim()}")
            val formatter = SimpleDateFormat("MMM dd, yyyy · h:mm a", Locale.getDefault())
            formatter.format(dateObj ?: Date())
        } catch (e: Exception) {
            "${date.trim()} · ${time.trim()}"
        }
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
        fun provideFactory(
            dao: TransactionDao
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val savedStateHandle = extras.createSavedStateHandle()
                return EditTransactionScreenViewModel(savedStateHandle, dao) as T
            }
        }
    }
}
