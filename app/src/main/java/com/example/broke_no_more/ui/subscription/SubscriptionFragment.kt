package com.example.broke_no_more.ui.subscription

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.broke_no_more.R
import com.example.broke_no_more.databinding.FragmentSubscriptionBinding
import com.example.broke_no_more.database.Expense
import com.example.broke_no_more.database.ExpenseDatabase
import com.example.broke_no_more.database.ExpenseDatabaseDao
import com.example.broke_no_more.database.ExpenseRepository
import com.example.broke_no_more.database.ExpenseViewModel
import com.example.broke_no_more.database.ExpenseViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class SubscriptionFragment: Fragment(), DatePickerDialog.OnDateSetListener {
    private var _binding: FragmentSubscriptionBinding? = null
    private val binding get() = _binding!!

    private lateinit var addPaymentBtn: FloatingActionButton
    private val paymentDue = mutableListOf<PaymentDue>()
    private lateinit var totalAmountSubscription: TextView
    private var num = 0

    private lateinit var recyclerView: RecyclerView
    private var subscriptionList = ArrayList<Expense>()
    private lateinit var subscriptionListAdapter: SubscriptionListAdapter

    private lateinit var prevMonthBtn: ImageButton
    private lateinit var nextMonthBtn: ImageButton
    private lateinit var calendar: Calendar

    //Initialize for Database
    private lateinit var database: ExpenseDatabase
    private lateinit var databaseDao: ExpenseDatabaseDao
    private lateinit var repository: ExpenseRepository
    private lateinit var viewModelFactory: ExpenseViewModelFactory
    private lateinit var expenseViewModel: ExpenseViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubscriptionBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //Change title in Toolbar
        val activity = activity as AppCompatActivity
        activity.supportActionBar?.title = "Subscription Management"

        //Initialize variables in view to use
        totalAmountSubscription = binding.totalAmountSubsciption

        //Add new subscription information
        addPaymentBtn = binding.addSubsciptionButton
        addPaymentBtn.setOnClickListener{
            addPaymentDue()
        }

        //Declare variables for database
        database = ExpenseDatabase.getInstance(requireActivity())
        databaseDao = database.expenseDatabaseDao
        repository = ExpenseRepository(databaseDao)
        viewModelFactory = ExpenseViewModelFactory(repository)
        expenseViewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(ExpenseViewModel::class.java)

        //Set up Recycler View with adapter to inflate custom rows
        subscriptionListAdapter = SubscriptionListAdapter(requireContext(), subscriptionList, expenseViewModel, 1)
        recyclerView = binding.subscriptionRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = subscriptionListAdapter

        calendar = Calendar.getInstance()
        setUpSubscription()

        //Set up Month and Year Card View
        prevMonthBtn = binding.prevMonthButton
        nextMonthBtn = binding.nextMonthButton

        prevMonthBtn.setOnClickListener{
            calendar.add(java.util.Calendar.MONTH, -1)
            setUpSubscription()
        }

        nextMonthBtn.setOnClickListener{
            calendar.add(java.util.Calendar.MONTH, 1)
            setUpSubscription()
        }

        //Swipe Left to delete the subscription
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                //Return false because we don't want to move object around
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                subscriptionListAdapter.removeAt(position)
            }

        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)


        return root
    }


    private fun addPaymentDue(){
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_payment_due, null)
        val paymentName = dialogView.findViewById<EditText>(R.id.edit_payment_name)
        val paymentDate = dialogView.findViewById<Button>(R.id.button_chooseDueDate)
        val chosenDate = dialogView.findViewById<TextView>(R.id.text_choosen_date)
        val paymentAmount = dialogView.findViewById<EditText>(R.id.edit_payment_amount)

        var date = Calendar.getInstance()//Choose current time as default
        var daysLeft = 0

        paymentDate.setOnClickListener(){
            //Show DatePickerDialog to choose Date
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(requireContext(),{ _, selectedYear, selectedMonth, selectedDay ->
                //Set date to chosen date
                calendar.set(selectedYear, selectedMonth, selectedDay)
                date = calendar

                daysLeft = calculateDayLeft(selectedDay)

                //Set format to day month (in text), year
                chosenDate.setText(SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault()).format(date.time))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            datePickerDialog.show()
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Add New Subscription")//Set title
            .setView(dialogView)
            .setPositiveButton("Save"){_, _ ->
                //Define name, amount and amount from user
                var name = paymentName.text.toString()
                val amount = paymentAmount.text.toString().toDoubleOrNull()?: 0.0

                //Create new object paymentDue with info from input
                val newPaymentDue = PaymentDue(name, date, amount)
                paymentDue.add(newPaymentDue)

                //Add default name if user does not enter anything
                if(name.isEmpty()){
                    num++
                    name = "Subscription $num"
                }

                //Insert new added subscription to database
                CoroutineScope(IO).launch {
                    //Create a new object Expense
                    val subscriptionExpense = Expense()
                    subscriptionExpense.subscriptionName = name
                    subscriptionExpense.date = date
                    subscriptionExpense.amount = amount
                    subscriptionExpense.isSubscription = true

                    //Add to database
                    expenseViewModel.insert(subscriptionExpense)
                }
                Toast.makeText(requireContext(), "Added successfully", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    //Display new added subscription to UI
    private fun displayPaymentDue(name: String, daysLeft: Int, amount: Double){
        val paymentView = layoutInflater.inflate(R.layout.subscription_list, null)
        val paymentName = paymentView.findViewById<TextView>(R.id.payment_name)
        val paymentDue = paymentView.findViewById<TextView>(R.id.payment_due)
        val paymentAmount = paymentView.findViewById<TextView>(R.id.payment_amount)

        paymentName.text = name
        paymentDue.text = "Due in $daysLeft days"
        paymentAmount.text = "$$amount"
    }

    private fun calculateDayLeft(selectedDay: Int): Int{
        //Find current date
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_MONTH)

        //Calculate how many days left from today until due date
        val daysLeft = selectedDay - today
        return daysLeft
    }

    private fun setUpSubscription(){
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        binding.monthText.text = monthFormat.format(calendar.time)

        //Inflates only rows is Subscription (isSubscription == true)
        expenseViewModel.allSubscriptionsLiveData.observe(viewLifecycleOwner){ list ->
            //Filter the list to match chosen Month
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentMonthList = list.filter { it.date.get(Calendar.MONTH) == currentMonth }
            subscriptionListAdapter.replace(currentMonthList)
            subscriptionListAdapter.notifyDataSetChanged()

            //Update total this month
            totalAmountSubscription.text = "$${currentMonthList.sumOf { it.amount }}"
        }
    }

    data class PaymentDue(val name: String, val date: Calendar, val amount: Double?)
    data class MonthItem(val month: String, var isSelected: Boolean = false)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        println("Selected date is $dayOfMonth/ $month/ $year")
    }
}