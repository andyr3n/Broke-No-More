package com.example.broke_no_more.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDatabaseDao {
    @Insert
    suspend fun insertExpense(expense: Expense)
    
    @Query("SELECT * FROM expense_table")
    fun getAllEntries(): Flow<List<Expense>>

    @Query("DELETE FROM expense_table WHERE id = :key")
    suspend fun deleteExpense(key: Long)

    @Query("SELECT * FROM expense_table WHERE isSubscription = 1")
    fun getSubscriptionExpenses(): Flow<List<Expense>>
}