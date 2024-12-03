package com.example.broke_no_more.database

import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.broke_no_more.R

class MyListAdapter(private val context: Context, private var expenseList: List<Expense>) : BaseAdapter(){

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
        val imageView = view.findViewById(R.id.expenseIcon) as ImageView

        val expense = expenseList[position]

        val expenseInfo = expense.name
        val expenseDetails = "$" + expense.amount.toString()

        textViewInfo.text = expenseInfo
        textViewExpense.text = expenseDetails

        when (expense.category) {
            "Housing" -> imageView.setImageResource(R.drawable.ic_housing)
            "Grocery" -> imageView.setImageResource(R.drawable.ic_grocery)
            "Clothes" -> imageView.setImageResource(R.drawable.ic_clothes)
            "Entertainment" -> imageView.setImageResource(R.drawable.ic_entertainment)
            "Subscription" -> imageView.setImageResource(R.drawable.ic_subscription)
            "Miscellaneous" -> imageView.setImageResource(R.drawable.ic_miscellaneous)
            else -> imageView.setImageResource(R.drawable.ic_miscellaneous)
        }


        return view
    }

    fun replace(newExpenseList: List<Expense>){
        expenseList = newExpenseList
    }
}