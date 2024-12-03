package com.example.broke_no_more.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// Data class to represent category expense breakdown
data class CategoryExpense(
    val category: String,
    val total: Double
)

@Dao
interface ExpenseDatabaseDao {
    @Insert
    suspend fun insertExpense(expense: Expense)

    @Query("SELECT * FROM expense_table")
    fun getAllEntries(): Flow<List<Expense>>

    @Query("SELECT * FROM expense_table")
    suspend fun getAllExpenses(): List<Expense>

    @Query("DELETE FROM expense_table WHERE id = :key")
    suspend fun deleteExpense(key: Long)

    @Query("SELECT * FROM expense_table WHERE isSubscription = 1")
    fun getSubscriptionExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expense_table WHERE isSubscription = 1")
    suspend fun getAllSubscriptions(): List<Expense>

    // Query to calculate total expenses by category
    @Query("SELECT category, SUM(amount) as total FROM expense_table GROUP BY category")
    suspend fun getCategoryExpenseBreakdown(): List<CategoryExpense>
}

