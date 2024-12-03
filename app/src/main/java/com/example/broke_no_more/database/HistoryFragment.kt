package com.example.broke_no_more.database

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.broke_no_more.R


class HistoryFragment : Fragment() {
    private lateinit var listView: ListView
    private lateinit var adapter: HistoryListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // go back to last fragment
//        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                findNavController().navigate(R.id.nav_expense_report)
//            }
//        })

        val view = inflater.inflate(R.layout.fragment_history, container, false)
        listView = view.findViewById(R.id.listViewEntries)

        val database = ExpenseDatabase.getInstance(requireActivity())
        val databaseDao = database.expenseDatabaseDao
        val repository = ExpenseRepository(databaseDao)
        val viewModelFactory = ExpenseViewModelFactory(repository)
        val expenseViewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(ExpenseViewModel::class.java)
        adapter = HistoryListAdapter(requireContext(), emptyList())
        listView.adapter = adapter
        expenseViewModel.allEntriesLiveData.observe(viewLifecycleOwner) {
            adapter.replace(it)
            adapter.notifyDataSetChanged()
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val expense = adapter.getItem(position)
            expense.let {
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

                val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
                val transaction: FragmentTransaction = fragmentManager.beginTransaction()

                transaction.replace(R.id.fragment_history, expenseFragment)
                transaction.addToBackStack(null)
                transaction.commit()
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()

        listView.setAdapter(listView.getAdapter())
    }
}
