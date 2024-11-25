package com.example.broke_no_more.ui.add_expense

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import java.text.ParseException
import java.text.SimpleDateFormat
import android.icu.util.Calendar
import java.util.Locale

class AddExpenseFragment : Fragment() {

    companion object {
        private const val TAG = "AddExpenseFragment"
    }

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
            Log.d(TAG, "Add Receipt button clicked. Launching OcrTestActivity.")
            val intent = Intent(requireContext(), OcrTestActivity::class.java)
            startActivityForResult(intent, 2001)
        }

        // Set up the "Save" button
        binding.button2.setOnClickListener {
            Log.d(TAG, "Save button clicked.")
            saveExpenseData()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d(TAG, "onDestroyView: AddExpenseFragment destroyed")
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
            // Get the recognized text from the OCR activity
            val recognizedText = data?.getStringExtra("recognizedText") ?: ""
            Log.d(TAG, "Recognized text received: $recognizedText")

            // Parse the recognized text to extract date and total amount
            val dateRegex = Regex("""\b(\d{1,2}/\d{1,2}/\d{2,4}|\d{4}-\d{2}-\d{2})\b""")
            val amountRegex = Regex("""\$?\s?\d+[.,]\d{2}""")

            // Extract date
            val dateMatch = dateRegex.find(recognizedText)
            if (dateMatch != null) {
                val dateValueRaw = dateMatch.value
                Log.d(TAG, "Date extracted raw: $dateValueRaw")
                val formattedDate = parseDate(dateValueRaw)
                if (formattedDate != null) {
                    binding.linearLayout2.getChildAt(1).let {
                        (it as EditText).setText(formattedDate)
                        Log.d(TAG, "Date set to EditText: $formattedDate")
                    }
                } else {
                    binding.linearLayout2.getChildAt(1).let {
                        (it as EditText).setText(dateValueRaw) // Set raw date if parsing fails
                        Log.d(TAG, "Date set to EditText (raw): $dateValueRaw")
                    }
                }
            } else {
                Log.w(TAG, "Date not found in recognized text.")
                Toast.makeText(requireContext(), "Date not found", Toast.LENGTH_SHORT).show()
            }

            // Extract total amount
            val amountMatch = amountRegex.findAll(recognizedText)
                .mapNotNull { it.value.replace("[^\\d.]".toRegex(), "").toDoubleOrNull() }
                .toList()
            if (amountMatch.isNotEmpty()) {
                val maxAmount = amountMatch.maxOrNull() ?: 0.0
                val formattedAmount = String.format("$%.2f", maxAmount)
                binding.linearLayout.getChildAt(1).let {
                    (it as EditText).setText(formattedAmount)
                    Log.d(TAG, "Total amount set to EditText: $formattedAmount")
                }
            } else {
                Log.w(TAG, "Total amount not found in recognized text.")
                Toast.makeText(requireContext(), "Total amount not found", Toast.LENGTH_SHORT).show()
            }

            Toast.makeText(requireContext(), "Receipt data added successfully", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Receipt data added successfully.")
        }
    }

    // Parsing function similar to ReceiptScannerActivity
    private fun parseDate(text: String): String? {
        // Define possible date formats, including MM/dd
        val dateFormats = arrayOf(
            "MM/dd/yyyy",
            "MM/dd/yy",
            "MM/dd",          // Added format without year
            "MM/yy",
            "yyyy-MM-dd",
            "M/d/yyyy",
            "M/d/yy",
            "M/yy"
        )

        for (format in dateFormats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.US)
                sdf.isLenient = false
                val date = sdf.parse(text)
                if (date != null) {
                    // If year is missing, assume the current year
                    val calendar = Calendar.getInstance()
                    calendar.time = date
                    if (format == "MM/dd") {
                        calendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR))
                        return SimpleDateFormat("MMM dd, yyyy", Locale.US).format(calendar.time)
                    }
                    // Format the date to a standard format, e.g., "MMM dd, yyyy"
                    return SimpleDateFormat("MMM dd, yyyy", Locale.US).format(date)
                }
            } catch (e: ParseException) {
                // Continue to try the next format
                Log.d(TAG, "parseDate: Failed to parse '$text' with format '$format'")
            }
        }
        // If no format matches, return null
        Log.w(TAG, "parseDate: Unable to parse date from text '$text'")
        return null
    }


    // Saving the data
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
        Log.d(TAG, "Expense inserted into database: $expense")

        Toast.makeText(
            requireContext(),
            "Expense saved:\nAmount=$amountText\nDate=$dateText\nCategory=$selectedCategory\nComment=$commentText",
            Toast.LENGTH_SHORT
        ).show()
        Log.d(TAG, "Expense data saved successfully.")

        // Navigate back
        requireActivity().onBackPressedDispatcher.onBackPressed()
        Log.d(TAG, "Navigated back after saving expense.")
    }
}



