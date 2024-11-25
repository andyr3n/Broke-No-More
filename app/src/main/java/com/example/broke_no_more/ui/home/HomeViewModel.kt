package com.example.broke_no_more.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    val spendingGoal = MutableLiveData<Double>()

}