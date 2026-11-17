package com.example.mobilefinance.ui.screens.transactions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.mobilefinance.ui.theme.Green700
import com.example.mobilefinance.ui.theme.ChipGray
import com.example.mobilefinance.ui.theme.Green100
import com.example.mobilefinance.ui.theme.InkMuted
import com.example.mobilefinance.ui.theme.Orange
import com.example.mobilefinance.ui.theme.Peach
import com.example.mobilefinance.ui.theme.ExpenseRed
import com.example.mobilefinance.ui.theme.ExpenseRedSoft

data class CategoryStyle(
    val icon: ImageVector,
    val background: Color,
    val tint: Color
)

private val BlueSoft = Color(0xFFE1EEFB)
private val Blue = Color(0xFF2F6FB5)
private val PurpleSoft = Color(0xFFECE8FB)
private val Purple = Color(0xFF6A4FD1)
private val LimeSoft = Color(0xFFEDF6D6)
private val Olive = Color(0xFF6E8A12)

/**
 * Picks an icon + colors from the category name.
 * Categories are plain strings so new ones just get the default look.
 */
fun categoryStyleFor(category: String): CategoryStyle = when (category.trim().lowercase()) {
    "food", "coffee", "restaurant" -> CategoryStyle(Icons.Outlined.LocalCafe, Peach, Orange)
    "groceries" -> CategoryStyle(Icons.Outlined.ShoppingBag, Peach, Orange)
    "transport", "travel" -> CategoryStyle(Icons.Outlined.DirectionsCar, BlueSoft, Blue)
    "bills", "utilities" -> CategoryStyle(Icons.Outlined.Bolt, PurpleSoft, Purple)
    "entertainment" -> CategoryStyle(Icons.Outlined.Movie, ExpenseRedSoft, ExpenseRed)
    "shopping" -> CategoryStyle(Icons.Outlined.ShoppingCart, LimeSoft, Olive)
    "income", "salary" -> CategoryStyle(Icons.Outlined.WorkOutline, Green100, Green700)
    "health" -> CategoryStyle(Icons.Outlined.LocalHospital, ExpenseRedSoft, ExpenseRed)
    "gifts" -> CategoryStyle(Icons.Outlined.CardGiftcard, PurpleSoft, Purple)
    else -> CategoryStyle(Icons.Outlined.ReceiptLong, ChipGray, InkMuted)
}
