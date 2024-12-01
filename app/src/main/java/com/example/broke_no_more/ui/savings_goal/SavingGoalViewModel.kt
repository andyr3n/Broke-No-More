package com.example.broke_no_more.ui.savings_goal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SavingGoalViewModel: ViewModel() {
    private val _housingGoal = MutableLiveData<Double>()
    val housingGoal: LiveData<Double>get() = _housingGoal

    private val _clothesGoal = MutableLiveData<Double>()
    val clothesGoal: LiveData<Double>get()= _clothesGoal

    private val _groceryGoal = MutableLiveData<Double>()
    val groceryGoal: LiveData<Double>get() = _groceryGoal

    private val _entertainmentGoal = MutableLiveData<Double>()
    val entertainmentGoal: LiveData<Double>get()= _entertainmentGoal

    private val _uncategorizedGoal = MutableLiveData<Double>()
    val uncategorizedGoal: LiveData<Double>get() = _uncategorizedGoal

    fun updateGroceryGoal(newGoal: Double){
        _groceryGoal.value = newGoal
    }

    fun updateHousingGoal(newGoal: Double){
        _housingGoal.value = newGoal
    }

    fun updateClothesGoal(newGoal: Double){
        _clothesGoal.value = newGoal
    }

    fun updateEntertainmentGoal(newGoal: Double){
        _entertainmentGoal.value = newGoal
    }

    fun updateUncategorizedGoal(newGoal: Double){
        _uncategorizedGoal.value = newGoal
    }
}