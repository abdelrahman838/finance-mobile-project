package com.example.mobilefinance.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourapp.ui.theme.*

// Transaction Category Enum
enum class TransactionCategory(val label: String, val icon: ImageVector) {
    FOOD("Food", Icons.Outlined.Restaurant),
    GROCERIES("Groceries", Icons.Outlined.ShoppingBag),
    TRANSPORT("Transport", Icons.Outlined.DirectionsCar),
    MORE("More", Icons.Outlined.MoreHoriz)
}

@Composable
fun AddTransactionScreen(
    onBackClick: () -> Unit = {},
    onSaveTransaction: (
        merchant: String,
        amount: Double,
        isExpense: Boolean,
        category: TransactionCategory,
        dateTime: String,
        description: String
    ) -> Unit = { _, _, _, _, _, _ -> }
) {
    // Variable data states for the new transaction
    var merchant by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var isExpense by remember { mutableStateOf(true) }
    var selectedCategory by remember { mutableStateOf(TransactionCategory.GROCERIES) }
    var dateTime by remember { mutableStateOf("Oct 24, 2026 · 10:30 AM") }
    var description by remember { mutableStateOf("") }

    Scaffold(
        containerColor = ScreenBg
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
            AddTransactionTopBar(
                onBackClick = onBackClick
            )

            // Expense / Income Toggle
            ExpenseIncomeToggle(
                isExpense = isExpense,
                onTypeChanged = { isExpense = it }
            )

            // Amount Input Card
            AmountInputCard(
                amount = amountText,
                onAmountChange = { amountText = it },
                isExpense = isExpense
            )

            // Merchant / Title Input
            OutlinedTextField(
                value = merchant,
                onValueChange = { merchant = it },
                label = { Text("Merchant / Title", color = Muted) },
                placeholder = { Text("e.g. Whole Foods, Coffee Shop") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = BrandGreen,
                    unfocusedBorderColor = CardBorder,
                    focusedTextColor = Ink,
                    unfocusedTextColor = Ink
                )
            )

            // Category Selection Section
            Text(
                text = "Select Category",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Ink
            )

            CategoryGrid(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )

            // Date & Time Input
            OutlinedTextField(
                value = dateTime,
                onValueChange = { dateTime = it },
                label = { Text("Date & Time", color = Muted) },
                singleLine = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Select Date",
                        tint = BrandGreen
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = BrandGreen,
                    unfocusedBorderColor = CardBorder,
                    focusedTextColor = Ink,
                    unfocusedTextColor = Ink
                )
            )

            // Description / Notes Input
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description / Notes", color = Muted) },
                placeholder = { Text("Add any extra notes here...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = BrandGreen,
                    unfocusedBorderColor = CardBorder,
                    focusedTextColor = Ink,
                    unfocusedTextColor = Ink
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Save Transaction Button
            Button(
                onClick = {
                    val parsedAmount = amountText.toDoubleOrNull() ?: 0.0

                    // =====================================================================
                    // TODO: ROOM DATABASE & VIEWMODEL CONNECTION
                    // ---------------------------------------------------------------------
                    // Once your Room Database DAO (e.g., TransactionDao) and ViewModel are
                    // implemented, invoke your ViewModel insertion method here to commit the
                    // transaction data to SQLite/Room DB.
                    //
                    // Example implementation:
                    // val transactionEntity = TransactionEntity(
                    //     userId = currentUserId, // fetched from session or user repository
                    //     amount = if (isExpense) -parsedAmount else parsedAmount,
                    //     description = "$merchant: $description"
                    // )
                    // transactionViewModel.insertTransaction(transactionEntity)
                    // =====================================================================

                    onSaveTransaction(
                        merchant,
                        parsedAmount,
                        isExpense,
                        selectedCategory,
                        dateTime,
                        description
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandGreen,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Transaction",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
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
        border = BorderStroke(1.dp, CardBorder),
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
fun AmountInputCard(
    amount: String,
    onAmountChange: (String) -> Unit,
    isExpense: Boolean
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = SummaryCardBg,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isExpense) "EXPENSE AMOUNT" else "INCOME AMOUNT",
                color = Muted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$",
                    color = Lime,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                TextField(
                    value = amount,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            onAmountChange(newValue)
                        }
                    },
                    placeholder = {
                        Text(
                            text = "0.00",
                            color = Muted,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Lime,
                        unfocusedTextColor = Lime
                    )
                )
            }
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
                indicatorColor = Red,
                onClick = { onTypeChanged(true) },
                modifier = Modifier.weight(1f)
            )

            ToggleTab(
                text = "Income",
                isSelected = !isExpense,
                indicatorColor = BrandGreen,
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
                color = if (isSelected) Ink else Muted,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun CategoryGrid(
    selectedCategory: TransactionCategory,
    onCategorySelected: (TransactionCategory) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TransactionCategory.entries.forEach { category ->
            val isSelected = category == selectedCategory
            Surface(
                onClick = { onCategorySelected(category) },
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) BrandGreen else Color.White,
                border = BorderStroke(1.dp, if (isSelected) BrandGreen else CardBorder),
                modifier = Modifier
                    .weight(1f)
                    .height(72.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = category.label,
                        tint = if (isSelected) Color.White else Ink,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = category.label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Ink
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AddTransactionScreenPreview() {
    AddTransactionScreen()
}
