package com.example.broke_no_more.ui.subscription

import android.content.Context
import android.icu.util.Calendar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.broke_no_more.R
import com.example.broke_no_more.database.Expense
import com.example.broke_no_more.database.ExpenseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class SubscriptionListAdapter(private val context: Context, private var subscriptionList: MutableList<Expense>,
    private val expenseViewModel: ExpenseViewModel):
    RecyclerView.Adapter<SubscriptionListAdapter.MyViewHolder>(){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        //Inflate custom layout
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.subscription_list, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val subscription = subscriptionList[position]//Get subscription at specific position

        //Calculate how many days left from until due date
        val daysLeft = calculateDayLeft(subscription.date.get(Calendar.DAY_OF_MONTH))

        //Set textView to saved information
        holder.paymentName.text = subscription.subscriptionName
        holder.paymentDue.text = "Due in $daysLeft days"
        holder.paymentAmount.setText("$${subscription.amount}")
    }

    override fun getItemCount(): Int {
        return subscriptionList.size
    }

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val paymentName = itemView.findViewById<TextView>(R.id.payment_name)
        val paymentDue = itemView.findViewById<TextView>(R.id.payment_due)
        val paymentAmount = itemView.findViewById<TextView>(R.id.payment_amount)
    }

    private fun calculateDayLeft(selectedDay: Int): Int{
        //Find current date
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_MONTH)

        //Calculate how many days left from today until due date
        val daysLeft = selectedDay - today
        return daysLeft
    }

    //Replace with a new subscription list if data change
    fun replace(newSubscriptionList: List<Expense>){
        subscriptionList = newSubscriptionList.toMutableList()
    }

    fun removeAt(position: Int){
        val subscriptionId = subscriptionList[position].id
        CoroutineScope(IO).launch {
            expenseViewModel.deleteExpenseById(subscriptionId)
        }
        subscriptionList.removeAt(position)
        notifyItemRemoved(position)
    }
}