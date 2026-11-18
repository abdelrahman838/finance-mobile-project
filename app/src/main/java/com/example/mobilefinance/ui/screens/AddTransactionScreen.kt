package com.example.mobilefinance.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mobilefinance.data.local.dao.TransactionDao
import com.example.mobilefinance.data.local.entity.Transaction
import com.example.mobilefinance.data.local.entity.TransactionCategory as EntityCategory
import com.example.mobilefinance.data.local.entity.TransactionType as EntityType
import com.example.mobilefinance.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

enum class TransactionCategory(val label: String, val icon: ImageVector) {
    FOOD("Food", Icons.Outlined.Restaurant),
    TRANSPORT("Transport", Icons.Outlined.DirectionsCar),
    BILLS("Bills", Icons.Outlined.Receipt),
    SHOPPING("Shopping", Icons.Outlined.ShoppingBag),
    FUN("Fun", Icons.Outlined.LocalActivity),
    HEALTH("Health", Icons.Outlined.MonitorHeart),
    TRAVEL("Travel", Icons.Outlined.Flight),
    MORE("More", Icons.Outlined.MoreHoriz)
}

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
}

class AddTransactionScreenViewModel(
    private val transactionDao: TransactionDao? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    init {
        initDefaultDateTime()
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
        _uiState.update { it.copy(merchantName = name, merchantError = null) }
    }

    fun onTypeChanged(isExpense: Boolean) {
        _uiState.update { it.copy(isExpense = isExpense) }
    }

    fun onAmountChange(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*$"))) {
            _uiState.update { it.copy(amount = value, amountError = null) }
        }
    }

    fun onCategorySelected(category: TransactionCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
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
                _uiState.update { it.copy(isLoading = true) }
                val entityType = if (state.isExpense) EntityType.EXPENSE else EntityType.INCOME
                val entityCategory = mapUiCategoryToEntity(state.selectedCategory)

                val newTransaction = Transaction(
                    userId = state.userId,
                    type = entityType,
                    amount = validAmount,
                    category = entityCategory,
                    title = state.merchantName.trim(),
                    description = state.description.trim(),
                    date = state.rawDate,
                    time = state.rawTime
                )

                dao.insertTransaction(newTransaction)
                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            }
        } else {
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

    companion object {
        fun provideFactory(dao: TransactionDao): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                    return AddTransactionScreenViewModel(dao) as T
                }
            }
    }
}

