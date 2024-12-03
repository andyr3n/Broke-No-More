package com.example.broke_no_more.ui

import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.broke_no_more.R
import com.example.broke_no_more.database.ExpenseDatabase
import com.example.broke_no_more.database.ExpenseFragment
import com.example.broke_no_more.database.ExpenseRepository
import com.example.broke_no_more.database.ExpenseViewModel
import com.example.broke_no_more.database.ExpenseViewModelFactory
import com.example.broke_no_more.database.MyListAdapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExpensesDialogFragment : DialogFragment() {
    private lateinit var listView: ListView
    private lateinit var adapter: MyListAdapter
    private lateinit var dateTextView: TextView
    private lateinit var emptyTextView: TextView

    companion object {
        private const val ARG_DATE = "date"

        fun newInstance(date: Date): ExpensesDialogFragment {
            val fragment = ExpensesDialogFragment()
            val args = Bundle()
            args.putSerializable(ARG_DATE, date)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_expense_dialog, container, false)

        // Initialize views
        listView = view.findViewById(R.id.listViewEntries)
        dateTextView = view.findViewById(R.id.dateTextView)
        emptyTextView = view.findViewById(R.id.emptyTextView)

        val database = ExpenseDatabase.getInstance(requireActivity())
        val databaseDao = database.expenseDatabaseDao
        val repository = ExpenseRepository(databaseDao)
        val viewModelFactory = ExpenseViewModelFactory(repository)
        val expenseViewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(
            ExpenseViewModel::class.java)

        adapter = MyListAdapter(requireContext(), emptyList())
        listView.adapter = adapter

        // Set the date in the TextView
        getDate()?.let { selectedDate ->
            val formattedDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(selectedDate)
            dateTextView.text = formattedDate
        }

        expenseViewModel.allEntriesLiveData.observe(viewLifecycleOwner) {
            val selectedDate = getDate()
            val formattedSelectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate)

            val filteredExpenses = it.filter { expense ->
                val expenseDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(expense.date.time)

                val selectedCalendar = Calendar.getInstance().apply {
                    time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(formattedSelectedDate)
                }

                val isSameDay = expenseDate == formattedSelectedDate

                val isMonthlySubscription = expense.isMonthly &&
                        expense.date.get(Calendar.DAY_OF_MONTH) == selectedCalendar.get(Calendar.DAY_OF_MONTH)

                val isAnnualSubscription = expense.isAnnually &&
                        expense.date.get(Calendar.DAY_OF_MONTH) == selectedCalendar.get(Calendar.DAY_OF_MONTH) &&
                        expense.date.get(Calendar.MONTH) == selectedCalendar.get(Calendar.MONTH)

                isSameDay || isMonthlySubscription || isAnnualSubscription
            }
            if (filteredExpenses.isEmpty()) {
                emptyTextView.visibility = View.VISIBLE
            } else {
                emptyTextView.visibility = View.GONE
            }
            adapter.replace(filteredExpenses)
            adapter.notifyDataSetChanged()
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val expense = adapter.getItem(position)
            expense?.let {
                val bundle = Bundle().apply {
                    putLong("id", it.id)
                    putString("name", it.name)
                    putString("date", it.date.time.toString())
                    putDouble("amount", it.amount)
                    putString("comment", it.comment)
                    putString("category", it.category)
                }
                val expenseFragment = ExpenseFragment()
                expenseFragment.arguments = bundle

                dismiss()

                findNavController().navigate(R.id.action_home_to_expenseFragment, bundle)
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): android.app.Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    fun getDate(): Date? {
        return arguments?.getSerializable(ARG_DATE) as? Date
    }
}


