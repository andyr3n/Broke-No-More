package com.example.broke_no_more.ui.home

import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.broke_no_more.R
import com.example.broke_no_more.database.ExpenseDatabase
import com.example.broke_no_more.database.ExpenseDatabaseDao
import com.example.broke_no_more.database.ExpenseRepository
import com.example.broke_no_more.database.ExpenseViewModel
import com.example.broke_no_more.database.ExpenseViewModelFactory
import com.example.broke_no_more.databinding.FragmentHomeBinding
import com.example.broke_no_more.ui.CalendarAdapter
import com.example.broke_no_more.ui.ExpensesDialogFragment
import com.example.broke_no_more.ui.add_expense.AddExpenseActivity
import com.example.broke_no_more.ui.subscription.SubscriptionFragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment(){

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var addExpenseButton: Button
    private lateinit var addSubsriptionButton: Button

    //Initialize for Database
    private lateinit var database: ExpenseDatabase
    private lateinit var databaseDao: ExpenseDatabaseDao
    private lateinit var repository: ExpenseRepository
    private lateinit var viewModelFactory: ExpenseViewModelFactory
    private lateinit var expenseViewModel: ExpenseViewModel

    private lateinit var subscriptionContainer: LinearLayout
    private lateinit var textNoSubscription: TextView

    //calendar
    private val calendar = java.util.Calendar.getInstance()
    private lateinit var calendarAdapter: CalendarAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //Declare variables for database
        database = ExpenseDatabase.getInstance(requireActivity())
        databaseDao = database.expenseDatabaseDao
        repository = ExpenseRepository(databaseDao)
        viewModelFactory = ExpenseViewModelFactory(repository)
        this.expenseViewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(ExpenseViewModel::class.java)

        setupCalendar()
        setupObservers()

        binding.prevMonthButton.setOnClickListener {
            calendar.add(java.util.Calendar.MONTH, -1)
            updateCalendar()
        }

        binding.nextMonthButton.setOnClickListener {
            calendar.add(java.util.Calendar.MONTH, 1)
            updateCalendar()
        }


        addExpenseButton = binding.addExpenseBtn
        addExpenseButton.setOnClickListener(){
            //Swap Fragment to addExpense
            val intent = Intent(requireContext(), AddExpenseActivity::class.java)
            startActivity(intent)
        }

        //Swap to SubscriptionFragment
        addSubsriptionButton = binding.addSubscriptionBtn
        addSubsriptionButton.setOnClickListener(){
            val subscriptionFragment = SubscriptionFragment()
            val manager = requireActivity().supportFragmentManager
            val transaction = manager.beginTransaction()
            transaction.replace(R.id.fragment_home, subscriptionFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        subscriptionContainer = binding.subscriptionContainer
        textNoSubscription = binding.noPaymentDueHeader

        //Inflate first 3 rows for subscription
        this.expenseViewModel.allSubscriptionsLiveData.observe(viewLifecycleOwner){
            subscriptionContainer.removeAllViews()//Remove old view

            // Inflate first 3 subscription
            var count = 0
            for(entry in it){
                val daysLeft = calculateDayLeft(entry.date.get(Calendar.DAY_OF_MONTH))
                if(daysLeft >= 0 && count <= 3) {
                    addSubsriptionButton.text = "See More"//Change status if there are subscriptions saved
                    textNoSubscription.visibility = View.GONE//Remove text for no subscription
                    displayPaymentDue(entry.name, daysLeft, entry.amount)
                    count++
                }
            }
        }


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //Display subscription
    private fun displayPaymentDue(name: String, daysLeft: Int, amount: Double){
        val paymentView = layoutInflater.inflate(R.layout.subscription_list, null)
        val paymentName = paymentView.findViewById<TextView>(R.id.payment_name)
        val paymentDue = paymentView.findViewById<TextView>(R.id.payment_due)
        val paymentAmount = paymentView.findViewById<TextView>(R.id.payment_amount)

        paymentName.text = name
        paymentDue.text = "Due in $daysLeft days"
        paymentAmount.text = "$$amount"

        subscriptionContainer.addView(paymentView)//Add new payment dynamically
    }

    //Calculate how many days left until due date
    private fun calculateDayLeft(selectedDay: Int): Int{
        //Find current date
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_MONTH)

        //Calculate how many days left from today until due date
        val daysLeft = selectedDay - today
        return daysLeft
    }

    private fun setupCalendar() {
        binding.calendarRecyclerView.layoutManager = GridLayoutManager(requireContext(), 7)
        updateCalendar()
    }

    private fun updateCalendar() {
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        binding.monthText.text = monthFormat.format(calendar.time)

        val daysInMonth = getDaysInMonth(calendar)

        val expensesByDate = this.expenseViewModel.allEntriesLiveData.value?.groupBy {
            val date = it.date.time
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
        } ?: emptyMap()

        calendarAdapter = CalendarAdapter(daysInMonth, expensesByDate, calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR)) { date ->
            showExpensesDialog(date)
        }
        binding.calendarRecyclerView.adapter = calendarAdapter

    }

    private fun setupObservers() {
        this.expenseViewModel.allEntriesLiveData.observe(viewLifecycleOwner) {
            updateCalendar()
        }
    }

    private fun getDaysInMonth(calendar: java.util.Calendar): List<Date> {
        val days = mutableListOf<Date>()
        val tempCalendar = calendar.clone() as java.util.Calendar

        tempCalendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        val firstDayOfMonth = tempCalendar.get(java.util.Calendar.DAY_OF_WEEK) - 1

        tempCalendar.add(java.util.Calendar.DAY_OF_MONTH, -firstDayOfMonth)

        val daysInMonth = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
        var maxDays = 35
        if ((firstDayOfMonth == 5 && daysInMonth == 31) || (firstDayOfMonth == 6 && daysInMonth >= 30)) {
            maxDays = 42
        }
        for (i in 0 until maxDays) {
            days.add(tempCalendar.time)
            tempCalendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
        }
        return days
    }

    private fun showExpensesDialog(date: Date) {
        val dialog = ExpensesDialogFragment.newInstance(date)
        dialog.show(parentFragmentManager, "ExpensesDialogFragment")
    }

}