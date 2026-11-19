package com.example.mobilefinance.ui.screens.dashboard

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalActivity
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mobilefinance.ui.components.clickableNoRipple
import com.example.mobilefinance.ui.theme.Background
import com.example.mobilefinance.ui.theme.Green100
import com.example.mobilefinance.ui.theme.Green700
import com.example.mobilefinance.ui.theme.Green800
import com.example.mobilefinance.ui.theme.Ink
import com.example.mobilefinance.ui.theme.InkMuted
import com.example.mobilefinance.ui.theme.Lime
import com.example.mobilefinance.ui.theme.MobileFinanceTheme
import com.example.mobilefinance.ui.theme.Orange
import com.example.mobilefinance.ui.theme.Outline
import com.example.mobilefinance.ui.theme.Peach
import com.example.mobilefinance.ui.theme.SurfaceDark
import java.text.DateFormatSymbols
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

// Dashboard-only shades of the dark header
private val CardDark = Color(0xFF262E29)
private val BarDark = Color(0xFF3A423D)
private val OnDarkMuted = Color(0xFF8A938D)

enum class DashboardTab { HOME, STATS, SMARTTOY, PROFILE }

@Composable
fun DashboardRoute(
    viewModel: DashboardViewModel,
    onAddTransaction: () -> Unit,
    onSeeAllTransactions: () -> Unit,
    onTransactionClick: (id: Long) -> Unit,
    onTabSelected: (DashboardTab) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    DashboardScreen(
        state = state,
        onMonthSelected = viewModel::onMonthSelected,
        onAddTransaction = onAddTransaction,
        onSeeAllTransactions = onSeeAllTransactions,
        onTransactionClick = onTransactionClick,
        onTabSelected = onTabSelected
    )
}

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onMonthSelected: (Int) -> Unit,
    onAddTransaction: () -> Unit,
    onSeeAllTransactions: () -> Unit,
    onTransactionClick: (id: Long) -> Unit,
    onTabSelected: (DashboardTab) -> Unit
) {
    LightStatusBarIcons()

    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = bottomInset + 120.dp)
        ) {
            item {
                DashboardHeader(
                    state = state,
                    onMonthSelected = onMonthSelected,
                    onAvatarClick = { onTabSelected(DashboardTab.PROFILE) }
                )
            }
            item {
                RecentTransactions(
                    transactions = state.recentTransactions,
                    onSeeAll = onSeeAllTransactions,
                    onTransactionClick = onTransactionClick
                )
            }
        }

        DashboardBottomBar(
            onAddTransaction = onAddTransaction,
            onTabSelected = onTabSelected,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun LightStatusBarIcons() {
    val view = LocalView.current
    if (view.isInEditMode) return
    DisposableEffect(view) {
        val window = (view.context as? Activity)?.window ?: return@DisposableEffect onDispose {}
        val controller = WindowCompat.getInsetsController(window, view)
        val previous = controller.isAppearanceLightStatusBars
        controller.isAppearanceLightStatusBars = false
        onDispose { controller.isAppearanceLightStatusBars = previous }
    }
}

// Header

@Composable
private fun DashboardHeader(
    state: DashboardUiState,
    onMonthSelected: (Int) -> Unit,
    onAvatarClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
            .background(SurfaceDark)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 24.dp)
    ) {
        GreetingRow(
            greeting = state.greeting,
            userName = state.userName,
            onAvatarClick = onAvatarClick
        )
        Spacer(Modifier.height(28.dp))
        BalanceSection(
            balance = state.totalBalance,
            changePercent = state.changeVsLastMonthPercent
        )
        Spacer(Modifier.height(28.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard(
                label = "Income",
                amount = state.income,
                icon = Icons.Filled.SouthWest,
                iconBackground = Lime,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                label = "Expenses",
                amount = state.expenses,
                icon = Icons.Filled.NorthEast,
                iconBackground = Color.White,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(28.dp))
        SpendingOverview(state = state, onMonthSelected = onMonthSelected)
    }
}

@Composable
private fun GreetingRow(
    greeting: String,
    userName: String,
    onAvatarClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Green100)
                .clickableNoRipple(onAvatarClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initialsOf(userName),
                color = Green800,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = greeting, color = OnDarkMuted, fontSize = 14.sp)
            Text(
                text = userName,
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun BalanceSection(balance: Double, changePercent: Double?) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "TOTAL BALANCE",
            color = OnDarkMuted,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(6.dp))

        val whole = formatMoney(balance, withCents = false, truncate = true)
        val cents = ".%02d".format(Locale.US, Math.round(abs(balance) * 100) % 100)
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = Color.White, fontSize = 46.sp, fontWeight = FontWeight.Bold)) {
                    append(whole)
                }
                withStyle(SpanStyle(color = OnDarkMuted, fontSize = 26.sp, fontWeight = FontWeight.Bold)) {
                    append(cents)
                }
            },
            letterSpacing = (-1).sp
        )

        if (changePercent != null) {
            Spacer(Modifier.height(12.dp))
            val isUp = changePercent >= 0
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(CardDark)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isUp) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                    contentDescription = null,
                    tint = Lime,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${if (isUp) "+" else "-"}${"%.1f".format(Locale.US, abs(changePercent))}% vs last month",
                    color = Lime,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(
    label: String,
    amount: Double,
    icon: ImageVector,
    iconBackground: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(CardDark)
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Ink, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(text = label, color = OnDarkMuted, fontSize = 13.sp)
            Text(
                text = formatMoney(amount),
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SpendingOverview(state: DashboardUiState, onMonthSelected: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Spending overview",
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "This year",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .clip(CircleShape)
                .background(CardDark)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
    Spacer(Modifier.height(20.dp))

    SpendingChart(
        monthlySpending = state.monthlySpending,
        selectedMonth = state.selectedMonth,
        onMonthSelected = onMonthSelected
    )
    Spacer(Modifier.height(18.dp))

    val monthName = DateFormatSymbols(Locale.US).months[state.selectedMonth]
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CardDark)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(Lime)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = "$monthName · ${formatMoney(state.selectedMonthSpending)} spent",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        state.monthlyBudget?.let { budget ->
            Text(
                text = "Budget ${formatMoney(budget)}",
                color = OnDarkMuted,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SpendingChart(
    monthlySpending: List<Double>,
    selectedMonth: Int,
    onMonthSelected: (Int) -> Unit
) {
    val maxSpending = monthlySpending.maxOrNull()?.takeIf { it > 0 } ?: 1.0
    val monthInitials = DateFormatSymbols(Locale.US).shortMonths.take(12).map { it.first().toString() }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        monthlySpending.forEachIndexed { month, value ->
            val isSelected = month == selectedMonth
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickableNoRipple { onMonthSelected(month) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .height(150.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight((value / maxSpending).toFloat().coerceIn(0.12f, 1f))
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Lime else BarDark)
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = monthInitials[month],
                    color = if (isSelected) Lime else OnDarkMuted,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

// Transactions

@Composable
private fun RecentTransactions(
    transactions: List<DashboardTransaction>,
    onSeeAll: () -> Unit,
    onTransactionClick: (Long) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(28.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Recent transactions",
                color = Ink,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickableNoRipple(onSeeAll)
                    .padding(start = 8.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "See all", color = Green700, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Green700
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (transactions.isEmpty()) {
                Text(
                    text = "No transactions yet.\nTap + to add your first one.",
                    color = InkMuted,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(24.dp)
                )
            } else {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    transactions.forEachIndexed { index, transaction ->
                        if (index > 0) HorizontalDivider(color = Outline)
                        TransactionRow(transaction, onClick = { onTransactionClick(transaction.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(transaction: DashboardTransaction, onClick: () -> Unit) {
    val style = categoryStyle(transaction)
    val isIncome = transaction.kind == DashboardTransactionKind.INCOME

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickableNoRipple(onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(style.background),
            contentAlignment = Alignment.Center
        ) {
            Icon(style.icon, contentDescription = null, tint = style.tint, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.title,
                color = Ink,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "${transaction.category} · ${relativeDate(transaction.timestampMillis)}",
                color = InkMuted,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = (if (isIncome) "+" else "-") + formatMoney(transaction.amount),
            color = if (isIncome) Green700 else Ink,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private data class CategoryStyle(val icon: ImageVector, val tint: Color, val background: Color)

private fun categoryStyle(transaction: DashboardTransaction): CategoryStyle {
    if (transaction.kind == DashboardTransactionKind.INCOME) {
        return CategoryStyle(Icons.Outlined.Work, Green700, Green100)
    }
    return when (transaction.category.lowercase(Locale.US)) {
        "groceries", "shopping" -> CategoryStyle(Icons.Outlined.ShoppingBag, Orange, Peach)
        "food" -> CategoryStyle(Icons.Outlined.Restaurant, Orange, Peach)
        "transport" -> CategoryStyle(Icons.Outlined.DirectionsCar, Color(0xFF3B6FD8), Color(0xFFE3ECFB))
        "bills" -> CategoryStyle(Icons.Outlined.Bolt, Color(0xFF6A4BD1), Color(0xFFECE7FB))
        "fun" -> CategoryStyle(Icons.Outlined.LocalActivity, Color(0xFFC2417A), Color(0xFFFBE5EF))
        "health" -> CategoryStyle(Icons.Outlined.Favorite, Color(0xFFD13B3B), Color(0xFFFCE8E6))
        "travel" -> CategoryStyle(Icons.Outlined.Flight, Color(0xFF1F8A9B), Color(0xFFDDF2F4))
        else -> CategoryStyle(Icons.Outlined.Receipt, InkMuted, Background)
    }
}

// Bottom bar

@Composable
private fun DashboardBottomBar(
    onAddTransaction: () -> Unit,
    onTabSelected: (DashboardTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(bottom = 12.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            shape = RoundedCornerShape(36.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Outline),
            shadowElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomBarItem(Icons.Outlined.Home, "Home", selected = true) { onTabSelected(DashboardTab.HOME) }
                // BottomBarItem(Icons.Outlined.PieChart, "Statistics") { onTabSelected(DashboardTab.STATS) }
                Spacer(Modifier.weight(1f))
                BottomBarItem(Icons.Outlined.SmartToy, "SmartToy") { onTabSelected(DashboardTab.SMARTTOY) }
                // BottomBarItem(Icons.Outlined.PersonOutline, "Profile") { onTabSelected(DashboardTab.PROFILE) }
            }
        }

        Box(
            modifier = Modifier
                .padding(bottom = 40.dp)
                .size(68.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(Background)
                .padding(4.dp)
                .clip(CircleShape)
                .background(Green700)
                .clickableNoRipple(onAddTransaction),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add transaction", tint = Color.White, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
private fun RowScope.BottomBarItem(
    icon: ImageVector,
    contentDescription: String,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickableNoRipple(onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = if (selected) Green700 else InkMuted,
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(if (selected) Green700 else Color.Transparent)
        )
    }
}

// Formatting

private val usSymbols = DecimalFormatSymbols(Locale.US)

/** "$4,820" for large whole amounts, "$84.20" otherwise. */
private fun formatMoney(amount: Double, withCents: Boolean? = null, truncate: Boolean = false): String {
    val value = abs(amount)
    val showCents = withCents ?: !(value >= 1000 && Math.round(value * 100) % 100 == 0L)
    val pattern = if (showCents) "#,##0.00" else "#,##0"
    val shown = if (truncate) kotlin.math.floor(Math.round(value * 100) / 100.0) else value
    val sign = if (amount < 0) "-" else ""
    return sign + "$" + DecimalFormat(pattern, usSymbols).format(shown)
}

private fun initialsOf(name: String): String =
    name.trim().split(Regex("\\s+"))
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "?" }

private fun relativeDate(timestampMillis: Long): String {
    val date = Calendar.getInstance().apply { timeInMillis = timestampMillis }
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

    fun Calendar.sameDayAs(other: Calendar) =
        get(Calendar.YEAR) == other.get(Calendar.YEAR) && get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)

    val time = Date(timestampMillis)
    return when {
        date.sameDayAs(today) -> "Today " + SimpleDateFormat("h:mm a", Locale.US).format(time)
        date.sameDayAs(yesterday) -> "Yesterday"
        date.get(Calendar.YEAR) == today.get(Calendar.YEAR) -> SimpleDateFormat("MMM d", Locale.US).format(time)
        else -> SimpleDateFormat("MMM d, yyyy", Locale.US).format(time)
    }
}

// Previews

@Preview(showBackground = true, heightDp = 1300)
@Composable
private fun DashboardScreenPreview() {
    val now = System.currentTimeMillis()
    val day = 24 * 60 * 60 * 1000L
    MobileFinanceTheme {
        DashboardScreen(
            state = DashboardUiState(
                userName = "Maya Chen",
                totalBalance = 7000.42,
                income = 4820.0,
                expenses = 2310.0,
                changeVsLastMonthPercent = 12.4,
                monthlySpending = listOf(1200.0, 1650.0, 1050.0, 1800.0, 1400.0, 2250.0, 1700.0, 2400.0, 1350.0, 1850.0, 1600.0, 1940.0),
                currentMonth = 7,
                selectedMonth = 7,
                monthlyBudget = 2500.0,
                recentTransactions = listOf(
                    DashboardTransaction(1, "Whole Foods Market", "Groceries", 84.20, DashboardTransactionKind.EXPENSE, now),
                    DashboardTransaction(2, "Salary — Linear", "Income", 3200.0, DashboardTransactionKind.INCOME, now),
                    DashboardTransaction(3, "Uber Trip", "Transport", 18.50, DashboardTransactionKind.EXPENSE, now - day),
                    DashboardTransaction(4, "Electric Bill", "Bills", 62.00, DashboardTransactionKind.EXPENSE, now - day)
                ),
                isLoading = false
            ),
            onMonthSelected = {},
            onAddTransaction = {},
            onSeeAllTransactions = {},
            onTransactionClick = {},
            onTabSelected = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 1100)
@Composable
private fun DashboardScreenEmptyPreview() {
    MobileFinanceTheme {
        DashboardScreen(
            state = DashboardUiState(userName = "Maya Chen", isLoading = false),
            onMonthSelected = {},
            onAddTransaction = {},
            onSeeAllTransactions = {},
            onTransactionClick = {},
            onTabSelected = {}
        )
    }
}
