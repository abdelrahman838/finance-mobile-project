package com.example.mobilefinance.ui.screens.transactions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.mobilefinance.ui.screens.editTransaction.TransactionCategory
import com.example.mobilefinance.ui.theme.Background
import com.example.mobilefinance.ui.theme.ChipGray
import com.example.mobilefinance.ui.theme.ExpenseRed
import com.example.mobilefinance.ui.theme.ExpenseRedSoft
import com.example.mobilefinance.ui.theme.Green100
import com.example.mobilefinance.ui.theme.Green500
import com.example.mobilefinance.ui.theme.Green700
import com.example.mobilefinance.ui.theme.Ink
import com.example.mobilefinance.ui.theme.InkMuted
import com.example.mobilefinance.ui.theme.Lime
import com.example.mobilefinance.ui.theme.Orange
import com.example.mobilefinance.ui.theme.Peach

data class CategoryStyle(
    val icon: ImageVector,
    val background: Color,
    val tint: Color
)

/**
 * Icon + colors for a category.
 * Icons come from the app's shared category list (same as the edit screen),
 * colors only from the theme folder.
 */
fun categoryStyleFor(category: String, isIncome: Boolean = false): CategoryStyle {
    if (isIncome) return CategoryStyle(Icons.Outlined.Work, Green100, Green700)

    val shared = TransactionCategory.entries.firstOrNull { it.label.equals(category.trim(), ignoreCase = true) }
        ?: return CategoryStyle(Icons.Outlined.Receipt, Background, InkMuted)

    val (background, tint) = when (shared) {
        TransactionCategory.FOOD -> Peach to Orange
        TransactionCategory.SHOPPING -> Lime to Ink
        TransactionCategory.TRANSPORT, TransactionCategory.TRAVEL -> Green100 to Green500
        TransactionCategory.BILLS -> ChipGray to Ink
        TransactionCategory.FUN, TransactionCategory.HEALTH -> ExpenseRedSoft to ExpenseRed
        TransactionCategory.MORE -> ChipGray to InkMuted
    }
    return CategoryStyle(shared.icon, background, tint)
}
