package com.example.broke_no_more.ui.expense_report

import CategoryAdapter
import android.graphics.Color
import android.graphics.Typeface
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
import com.github.mikephil.charting.animation.Easing
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
import com.github.mikephil.charting.formatter.ValueFormatter
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
            PieEntry(total.toFloat(), category) // Include category name for legend
        }

        val colors = listOf(
            Color.parseColor("#FFD700"),
            Color.parseColor("#FF6347"),
            Color.parseColor("#40E0D0"),
            Color.parseColor("#8A2BE2"),
            Color.parseColor("#9772ED"),
            Color.parseColor("#9E9E9E")
        )

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = colors
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 12f
        val typefaceBold = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        dataSet.valueTypeface = typefaceBold
        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val percent = value
                return "%.1f%%".format(percent)
            }
        }

        val data = PieData(dataSet)
        pieChart.description.isEnabled = false
        pieChart.setUsePercentValues(true)
        pieChart.setDrawHoleEnabled(false)
        pieChart.setEntryLabelColor(Color.TRANSPARENT)
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleRadius(60f)
        pieChart.setTransparentCircleRadius(65f)
        pieChart.setTransparentCircleColor(Color.WHITE)
        pieChart.setTransparentCircleAlpha(10)
        pieChart.setRotationEnabled(true)
        pieChart.setRotationAngle(10f)
        pieChart.animateY(1400, Easing.EaseInOutQuad)


        // Configure the legend
        val legend = pieChart.legend
        legend.isEnabled = true
        legend.verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
        legend.orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
        legend.setDrawInside(false)
        legend.textColor = Color.BLACK
        legend.textSize = 15f // text size
        legend.isWordWrapEnabled = true
        legend.xEntrySpace = 8f
        legend.typeface = Typeface.create("sans-serif", Typeface.BOLD)
        //legend.yEntrySpace = 0f
        //legend.xEntrySpace = 20f // horizontal spacing
        //legend.yOffset = 20f // vertical spacing
        pieChart.data = data
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

        val barDataSet = BarDataSet(entries, "Monthly Spending")
        barDataSet.color = Color.parseColor("#999999")
        barDataSet.valueTextColor = Color.parseColor("#999999")
        barDataSet.valueTypeface = Typeface.create("sans-serif", Typeface.BOLD)
        barDataSet.valueTextSize = 15f
        barDataSet.setDrawValues(true)
        barDataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "$${value.toInt()}"
            }
        }

        val barData = BarData(barDataSet)
        barData.barWidth = 0.8f
        barChart.data = barData

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(monthLabels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor = Color.parseColor("#666666")
        xAxis.typeface = Typeface.create("sans-serif", Typeface.BOLD)
        xAxis.textSize = 15f
        xAxis.granularity = 1f
        xAxis.isGranularityEnabled = true
        xAxis.setDrawGridLines(false)
        xAxis.axisLineWidth = 2f

        val yAxis = barChart.axisLeft
        yAxis.textColor = Color.parseColor("#666666")
        yAxis.typeface = Typeface.create("sans-serif", Typeface.BOLD)
        yAxis.axisMinimum = 0f
        yAxis.textSize = 15f
        yAxis.setDrawGridLines(false)
        yAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "$${value.toInt()}"
            }
        }
        yAxis.axisLineWidth = 2f

        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false
        barChart.setFitBars(true)
        barChart.legend.isEnabled = false
        barChart.animateY(1000)
        barChart.setPinchZoom(false)
        barChart.setScaleEnabled(false)
        barChart.setExtraBottomOffset(20f)


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

