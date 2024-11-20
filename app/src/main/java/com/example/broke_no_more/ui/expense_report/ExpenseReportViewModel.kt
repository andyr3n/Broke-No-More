package com.example.broke_no_more.ui.expense_report

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ExpenseReportViewModel : ViewModel() {
    val rentCategoryName = MutableLiveData("Rent")
    val rentAmount = MutableLiveData("$600")

    val groceryCategoryName = MutableLiveData("Grocery")
    val groceryAmount = MutableLiveData("$800")
    val grocerySubcategory1 = MutableLiveData("• Walmart: $400")
    val grocerySubcategory2 = MutableLiveData("• Target: $400")

    val clothesCategoryName = MutableLiveData("Clothes")
    val clothesAmount = MutableLiveData("$200")

    val uncategorizedCategoryName = MutableLiveData("Uncategorized")
    val uncategorizedAmount = MutableLiveData("$100")

    val notice1 = MutableLiveData("IT Services: $50")
    val notice2 = MutableLiveData("Revenue Service: $20")
}
