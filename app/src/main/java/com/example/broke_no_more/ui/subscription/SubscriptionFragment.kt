package com.example.broke_no_more.ui.subscription

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
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
    private lateinit var totalAmountSubscription: TextView

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

        //Declare variables for database
        database = ExpenseDatabase.getInstance(requireActivity())
        databaseDao = database.expenseDatabaseDao
        repository = ExpenseRepository(databaseDao)
        viewModelFactory = ExpenseViewModelFactory(repository)
        expenseViewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(ExpenseViewModel::class.java)

        calendar = Calendar.getInstance()
        setUpSubscription()

        //Set up Month and Year Card View
        prevMonthBtn = binding.prevMonthButton
        nextMonthBtn = binding.nextMonthButton

        prevMonthBtn.setOnClickListener{
            calendar.add(Calendar.MONTH, -1)
            setUpSubscription()
        }

        nextMonthBtn.setOnClickListener{
            calendar.add(Calendar.MONTH, 1)
            setUpSubscription()
        }

        //Set up Recycler View with adapter to inflate custom rows
        subscriptionListAdapter = SubscriptionListAdapter(requireContext(), subscriptionList, expenseViewModel,
            calendar)
        recyclerView = binding.subscriptionRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = subscriptionListAdapter

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

    private fun setUpSubscription(){
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        binding.monthText.text = monthFormat.format(calendar.time)

        //Inflates only rows is Subscription (isSubscription == true)
        expenseViewModel.allSubscriptionsLiveData.observe(viewLifecycleOwner){ list ->
            //Filter the list: Match current month/ is Monthly subscription/ is Annually subscription
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentMonthList = list.filter { it.date.get(Calendar.MONTH) == currentMonth }
            val repeatMonthlyList = list.filter { it.isMonthly }
            val repeatAnnuallyList = list.filter { it.isAnnually  && it.date.get(Calendar.MONTH) == currentMonth}

            //Combine all list and filter to keep distinct subscription on screen only
            var allList = currentMonthList + repeatMonthlyList + repeatAnnuallyList
            allList = allList.distinctBy { it.id }

            subscriptionListAdapter.updateDate(calendar)
            subscriptionListAdapter.replace(allList)
            subscriptionListAdapter.notifyDataSetChanged()

            //Update total this month
            totalAmountSubscription.text = "$${allList.sumOf { it.amount }}"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        println("Selected date is $dayOfMonth/ $month/ $year")
    }
}