@Composable
fun AddTransactionRoute(
    viewModel: AddTransactionScreenViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            Toast.makeText(context, "Transaction added successfully", Toast.LENGTH_SHORT).show()
            viewModel.onSaveHandled()
            onNavigateBack()
        }
    }

    AddTransactionScreen(
        state = state,
        onBackClick = onNavigateBack,
        onMerchantChange = viewModel::onMerchantChange,
        onTypeChanged = viewModel::onTypeChanged,
        onAmountChange = viewModel::onAmountChange,
        onCategorySelected = viewModel::onCategorySelected,
        onDescriptionChange = viewModel::onDescriptionChange,
        onClearDescription = viewModel::onClearDescription,
        onOpenDatePicker = viewModel::onOpenDatePicker,
        onDismissDatePicker = viewModel::onDismissDatePicker,
        onDatePicked = viewModel::onDatePicked,
        onDismissTimePicker = viewModel::onDismissTimePicker,
        onTimePicked = viewModel::onTimePicked,
        onSaveClick = viewModel::saveTransaction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    state: AddTransactionUiState = AddTransactionUiState(),
    onBackClick: () -> Unit = {},
    onMerchantChange: (String) -> Unit = {},
    onTypeChanged: (Boolean) -> Unit = {},
    onAmountChange: (String) -> Unit = {},
    onCategorySelected: (TransactionCategory) -> Unit = {},
    onDescriptionChange: (String) -> Unit = {},
    onClearDescription: () -> Unit = {},
    onOpenDatePicker: () -> Unit = {},
    onDismissDatePicker: () -> Unit = {},
    onDatePicked: (Long?) -> Unit = {},
    onDismissTimePicker: () -> Unit = {},
    onTimePicked: (Int, Int) -> Unit = { _, _ -> },
    onSaveClick: () -> Unit = {}
) {
    Scaffold(
        containerColor = Background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Top Bar
            AddTransactionTopBar(onBackClick = onBackClick)

            // Summary Card Preview
            TransactionSummaryCard(
                merchantName = if (state.merchantName.isBlank()) "New Transaction" else state.merchantName,
                subtitle = state.subtitle,
                amountText = state.summaryAmountText,
                icon = state.selectedCategory.icon
            )

            // Expense / Income Toggle
            ExpenseIncomeToggle(
                isExpense = state.isExpense,
                onTypeChanged = onTypeChanged
            )

            // Merchant / Title Input Field
            MerchantInputField(
                merchant = state.merchantName,
                onMerchantChange = onMerchantChange,
                errorMessage = state.merchantError
            )

            // Amount Input Field
            AmountInputField(
                amount = state.amount,
                onAmountChange = onAmountChange,
                errorMessage = state.amountError
            )

            // Category Selector Row
            CategorySelectorRow(
                selectedCategory = state.selectedCategory,
                onCategorySelected = onCategorySelected
            )

            // Date & Time Picker Field
            DateTimeField(
                dateTime = state.dateTime,
                onClick = onOpenDatePicker
            )

            // Description / Notes Field
            DescriptionField(
                description = state.description,
                onDescriptionChange = onDescriptionChange,
                onClearClick = onClearDescription
            )

            // Save Transaction Button
            SaveTransactionButton(
                onClick = onSaveClick,
                isLoading = state.isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // 1. Date Picker Dialog
        if (state.showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = state.tempDateMillis ?: System.currentTimeMillis(),
                selectableDates = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        return utcTimeMillis <= System.currentTimeMillis()
                    }
                }
            )

            DatePickerDialog(
                onDismissRequest = onDismissDatePicker,
                confirmButton = {
                    TextButton(onClick = {
                        onDatePicked(datePickerState.selectedDateMillis)
                    }) {
                        Text("Next")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissDatePicker) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // 2. Time Picker Dialog
        if (state.showTimePicker) {
            val timePickerState = rememberTimePickerState()

            AlertDialog(
                onDismissRequest = onDismissTimePicker,
                confirmButton = {
                    TextButton(onClick = {
                        onTimePicked(timePickerState.hour, timePickerState.minute)
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissTimePicker) {
                        Text("Cancel")
                    }
                },
                text = {
                    TimePicker(state = timePickerState)
                }
            )
        }
    }
}

@Composable
fun AddTransactionTopBar(
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularIconButton(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = "Back",
            onClick = onBackClick
        )

        Text(
            text = "Add Transaction",
            color = Ink,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.size(44.dp))
    }
}

@Composable
fun CircularIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color.White,
        border = BorderStroke(1.dp, Outline),
        modifier = Modifier.size(44.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Ink,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
fun TransactionSummaryCard(
    merchantName: String,
    subtitle: String,
    amountText: String,
    icon: ImageVector
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = SurfaceDark,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Peach,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "Category Icon",
                        tint = Orange,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = merchantName,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = InkMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = amountText,
                color = Lime,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ExpenseIncomeToggle(
    isExpense: Boolean,
    onTypeChanged: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = ChipGray,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToggleTab(
                text = "Expense",
                isSelected = isExpense,
                indicatorColor = ExpenseRed,
                onClick = { onTypeChanged(true) },
                modifier = Modifier.weight(1f)
            )

            ToggleTab(
                text = "Income",
                isSelected = !isExpense,
                indicatorColor = Green700,
                onClick = { onTypeChanged(false) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ToggleTab(
    text: String,
    isSelected: Boolean,
    indicatorColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) Color.White else Color.Transparent,
        shadowElevation = if (isSelected) 1.dp else 0.dp,
        modifier = modifier.fillMaxHeight()
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(indicatorColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = if (isSelected) Ink else InkMuted,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun MerchantInputField(
    merchant: String,
    onMerchantChange: (String) -> Unit,
    errorMessage: String? = null
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "MERCHANT / TITLE",
            color = InkMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp)
        )

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = BorderStroke(2.dp, if (errorMessage != null) ExpenseRed else Green700),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Store,
                    contentDescription = "Merchant",
                    tint = InkMuted,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                BasicTextField(
                    value = merchant,
                    onValueChange = onMerchantChange,
                    textStyle = TextStyle(
                        color = Ink,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = ExpenseRed,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
fun AmountInputField(
    amount: String,
    onAmountChange: (String) -> Unit,
    errorMessage: String? = null
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "AMOUNT",
            color = InkMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp)
        )

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = BorderStroke(2.dp, if (errorMessage != null) ExpenseRed else Green700),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$",
                    color = InkMuted,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(8.dp))

                BasicTextField(
                    value = amount,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            onAmountChange(newValue)
                        }
                    },
                    textStyle = TextStyle(
                        color = Ink,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Green100
                ) {
                    Text(
                        text = "USD",
                        color = Green700,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = ExpenseRed,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
fun CategorySelectorRow(
    selectedCategory: TransactionCategory,
    onCategorySelected: (TransactionCategory) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "CATEGORY",
            color = InkLabel,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(start = 4.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TransactionCategory.entries.forEach { category ->
                CategoryCard(
                    category = category,
                    isSelected = category == selectedCategory,
                    onClick = { onCategorySelected(category) }
                )
            }
        }
    }
}

@Composable
fun CategoryCard(
    category: TransactionCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val cardBg = if (isSelected) Ink else Surface
    val cardBorder = if (isSelected) null else BorderStroke(1.dp, Outline)
    val iconBg = if (isSelected) Color(0xFF2D362F) else Background
    val iconTint = if (isSelected) Lime else Ink
    val textColor = if (isSelected) Surface else Ink

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = cardBg,
        border = cardBorder,
        modifier = Modifier.size(width = 84.dp, height = 100.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = iconBg,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = category.label,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = category.label,
                color = textColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun DateTimeField(
    dateTime: String,
    onClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "DATE & TIME",
            color = InkLabel,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(start = 4.dp)
        )

        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            color = Surface,
            border = BorderStroke(1.dp, Outline),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarToday,
                    contentDescription = "Calendar",
                    tint = InkMuted,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = dateTime,
                    color = Ink,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowDown,
                    contentDescription = "Select Date",
                    tint = InkMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun DescriptionField(
    description: String,
    onDescriptionChange: (String) -> Unit,
    onClearClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "DESCRIPTION",
            color = InkLabel,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(start = 4.dp)
        )

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Surface,
            border = BorderStroke(1.dp, Outline),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Edit Description",
                    tint = InkMuted,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                BasicTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    textStyle = TextStyle(
                        color = Ink,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                if (description.isNotEmpty()) {
                    IconButton(
                        onClick = onClearClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Clear Description",
                            tint = InkMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SaveTransactionButton(
    onClick: () -> Unit,
    isLoading: Boolean
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = Green700,
            contentColor = Surface
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Surface,
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = "Save",
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Save Transaction",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AddTransactionScreenPreview() {
    AddTransactionScreen()
}
