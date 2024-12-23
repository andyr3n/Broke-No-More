package com.example.broke_no_more.ui

import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.broke_no_more.R
import com.example.broke_no_more.database.Expense
import java.text.SimpleDateFormat
import java.util.*

class CalendarAdapter(
    private val days: List<Date>,
    private val allExpenses: List<Expense>?,
    private val currentMonth: Int,
    private val currentYear: Int,
    private val onDateClick: (Date) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val displayMetrics = parent.context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.calendar_day_item, parent, false)

        val itemWidth = screenWidth / 9

        val layoutParams = view.layoutParams
        layoutParams.width = itemWidth
        layoutParams.height = itemWidth

        val margin = 2
        val density = parent.context.resources.displayMetrics.density
        val marginInPixels = (margin * density).toInt()

        (layoutParams as ViewGroup.MarginLayoutParams).setMargins(marginInPixels, marginInPixels, marginInPixels, marginInPixels)

        view.layoutParams = layoutParams
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val date = days[position]

        val dateCalendar = Calendar.getInstance()
        dateCalendar.time = date
        val isSameMonth = dateCalendar.get(Calendar.MONTH) == currentMonth &&
                dateCalendar.get(Calendar.YEAR) == currentYear

        if (isSameMonth) {
            holder.itemView.visibility = View.VISIBLE
            val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
            if (allExpenses != null) {
                val filteredExpenses = allExpenses.filter { expense ->
                    val expenseDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(expense.date.time)
                    val selectedCalendar = Calendar.getInstance().apply {
                        time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(formattedDate)
                    }
                    val isSameDay = expenseDate == formattedDate
                    val isMonthlySubscription = expense.isMonthly &&
                            expense.date.get(Calendar.DAY_OF_MONTH) == selectedCalendar.get(Calendar.DAY_OF_MONTH)
                    val isAnnualSubscription = expense.isAnnually &&
                            expense.date.get(Calendar.DAY_OF_MONTH) == selectedCalendar.get(Calendar.DAY_OF_MONTH) &&
                            expense.date.get(Calendar.MONTH) == selectedCalendar.get(Calendar.MONTH)
                    isSameDay || isMonthlySubscription || isAnnualSubscription
                }
                holder.bind(date, filteredExpenses)
            }
            holder.itemView.setOnClickListener { onDateClick(date) }
        } else {
            holder.itemView.visibility = View.INVISIBLE
        }
    }

    override fun getItemCount(): Int = days.size

    class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateTextView: TextView = itemView.findViewById(R.id.dayText)
        private val expenseIndicator: View = itemView.findViewById(R.id.expenseIndicator)

        fun bind(date: Date, expenses: List<Expense>) {
            val dayFormat = SimpleDateFormat("d", Locale.getDefault())
            dateTextView.text = dayFormat.format(date)

            if (expenses.isNotEmpty()) {
                expenseIndicator.visibility = View.VISIBLE
            } else {
                expenseIndicator.visibility = View.INVISIBLE
            }
        }
    }
}
