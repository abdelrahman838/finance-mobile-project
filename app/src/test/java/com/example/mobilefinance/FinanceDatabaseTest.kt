package com.example.mobilefinance

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.mobilefinance.data.local.AppDatabase
import com.example.mobilefinance.data.local.dao.TransactionDao
import com.example.mobilefinance.data.local.dao.UserDao
import com.example.mobilefinance.data.local.entity.Transaction
import com.example.mobilefinance.data.local.entity.TransactionCategory
import com.example.mobilefinance.data.local.entity.TransactionType
import com.example.mobilefinance.data.local.entity.User
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FinanceDatabaseTest {

    private lateinit var db: AppDatabase
    private lateinit var userDao: UserDao
    private lateinit var transactionDao: TransactionDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // In-memory database resets cleanly after every test
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        userDao = db.userDao()
        transactionDao = db.transactionDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    // =========================================================================
    // USER DAO TESTS
    // =========================================================================

    @Test
    fun registerAndLoginUser_returnsMatchingUser() = runTest {
        val user = User(
            name = "Maya Chen",
            username = "maya.chen@mail.com",
            hashedPassword = "hashed_pw_123"
        )
        val insertedId = userDao.registerUser(user)

        assertTrue(userDao.isUsernameTaken("maya.chen@mail.com"))
        assertFalse(userDao.isUsernameTaken("unknown@mail.com"))

        val loggedInUser = userDao.login("maya.chen@mail.com", "hashed_pw_123")
        assertNotNull(loggedInUser)
        assertEquals(insertedId, loggedInUser?.userId)
        assertEquals("Maya Chen", loggedInUser?.name)

        // Wrong password should fail
        val failedLogin = userDao.login("maya.chen@mail.com", "wrong_hash")
        assertNull(failedLogin)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun registerDuplicateUsername_throwsConstraintException() = runTest {
        val user1 = User(name = "Maya", username = "maya@mail.com", hashedPassword = "h1")
        val user2 = User(name = "Maya Duplicate", username = "maya@mail.com", hashedPassword = "h2")

        userDao.registerUser(user1)
        userDao.registerUser(user2) // Must throw SQLiteConstraintException due to unique index
    }

    // =========================================================================
    // TRANSACTION DAO TESTS
    // =========================================================================

    @Test
    fun insertTransaction_defaultsToExpenseAndFood_andCalculatesBalancesCorrectly() = runTest {
        val userId = userDao.registerUser(
            User(name = "Maya Chen", username = "maya@mail.com", hashedPassword = "h1")
        )

        // 1. Insert Income (+3200.00)
        transactionDao.insertTransaction(
            Transaction(
                userId = userId,
                type = TransactionType.INCOME,
                amount = 3200.00,
                category = TransactionCategory.MORE,
                title = "Salary — Linear",
                description = "October paycheck",
                date = "2026-10-22",
                time = "09:00 AM"
            )
        )

        // 2. Insert Expense without specifying `type` or `category` (verifies defaults: EXPENSE & FOOD)
        val defaultTxId = transactionDao.insertTransaction(
            Transaction(
                userId = userId,
                amount = 84.20,
                title = "Whole Foods Market",
                description = "Weekly groceries + oat milk",
                date = "2026-10-23",
                time = "02:40 PM"
            )
        )

        val savedDefaultTx = transactionDao.getTransactionById(defaultTxId)
        assertEquals(TransactionType.EXPENSE, savedDefaultTx?.type)
        assertEquals(TransactionCategory.FOOD, savedDefaultTx?.category)

        // 3. Insert second Expense (-18.50)
        transactionDao.insertTransaction(
            Transaction(
                userId = userId,
                type = TransactionType.EXPENSE,
                amount = 18.50,
                category = TransactionCategory.TRANSPORT,
                title = "Uber Trip Downtown",
                date = "2026-10-23",
                time = "11:15 AM"
            )
        )

        val totalIncome = transactionDao.getTotalByType(userId, TransactionType.INCOME).first()
        val totalExpense = transactionDao.getTotalByType(userId, TransactionType.EXPENSE).first()
        val netBalance = transactionDao.getTotalBalance(userId).first()

        assertEquals(3200.00, totalIncome, 0.001)
        assertEquals(102.70, totalExpense, 0.001) // 84.20 + 18.50
        assertEquals(3097.30, netBalance, 0.001)   // 3200.00 - 102.70
    }

    @Test
    fun updateAndDeleteTransaction_updatesDatabaseState() = runTest {
        val userId = userDao.registerUser(
            User(name = "Maya", username = "maya@mail.com", hashedPassword = "h1")
        )

        val txId = transactionDao.insertTransaction(
            Transaction(
                userId = userId,
                type = TransactionType.EXPENSE,
                amount = 84.20,
                category = TransactionCategory.SHOPPING,
                title = "Whole Foods Market",
                description = "Old note",
                date = "2026-10-23",
                time = "02:40 PM"
            )
        )

        // Update category and amount (Edit Transaction screen)
        val savedTx = transactionDao.getTransactionById(txId)!!
        val updatedTx = savedTx.copy(
            amount = 90.00,
            category = TransactionCategory.FOOD,
            description = "Updated note"
        )
        transactionDao.updateTransaction(updatedTx)

        val fetchedAfterUpdate = transactionDao.getTransactionById(txId)
        assertEquals(90.00, fetchedAfterUpdate?.amount ?: 0.0, 0.001)
        assertEquals(TransactionCategory.FOOD, fetchedAfterUpdate?.category)

        // Delete Transaction
        transactionDao.deleteTransaction(fetchedAfterUpdate!!)
        assertNull(transactionDao.getTransactionById(txId))
    }

    @Test
    fun searchAndCategoryFilter_returnsExpectedTransactions() = runTest {
        val userId = userDao.registerUser(
            User(name = "Maya", username = "maya@mail.com", hashedPassword = "h1")
        )

        transactionDao.insertTransaction(
            Transaction(
                userId = userId,
                type = TransactionType.EXPENSE,
                amount = 84.20,
                category = TransactionCategory.FOOD,
                title = "Whole Foods Market",
                description = "Weekly groceries + oat milk",
                date = "2026-10-23",
                time = "02:40 PM"
            )
        )
        transactionDao.insertTransaction(
            Transaction(
                userId = userId,
                type = TransactionType.EXPENSE,
                amount = 62.00,
                category = TransactionCategory.BILLS,
                title = "Electric Bill",
                description = "Monthly utility",
                date = "2026-10-22",
                time = "10:00 AM"
            )
        )

        // Filter by category chip (TransactionCategory.FOOD)
        val foodList = transactionDao.getTransactionsByCategory(userId, TransactionCategory.FOOD).first()
        assertEquals(1, foodList.size)
        assertEquals("Whole Foods Market", foodList[0].title)

        // Search by description keyword ("oat milk")
        val searchResults = transactionDao.searchTransactions(userId, "oat milk").first()
        assertEquals(1, searchResults.size)
        assertEquals("Whole Foods Market", searchResults[0].title)
    }

    @Test
    fun getMonthlyExpenses_groupsByYearMonthForChart() = runTest {
        val userId = userDao.registerUser(
            User(name = "Maya", username = "maya@mail.com", hashedPassword = "h1")
        )

        // August expenses
        transactionDao.insertTransaction(
            Transaction(userId = userId, type = TransactionType.EXPENSE, amount = 1000.0, category = TransactionCategory.BILLS, title = "Rent", date = "2026-08-01", time = "09:00 AM")
        )
        transactionDao.insertTransaction(
            Transaction(userId = userId, type = TransactionType.EXPENSE, amount = 940.0, category = TransactionCategory.SHOPPING, title = "Laptop", date = "2026-08-15", time = "03:00 PM")
        )
        // August income (should NOT be counted in monthly expenses)
        transactionDao.insertTransaction(
            Transaction(userId = userId, type = TransactionType.INCOME, amount = 5000.0, category = TransactionCategory.MORE, title = "Bonus", date = "2026-08-20", time = "12:00 PM")
        )
        // October expense
        transactionDao.insertTransaction(
            Transaction(userId = userId, type = TransactionType.EXPENSE, amount = 250.0, category = TransactionCategory.FOOD, title = "Dinner", date = "2026-10-10", time = "08:00 PM")
        )

        val monthlyExpenses = transactionDao.getMonthlyExpenses(userId, "2026%").first()

        assertEquals(2, monthlyExpenses.size)
        assertEquals("2026-08", monthlyExpenses[0].month)
        assertEquals(1940.0, monthlyExpenses[0].totalSpent, 0.001)
        assertEquals("2026-10", monthlyExpenses[1].month)
        assertEquals(250.0, monthlyExpenses[1].totalSpent, 0.001)
    }
}