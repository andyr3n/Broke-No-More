package com.example.broke_no_more.ui.expense_report

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.broke_no_more.R
import com.example.broke_no_more.database.Expense
import com.example.broke_no_more.database.ExpenseDatabase
import com.example.broke_no_more.database.ExpenseRepository
import com.example.broke_no_more.database.ExpenseViewModel
import com.example.broke_no_more.database.ExpenseViewModelFactory
import com.example.broke_no_more.database.HistoryFragment
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

class ExpenseReportFragment : Fragment() {

    private lateinit var expenseViewModel: ExpenseViewModel
    private lateinit var categoryRecyclerView: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.expense_report, container, false)

        // Initialize ViewModel
        val application = requireNotNull(this.activity).application
        val databaseDao = ExpenseDatabase.getInstance(application).expenseDatabaseDao
        val repository = ExpenseRepository(databaseDao)
        val viewModelFactory = ExpenseViewModelFactory(repository)
        expenseViewModel = ViewModelProvider(this, viewModelFactory).get(ExpenseViewModel::class.java)

        // Set up RecyclerView for category totals
        categoryRecyclerView = view.findViewById(R.id.categoryRecyclerView)
        categoryRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        categoryAdapter = CategoryAdapter()
        categoryRecyclerView.adapter = categoryAdapter

        // Set up "View All Expenses" button
        val allExpensesButton = view.findViewById<Button>(R.id.allExpensesButton)
        allExpensesButton.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            val transaction = fragmentManager.beginTransaction()

            // Navigate to HistoryFragment
            transaction.replace(R.id.expense_report, HistoryFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pieChart: PieChart = view.findViewById(R.id.expensePieChart)

        // Observe LiveData from ViewModel
        expenseViewModel.allEntriesLiveData.observe(viewLifecycleOwner) { expenses: List<Expense> ->
            updatePieChart(pieChart, expenses)
            updateCategoryList(expenses)
        }
    }

    private fun updatePieChart(pieChart: PieChart, expenses: List<Expense>) {
        // Group expenses by category
        val categoryTotals = expenses.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { expense -> expense.amount } }

        // Create PieChart entries
        val entries = categoryTotals.map { (category, total) ->
            PieEntry(total.toFloat(), category)
        }

        // Define colors for the chart
        val colors = listOf(
            Color.parseColor("#FF5722"), // Rent
            Color.parseColor("#FFC107"), // Grocery
            Color.parseColor("#03A9F4"), // Clothes
            Color.parseColor("#8BC34A"), // Entertainment
            Color.parseColor("#9E9E9E")  // Miscellaneous
        )

        // Configure the dataset
        val dataSet = PieDataSet(entries, "Expense Categories")
        dataSet.colors = colors
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 12f

        // Configure the data
        val data = PieData(dataSet)

        // Configure the PieChart
        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.setUsePercentValues(true)
        pieChart.setDrawHoleEnabled(false)
        pieChart.setEntryLabelColor(Color.WHITE)
        pieChart.setEntryLabelTextSize(12f)

        pieChart.invalidate()
    }

    private fun updateCategoryList(expenses: List<Expense>) {
        // Group expenses by category and calculate totals
        val categoryTotals = expenses.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        // Update the RecyclerView adapter with the data
        categoryAdapter.setCategories(categoryTotals.toList())
    }
}




