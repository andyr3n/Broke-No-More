package com.example.broke_no_more.ui.expense_report

import CategoryAdapter
import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*

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
        val barChart: BarChart = view.findViewById(R.id.monthlyBarChart)

        // Observe LiveData from ViewModel
        expenseViewModel.allEntriesLiveData.observe(viewLifecycleOwner) { expenses: List<Expense> ->
            updatePieChart(pieChart, expenses)
            updateBarChart(barChart, expenses)
            updateCategoryList(expenses)
        }
    }

    private fun updatePieChart(pieChart: PieChart, expenses: List<Expense>) {
        val categoryTotals = expenses.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val entries = categoryTotals.map { (category, total) ->
            PieEntry(total.toFloat(), category)
        }

        val colors = listOf(
            Color.parseColor("#FF5722"), // Rent
            Color.parseColor("#FFC107"), // Grocery
            Color.parseColor("#03A9F4"), // Clothes
            Color.parseColor("#8BC34A"), // Entertainment
            Color.parseColor("#9E9E9E")  // Miscellaneous
        )

        val dataSet = PieDataSet(entries, "Expense Categories")
        dataSet.colors = colors
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 12f

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.setUsePercentValues(true)
        pieChart.setDrawHoleEnabled(false)
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.setEntryLabelTextSize(12f)
        pieChart.invalidate()
    }

    private fun updateBarChart(barChart: BarChart, expenses: List<Expense>) {
        val monthKeyFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault()) // For grouping
        val monthLabelFormat = SimpleDateFormat("MMM", Locale.getDefault()) // For display labels
        val calendar = Calendar.getInstance()

        // Prepare the last 6 months in ascending order (oldest to newest)
        val lastSixMonths = mutableListOf<String>()
        val monthLabels = mutableListOf<String>()
        for (i in 5 downTo 0) {
            calendar.add(Calendar.MONTH, -i) // Move back i months
            lastSixMonths.add(monthKeyFormat.format(calendar.time)) // e.g., "2024-06"
            monthLabels.add(monthLabelFormat.format(calendar.time)) // e.g., "Jun"
            calendar.add(Calendar.MONTH, i) // Reset calendar to current month
        }

        // Initialize monthly totals for the last 6 months
        val monthlyTotals = lastSixMonths.associateWith { 0f }.toMutableMap()

        // Aggregate expenses by month
        for (expense in expenses) {
            val expenseMonth = monthKeyFormat.format(expense.date.time)
            if (expenseMonth in monthlyTotals) {
                monthlyTotals[expenseMonth] = monthlyTotals[expenseMonth]!! + expense.amount.toFloat()
            }
        }

        // Debugging: Log the monthly totals
        monthlyTotals.forEach { (month, total) ->
            Log.d("BarChart", "Month: $month, Total: $total")
        }

        // Create BarChart entries in ascending order
        val entries = monthlyTotals.values.mapIndexed { index, total ->
            BarEntry(index.toFloat(), total)
        }

        // Configure the BarDataSet
        val barDataSet = BarDataSet(entries, "Monthly Spending")
        barDataSet.color = Color.CYAN
        barDataSet.valueTextColor = Color.BLACK
        barDataSet.valueTextSize = 12f

        // Configure BarData and assign it to the BarChart
        val barData = BarData(barDataSet)
        barData.barWidth = 0.5f
        barChart.data = barData

        // Configure x-axis with ascending month labels
        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(monthLabels) // Month names as x-axis labels
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor = Color.BLACK
        xAxis.textSize = 12f
        xAxis.granularity = 1f
        xAxis.isGranularityEnabled = true

        // Configure other chart settings
        barChart.description.isEnabled = false
        barChart.setFitBars(true)
        barChart.axisLeft.textColor = Color.BLACK
        barChart.axisLeft.axisMinimum = 0f // Ensure the y-axis starts from 0
        barChart.axisRight.isEnabled = false
        barChart.legend.textColor = Color.BLACK

        // Refresh the BarChart
        barChart.invalidate()
    }


    private fun updateCategoryList(expenses: List<Expense>) {
        val categoryData = expenses.groupBy { it.category }.map { (category, expensesInCategory) ->
            category to expensesInCategory.groupBy { it.comment.orEmpty() }
                .map { (subCategory, subExpenses) -> subCategory to subExpenses.sumOf { it.amount } }
        }
        categoryAdapter.setCategories(categoryData)
    }
}

