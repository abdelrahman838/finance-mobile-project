package com.example.mobilefinance.data.model

/**
 * The transaction shape the UI works with.
 *
 * Screens never touch the Room entity directly, they only see this class.
 * If the database table changes, only the mapper has to be updated.
 */
data class TransactionItem(
    val id: Long,
    val title: String,
    val amount: Double,          // always positive, use [type] for the sign
    val type: TransactionType,
    val category: String = DEFAULT_CATEGORY,
    val note: String = "",
    val dateMillis: Long? = null // null when the source has no date yet
) {
    val signedAmount: Double
        get() = if (type == TransactionType.INCOME) amount else -amount

    companion object {
        const val DEFAULT_CATEGORY = "Other"
    }
}

enum class TransactionType { INCOME, EXPENSE }
