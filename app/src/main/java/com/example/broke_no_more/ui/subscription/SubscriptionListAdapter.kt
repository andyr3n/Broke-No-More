package com.example.broke_no_more.ui.subscription

import android.content.Context
import android.icu.util.Calendar
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.broke_no_more.R
import com.example.broke_no_more.database.Expense
import com.example.broke_no_more.database.ExpenseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date

class SubscriptionListAdapter(private val context: Context, private var subscriptionList: MutableList<Expense>,
    private val expenseViewModel: ExpenseViewModel, private var calendar: Calendar):
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val subscription = subscriptionList[position]//Get subscription at specific position
        val subscriptionDate = subscription.date//Subscription Date

        //Calculate how many days left from until due date
        val daysLeft = calculateDayLeft(subscriptionDate.get(Calendar.YEAR),
            subscriptionDate.get(Calendar.MONTH), subscriptionDate.get(Calendar.DAY_OF_MONTH),
            subscription.isAnnually)

        //Display subscription's name
        holder.paymentName.text = subscription.name

//        if(daysLeft < 0) {
//            holder.paymentDue.text = "Past Due!"
//        }
//        else {
//            holder.paymentDue.text = "Due in $daysLeft days"
//        }

        //Display Due date
        val day = subscriptionDate.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
        val dueDate = LocalDate.of(year, month, day)
        val formattedDate = dueDate.format(dateFormatter)
        holder.paymentDue.text = "Due on: $formattedDate"

        //Display subscription's amount
        holder.paymentAmount.text = "$${subscription.amount}"
    }

    override fun getItemCount(): Int {
        return subscriptionList.size
    }

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val paymentName: TextView = itemView.findViewById(R.id.payment_name)
        val paymentDue: TextView = itemView.findViewById(R.id.payment_due)
        val paymentAmount: TextView = itemView.findViewById(R.id.payment_amount)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateDayLeft(selectedYear: Int, selectedMonth: Int, selectedDay: Int, isAnnually: Boolean): Long {
        println("Selected day is: $selectedDay/ ${selectedMonth + 1}/ $selectedYear")
        val dueDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
        val today = LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH))

        //Calculate how many days left from today until due date
        val daysLeft = Duration.between(dueDate.atStartOfDay(), today.atStartOfDay()).toDays()
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

    fun updateDate(newCalendar: Calendar){
        calendar = newCalendar
        notifyDataSetChanged()
    }
}