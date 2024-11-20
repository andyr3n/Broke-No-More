package com.example.broke_no_more.ui.expense_report

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.broke_no_more.R
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

class ExpenseReportFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.expense_report, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pieChart: PieChart = view.findViewById(R.id.expensePieChart)

        // Placeholder data
        val expenseCategories = mapOf(
            "Rent" to 600f,
            "Grocery" to 300f,
            "Clothes" to 200f,
            "Entertainment" to 150f,
            "Miscellaneous" to 100f
        )

        // Create PieChart entries
        val entries = expenseCategories.map { (category, amount) ->
            PieEntry(amount, category)
        }

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

        // Refresh the chart
        pieChart.invalidate()
    }
}

