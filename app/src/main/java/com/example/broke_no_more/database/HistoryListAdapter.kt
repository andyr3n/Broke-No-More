package com.example.broke_no_more.database

import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.broke_no_more.R

class HistoryListAdapter(private val context: Context, private var expenseList: List<Expense>) : BaseAdapter(){

    override fun getItem(position: Int): Expense {
        return expenseList.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return expenseList.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = View.inflate(context, R.layout.layout_adapter,null)
        val textViewInfo = view.findViewById(R.id.expenseInfo) as TextView
        val textViewExpense = view.findViewById(R.id.expenseDetails) as TextView

        val expense = expenseList[position]

        val expenseInfo = expense.date.time.toString().substring(0, 10) + " - " + expense.category
        val expenseDetails = "$" + expense.amount.toString()

        textViewInfo.text = expenseInfo
        textViewExpense.text = expenseDetails

        return view
    }

    fun replace(newExpenseList: List<Expense>){
        expenseList = newExpenseList
    }
}