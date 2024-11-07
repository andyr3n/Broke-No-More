package com.example.broke_no_more.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    val _spendingGoal = MutableLiveData<Double>()
    val spendingGoal : LiveData<Double> = _spendingGoal

    fun updateSpendingGoal(goal: Double){
        println("Inside updating: $goal")
        //Update only if new spending goal entered
        if(_spendingGoal.value != goal){
            _spendingGoal.value = goal
            println("New value: ${_spendingGoal.value}")
        }
        else
            println("Old value")
    }

//    private val _text = MutableLiveData<String>().apply {
//        value = "This is home Fragment"
//    }
//    val text: LiveData<String> = _text
}