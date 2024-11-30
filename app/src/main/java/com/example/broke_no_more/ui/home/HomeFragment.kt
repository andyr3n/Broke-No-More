package com.example.broke_no_more.ui.home

import android.content.Context
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.broke_no_more.R
import com.example.broke_no_more.database.ExpenseDatabase
import com.example.broke_no_more.database.ExpenseDatabaseDao
import com.example.broke_no_more.database.ExpenseRepository
import com.example.broke_no_more.database.ExpenseViewModel
import com.example.broke_no_more.database.ExpenseViewModelFactory
import com.example.broke_no_more.databinding.FragmentHomeBinding
import com.example.broke_no_more.ui.CalendarFragment
import com.example.broke_no_more.ui.SavingsGoal.SavingsGoalFragment
import com.example.broke_no_more.ui.add_expense.AddExpenseFragment
import com.example.broke_no_more.ui.subscription.SubscriptionFragment

class HomeFragment : Fragment(){

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var editSpending: TextView
    private lateinit var spendingGoalAmount: TextView
    private lateinit var spendingGoalProcess: ProgressBar
    private lateinit var haveSpentAmount: TextView
    private lateinit var moreSaving: TextView
    private lateinit var addExpenseButton: Button
    private lateinit var addSubsriptionButton: Button
    private lateinit var calendarButton: Button

    //Initialize for Database
    private lateinit var database: ExpenseDatabase
    private lateinit var databaseDao: ExpenseDatabaseDao
    private lateinit var repository: ExpenseRepository
    private lateinit var viewModelFactory: ExpenseViewModelFactory
    private lateinit var expenseViewModel: ExpenseViewModel

    private lateinit var subscriptionContainer: LinearLayout
    private lateinit var textNoSubscription: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val homeViewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]
        homeViewModel.spendingGoal.observe(viewLifecycleOwner) { goal ->
            updateSpendingGoal()
        }
        Log.i("HomeFragment", "onCreateView called")

        //Edit Spending Goal
        editSpending = binding.editSpendingGoal
        editSpending.setOnClickListener(){
           //Open Dialog Fragment to Edit
            val spendingDialog = MyDialog()
            val bundle = Bundle()
            bundle.putInt(MyDialog.DIALOG_KEY, MyDialog.SPENDING_GOAL)
            spendingDialog.arguments = bundle
            spendingDialog.show(requireActivity().supportFragmentManager, "Edit Spending Dialog")
        }

        updateSpendingGoal()

        moreSaving = binding.savingMoreDetails
        moreSaving.setOnClickListener(){

            //Swap Fragment to SavingGoal
            val savingGoalFragment = SavingsGoalFragment()
            val manager = requireActivity().supportFragmentManager
            val transaction = manager.beginTransaction()
            transaction.replace(R.id.fragment_home, savingGoalFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        addExpenseButton = binding.addExpenseBtn
        addExpenseButton.setOnClickListener(){
            //Swap Fragment to addExpense
            val addExpenseFragment = AddExpenseFragment()
            val manager = requireActivity().supportFragmentManager
            val transaction = manager.beginTransaction()
            transaction.replace(R.id.fragment_home, addExpenseFragment)
            transaction.addToBackStack(null)
            transaction.commit()
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

        calendarButton = binding.calendar
        calendarButton.setOnClickListener(){
            val calendarFragment = CalendarFragment()
            val manager = requireActivity().supportFragmentManager
            val transaction = manager.beginTransaction()
            transaction.replace(R.id.fragment_home, calendarFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        //Declare variables for database
        database = ExpenseDatabase.getInstance(requireActivity())
        databaseDao = database.expenseDatabaseDao
        repository = ExpenseRepository(databaseDao)
        viewModelFactory = ExpenseViewModelFactory(repository)
        expenseViewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(ExpenseViewModel::class.java)


        subscriptionContainer = binding.subscriptionContainer
        textNoSubscription = binding.noPaymentDueHeader

        //Inflate first 3 rows for subscription
        expenseViewModel.allSubscriptionsLiveData.observe(viewLifecycleOwner){
            subscriptionContainer.removeAllViews()//Remove old view

            // Inflate first 3 subscription
            var count = 0
            for(entry in it){
                val daysLeft = calculateDayLeft(entry.date.get(Calendar.DAY_OF_MONTH))
                if(daysLeft >= 0 && count <= 3) {
                    addSubsriptionButton.text = "See More"//Change status if there are subscriptions saved
                    textNoSubscription.visibility = View.GONE//Remove text for no subscription
                    displayPaymentDue(entry.subscriptionName, daysLeft, entry.amount)
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

    private fun updateSpendingGoal() {
        val spentAmount = getSpentAmount()
        val spendGoal = getSpendingGoal()
        spendingGoalAmount = binding.spendingGoalAmount
        spendingGoalAmount.text = "$" + spendGoal
        spendingGoalProcess = binding.spendingProgress
        val spendPercentage = (( spentAmount/ spendGoal) * 100).toInt()
        spendingGoalProcess.progress = spendPercentage
        haveSpentAmount = binding.haveSpentText
        haveSpentAmount.text = "You have spent $$spentAmount/ $$spendGoal this month"
    }

    //Get saved spending goal
    private fun getSpendingGoal(): Double {
        val sharedPref = requireContext().getSharedPreferences("SPENDING GOAL", Context.MODE_PRIVATE)
        val goal = sharedPref.getString("spendgoal", "")
        var goalDouble = 0.0
        if (goal != null) {
            if(goal.isNotEmpty())//Only if user entered a value and saved
                goalDouble = goal.toDouble()//Convert from string to double
        }
        return goalDouble
    }

    //Get saved spent amount
    private fun getSpentAmount(): Double{
        val sharedPref = requireContext().getSharedPreferences("SPENDING GOAL", Context.MODE_PRIVATE)
        val spent = sharedPref.getString("spent amount", "")
        var spentDouble = 0.0
        if (spent != null) {
            if(spent.isNotEmpty())//Only if user entered a value and saved
                spentDouble = spent.toDouble()//Convert from string to double
        }
        return spentDouble
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

}