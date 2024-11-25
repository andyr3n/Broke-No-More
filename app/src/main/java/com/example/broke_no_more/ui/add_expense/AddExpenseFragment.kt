package com.example.broke_no_more.ui.add_expense

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.broke_no_more.databinding.FragmentAddExpenseBinding
import com.example.broke_no_more.database.Expense
import com.example.broke_no_more.database.ExpenseDatabase
import com.example.broke_no_more.database.ExpenseRepository
import com.example.broke_no_more.database.ExpenseViewModel
import com.example.broke_no_more.database.ExpenseViewModelFactory
import com.example.broke_no_more.ui.ocr.OcrTestActivity
import java.text.SimpleDateFormat
import android.icu.util.Calendar
import java.util.Locale

class AddExpenseFragment : Fragment() {

    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ExpenseViewModel

    // Calendar instance for date selection
    private val calendar: Calendar = Calendar.getInstance()
    private lateinit var selectedCalendar: Calendar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddExpenseBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize ViewModel
        val database = ExpenseDatabase.getInstance(requireActivity())
        val repository = ExpenseRepository(database.expenseDatabaseDao)
        val viewModelFactory = ExpenseViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ExpenseViewModel::class.java)

        // Set up category spinner
        setupCategorySpinner()

        // Set up date picker
        setupDatePicker()

        // Set up the "Add Receipt" button to launch OCR
        binding.button.setOnClickListener {
            val intent = Intent(requireContext(), OcrTestActivity::class.java)
            startActivityForResult(intent, 2001)
        }

        // Set up the "Save" button
        binding.button2.setOnClickListener {
            saveExpenseData()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Populate the category spinner with predefined categories.
     */
    private fun setupCategorySpinner() {
        val spinner: Spinner = binding.categorySpinner

        // Predefined categories (these can be extended or fetched from a database)
        val categories = listOf("Rent", "Grocery", "Clothes", "Entertainment", "Miscellaneous")
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    /**
     * Set up the date picker dialog for the date EditText.
     */
    private fun setupDatePicker() {
        val dateEditText: EditText = binding.dateEditText
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

        dateEditText.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    dateEditText.setText(dateFormat.format(calendar.time))
                    selectedCalendar = calendar
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }
    }

    /**
     * Handle the result from the OCR activity.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2001 && resultCode == Activity.RESULT_OK) {
            val recognizedText = data?.getStringExtra("recognizedText") ?: ""

            // Extract data using regular expressions
            val dateRegex = Regex("""\b(\d{1,2}/\d{1,2}/\d{4})\b""")
            val amountRegex = Regex("""[\$€£]?\s?\d+(\.\d{2})?""")

            dateRegex.find(recognizedText)?.value?.let {
                binding.dateEditText.setText(it)
            }

            amountRegex.find(recognizedText)?.value?.let {
                binding.linearLayout.getChildAt(1).let { view ->
                    (view as EditText).setText(it)
                }
            }

            Toast.makeText(requireContext(), "Receipt data added successfully", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Save the entered expense data to the database.
     */
    private fun saveExpenseData() {
        val amountEditText = binding.linearLayout.getChildAt(1) as EditText
        val dateEditText = binding.dateEditText
        val commentEditText = binding.linearLayout4.getChildAt(1) as EditText
        val categorySpinner: Spinner = binding.categorySpinner

        val amountText = amountEditText.text.toString()
        val dateText = dateEditText.text.toString()
        val commentText = commentEditText.text.toString()
        val selectedCategory = categorySpinner.selectedItem.toString()

        // Validate the inputs
        if (amountText.isEmpty() || dateText.isEmpty() || selectedCategory.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all the fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Create and save the expense object
        val expense = Expense(
            date = selectedCalendar,
            amount = amountText.toDouble(),
            comment = commentText,
            category = selectedCategory
        )
        viewModel.insert(expense)

        Toast.makeText(
            requireContext(),
            "Expense saved:\nAmount=$amountText\nDate=$dateText\nCategory=$selectedCategory\nComment=$commentText",
            Toast.LENGTH_SHORT
        ).show()

        // Return to the previous screen
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }
}



