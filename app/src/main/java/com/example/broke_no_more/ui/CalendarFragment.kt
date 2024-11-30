package com.example.broke_no_more.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.broke_no_more.database.ExpenseViewModel
import com.example.broke_no_more.databinding.FragmentCalendarBinding
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ExpenseViewModel by activityViewModels()

    private val calendar = Calendar.getInstance()
    private lateinit var calendarAdapter: CalendarAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)

        setupCalendar()
        setupObservers()

        binding.prevMonthButton.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateCalendar()
        }

        binding.nextMonthButton.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateCalendar()
        }

        return binding.root
    }

    private fun setupCalendar() {
        binding.calendarRecyclerView.layoutManager = GridLayoutManager(requireContext(), 7)
        updateCalendar()
    }

    private fun updateCalendar() {
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        binding.monthText.text = monthFormat.format(calendar.time)

        val daysInMonth = getDaysInMonth(calendar)

        val expensesByDate = viewModel.allEntriesLiveData.value?.groupBy {
            val date = it.date.time
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
        } ?: emptyMap()

        calendarAdapter = CalendarAdapter(daysInMonth, expensesByDate) { date ->
            showExpensesDialog(date)
        }
        binding.calendarRecyclerView.adapter = calendarAdapter
    }

    private fun setupObservers() {
        viewModel.allEntriesLiveData.observe(viewLifecycleOwner) {
            updateCalendar()
        }
    }

    private fun getDaysInMonth(calendar: Calendar): List<Date> {
        val days = mutableListOf<Date>()
        val tempCalendar = calendar.clone() as Calendar

        tempCalendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfMonth = tempCalendar.get(Calendar.DAY_OF_WEEK) - 1

        tempCalendar.add(Calendar.DAY_OF_MONTH, -firstDayOfMonth)

        for (i in 0 until 42) {
            days.add(tempCalendar.time)
            tempCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        return days
    }

    private fun showExpensesDialog(date: Date) {
        val dialog = ExpensesDialogFragment.newInstance(date)
        dialog.show(parentFragmentManager, "ExpensesDialogFragment")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
