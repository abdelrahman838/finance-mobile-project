package com.example.mobilefinance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourapp.ui.theme.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingBag

enum class TransactionCategory(val label: String, val icon: ImageVector) {
    FOOD("Food", Icons.Outlined.Restaurant),
    GROCERIES("Groceries", Icons.Outlined.ShoppingBag),
    TRANSPORT("Transport", Icons.Outlined.DirectionsCar),
    MORE("More", Icons.Outlined.MoreHoriz)
}

@Composable
fun EditTransactionScreen(
    // Initial values passed when a transaction is selected
    initialMerchant: String = "Whole Foods\nMarket",
    initialIsExpense: Boolean = true,
    initialAmount: String = "84.20",
    initialCategory: TransactionCategory = TransactionCategory.GROCERIES,
    initialDateTime: String = "Oct 23, 2026 · 2:40 PM",
    initialDescription: String = "Weekly groceries + oat milk",
    onBackClick: () -> Unit = {},
    onMoreClick: () -> Unit = {}
) {
    // Editable state that updates the UI automatically when changed:
    var isExpense by remember { mutableStateOf(initialIsExpense) }
    var amount by remember { mutableStateOf(initialAmount) }
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var dateTime by remember { mutableStateOf(initialDateTime) }
    var description by remember { mutableStateOf(initialDescription) }

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
            EditTransactionTopBar(
                onBackClick = onBackClick,
                onMoreClick = onMoreClick
            )

            // Summary Card
            val typeLabel = if (isExpense) "Expense" else "Income"
            val sign = if (isExpense) "-" else "+"
            val displayAmount = amount.ifEmpty { "0.00" }

            TransactionSummaryCard(
                merchantName = initialMerchant,
                subtitle = "$typeLabel · ${selectedCategory.label}",
                amountText = "$sign$$displayAmount",
                icon = selectedCategory.icon
            )

            // Expense Type Switch
            ExpenseIncomeToggle(
                isExpense = isExpense,
                onTypeChanged = { isExpense = it }
            )
        }
    }
}

@Composable
fun EditTransactionTopBar(
    onBackClick: () -> Unit,
    onMoreClick: () -> Unit
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
            text = "Edit transaction",
            color = Ink,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        CircularIconButton(
            icon = Icons.Default.MoreHoriz,
            contentDescription = "More options",
            onClick = onMoreClick
        )
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
fun TransactionSummaryCard(
    merchantName: String,
    subtitle: String,
    amountText: String,
    icon: ImageVector
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = SummaryCardBg,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Box
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

            // Title
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
                    color = Muted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Amount 321
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
            // Expense Tab
            ToggleTab(
                text = "Expense",
                isSelected = isExpense,
                indicatorColor = Red,
                onClick = { onTypeChanged(true) },
                modifier = Modifier.weight(1f)
            )

            // Income Tab
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
            // Only show the dot indicator when the tab is selected
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EditTransactionScreenPreview() {
    EditTransactionScreen()
}