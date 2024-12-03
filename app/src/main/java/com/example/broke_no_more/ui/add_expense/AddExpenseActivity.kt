package com.example.broke_no_more.ui.add_expense

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.broke_no_more.databinding.ActivityAddExpenseBinding
import com.example.broke_no_more.database.Expense
import com.example.broke_no_more.database.ExpenseDatabase
import com.example.broke_no_more.database.ExpenseRepository
import com.example.broke_no_more.database.ExpenseViewModel
import com.example.broke_no_more.database.ExpenseViewModelFactory
import com.example.broke_no_more.ui.ocr.OcrTestActivity
import java.text.ParseException
import java.text.SimpleDateFormat
import android.icu.util.Calendar
import com.example.broke_no_more.R
import java.util.Date
import java.util.Locale

class AddExpenseActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "AddExpenseActivity"
    }

    private lateinit var binding: ActivityAddExpenseBinding
    private lateinit var viewModel: ExpenseViewModel

    private val calendar: Calendar = Calendar.getInstance()
    private lateinit var selectedCalendar: Calendar
    private lateinit var cancelBtn: Button
    private var recurrenceType = arrayOf("Never", "Monthly", "Annually")
    private lateinit var recurrenceSpinner: Spinner
    private lateinit var spinnerAdapter: ArrayAdapter<CharSequence>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recurrenceSpinner = findViewById(R.id.recurrence_spinner)
        spinnerAdapter = ArrayAdapter<CharSequence>(this, android.R.layout.simple_list_item_1, recurrenceType)
        recurrenceSpinner.adapter = spinnerAdapter

        // Initialize ViewModel
        val database = ExpenseDatabase.getInstance(this)
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
            val intent = Intent(this, OcrTestActivity::class.java)
            startActivityForResult(intent, 2001)
        }

        // Set up the "Save" button
        binding.button2.setOnClickListener {
            Log.d(TAG, "Save button clicked.")
            saveExpenseData()
        }

        // Cancel adding new expense
        cancelBtn = binding.cancelAddExpense
        cancelBtn.setOnClickListener{
            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            onBackPressed()
        }
    }

    /**
     * Populate the category spinner with predefined categories.
     */
    private fun setupCategorySpinner() {
        val spinner: Spinner = binding.categorySpinner
        val categories = listOf("Housing", "Grocery", "Clothes", "Entertainment", "Subscription", "Miscellaneous")
        val adapter = ArrayAdapter(
            this,
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
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    dateEditText.setText(dateFormat.format(calendar.time))
                    selectedCalendar = calendar.clone() as Calendar
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }
    }

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
                val parsedDate = parseDate(dateValueRaw)
                if (parsedDate != null) {
                    // Update the calendar with the parsed date
                    calendar.time = parsedDate
                    val formattedDate = SimpleDateFormat("MM/dd/yyyy", Locale.US).format(parsedDate)
                    binding.dateEditText.setText(formattedDate)
                    selectedCalendar = calendar.clone() as Calendar
                    Log.d(TAG, "selectedCalendar updated with parsed date.")
                } else {
                    binding.dateEditText.setText(dateValueRaw) // Set raw date if parsing fails
                }
            } else {
                Log.w(TAG, "Date not found in recognized text.")
                Toast.makeText(this, "Date not found", Toast.LENGTH_SHORT).show()
            }

            // Extract total amount
            val amountMatch = amountRegex.findAll(recognizedText)
                .mapNotNull { it.value.replace("[^\\d.]".toRegex(), "").toDoubleOrNull() }
                .toList()
            if (amountMatch.isNotEmpty()) {
                val maxAmount = amountMatch.maxOrNull() ?: 0.0
                binding.linearLayout.getChildAt(1).let {
                    (it as EditText).setText(maxAmount.toString()) // Store as plain number
                    Log.d(TAG, "Total amount set to EditText: $maxAmount")
                }
            } else {
                Toast.makeText(this, "Total amount not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun parseDate(text: String): Date? {
        val dateFormats = arrayOf(
            "MM/dd/yyyy", "MM/dd/yy", "MM/dd", "MM/yy",
            "yyyy-MM-dd", "M/d/yyyy", "M/d/yy", "M/yy"
        )

        for (format in dateFormats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.US)
                sdf.isLenient = false
                val date = sdf.parse(text)
                if (date != null) {
                    return date
                }
            } catch (e: ParseException) {
                Log.d(TAG, "parseDate: Failed to parse '$text' with format '$format'")
            }
        }
        Log.w(TAG, "parseDate: Unable to parse date from text '$text'")
        return null
    }

    private fun saveExpenseData() {
        val amountEditText = binding.linearLayout.getChildAt(1) as EditText
        val dateEditText = binding.dateEditText
        val commentEditText = binding.linearLayout4.getChildAt(1) as EditText
        val categorySpinner: Spinner = binding.categorySpinner
        val nameEditText = binding.expenseName

        val amountText = amountEditText.text.toString()
        val dateText = dateEditText.text.toString()
        val commentText = commentEditText.text.toString()
        val selectedCategory = categorySpinner.selectedItem.toString()

        // Validate the inputs
        if (amountText.isEmpty() || dateText.isEmpty() || selectedCategory.isEmpty()) {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Ensure selectedCalendar is initialized
        if (!::selectedCalendar.isInitialized) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
            return
        }

        // Create and save the expense object
        val expense = Expense(
            name = nameEditText.text.toString(),
            date = selectedCalendar,
            amount = amountText.toDouble(),
            comment = commentText,
            category = selectedCategory
        )

        //Change Recurrence status based on what user chose
        when(recurrenceSpinner.selectedItem.toString()){
            "Monthly" ->{
                expense.isSubscription = true
                expense.isMonthly = true
            }
            "Annually" ->{
                expense.isSubscription = true
                expense.isAnnually = true
            }
        }

        viewModel.insert(expense)
        Toast.makeText(this, "Expense saved", Toast.LENGTH_SHORT).show()

        // Navigate back
        onBackPressed()
    }
}
