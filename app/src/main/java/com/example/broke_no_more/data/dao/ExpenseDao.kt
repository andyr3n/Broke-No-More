package com.example.broke_no_more.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.broke_no_more.database.Expense

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expense_table")
    fun getAllExpenses(): LiveData<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("DELETE FROM expense_table")
    suspend fun deleteAllExpenses()

    @Query("SELECT * FROM expense_table WHERE isSubscription = 1")
    fun getSubscriptionExpenses(): LiveData<List<Expense>>
}