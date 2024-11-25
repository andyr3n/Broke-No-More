package com.example.broke_no_more.database

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ExpenseRepository(private val expenseDatabaseDao: ExpenseDatabaseDao) {

    val allEntries: Flow<List<Expense>> = expenseDatabaseDao.getAllEntries()
    val allSubscriptions: Flow<List<Expense>> = expenseDatabaseDao.getSubscriptionExpenses()

    fun insert(expense: Expense){
        CoroutineScope(IO).launch{
            expenseDatabaseDao.insertExpense(expense)
        }
    }

    fun delete(id: Long){
        CoroutineScope(IO).launch {
            expenseDatabaseDao.deleteExpense(id)
        }
    }
}