package com.example.broke_no_more.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    val _spendingGoal = MutableLiveData<Double>()
    val spendingGoal : LiveData<Double> = _spendingGoal

    fun updateSpendingGoal(goal: Double){
        //Update only if new spending goal entered
        if(_spendingGoal.value != goal) {
            _spendingGoal.value = goal
        }
    }
}