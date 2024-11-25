package com.example.broke_no_more.ui.home

import androidx.lifecycle.*
import kotlinx.coroutines.launch

class ExpenseViewModel(private val repository: ExpenseRepository) : ViewModel() {
    val allEntriesLiveData: LiveData<List<Expense>> = repository.allEntries.asLiveData()
    val allSubscriptionsLiveData: LiveData<List<Expense>> = repository.allSubscriptions.asLiveData()

    fun insert(expense: Expense) {
        viewModelScope.launch {
            repository.insert(expense)
        }
    }

    fun deleteExpenseById(id: Long) {
        viewModelScope.launch {
            repository.delete(id)
        }
    }
}

class ExpenseViewModelFactory(private val repository: ExpenseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            return ExpenseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
