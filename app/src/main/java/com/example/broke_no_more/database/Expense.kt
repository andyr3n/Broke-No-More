package com.example.broke_no_more.ui.home

import android.icu.util.Calendar
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense_table")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "expense_column")
    var date: Calendar = Calendar.getInstance(),
    var amount: Double = 0.0,
    val comment: String? = null,
    var isSubscription: Boolean = false,
    var subscriptionName: String = ""
)