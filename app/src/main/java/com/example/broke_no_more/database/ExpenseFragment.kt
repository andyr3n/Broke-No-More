package com.example.broke_no_more.database

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.broke_no_more.R

class ExpenseFragment : Fragment() {
    private lateinit var dateEditText: EditText
    private lateinit var amountEditText: EditText
    private lateinit var commentEditText: EditText
    private lateinit var categoryEditText: EditText
    private lateinit var expenseViewModel: ExpenseViewModel
    private var expenseId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        expenseViewModel = ViewModelProvider(requireActivity()).get(ExpenseViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_expense, container, false)

        dateEditText = view.findViewById(R.id.dateEditText)
        amountEditText = view.findViewById(R.id.amountEditText)
        commentEditText = view.findViewById(R.id.commentEditText)
        categoryEditText = view.findViewById(R.id.categoryEditText)

        val date = arguments?.getString("date")
        val amount = arguments?.getDouble("amount")
        val comment = arguments?.getString("comment")
        val category = arguments?.getString("category")
        expenseId = arguments?.getLong("id")

        if (date != null) {
            dateEditText.setText(date.substring(0, 10))
        }
        amountEditText.setText("$" + amount.toString())
        commentEditText.setText(comment.toString())
        categoryEditText.setText(category.toString())

        return view
    }

//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.menu_main, menu)
//        super.onCreateOptionsMenu(menu, inflater)
//    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            R.id.action_delete -> {
//                expenseId?.let { id ->
//                    expenseViewModel.deleteExpenseById(id)
//                }
//                requireActivity().supportFragmentManager.popBackStack()
//                true
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//    }
}

