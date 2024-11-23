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
import com.example.broke_no_more.ui.ocr.OcrTestActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddExpenseFragment : Fragment() {

    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!

    // Request code for OCR scan
    private val REQUEST_OCR_SCAN = 2001

    // Example categories (can be dynamically fetched from ViewModel or database)
    private val expenseCategories = listOf("Rent", "Grocery", "Clothes", "Entertainment", "Miscellaneous")

    private val calendar: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val addExpenseViewModel =
            ViewModelProvider(this).get(AddExpenseViewModel::class.java)

        _binding = FragmentAddExpenseBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Set up category spinner
        setupCategorySpinner()

        // Set up date picker
        setupDatePicker()

        // Set up the "Add Receipt" button to launch the OCR test activity
        binding.button.setOnClickListener {
            val intent = Intent(requireContext(), OcrTestActivity::class.java)
            startActivityForResult(intent, REQUEST_OCR_SCAN)
        }

        // Set up the "Save" button click listener
        binding.button2.setOnClickListener {
            saveExpenseData()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Populate the category spinner with categories
    private fun setupCategorySpinner() {
        val spinner: Spinner = binding.categorySpinner
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            expenseCategories
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    // Set up the date picker for the date EditText
    private fun setupDatePicker() {
        val dateEditText: EditText = binding.linearLayout2.getChildAt(1) as EditText
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

        dateEditText.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    dateEditText.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }
    }

    // Handle the result from the OCR test activity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_OCR_SCAN && resultCode == Activity.RESULT_OK) {
            // Get the recognized text from the OCR activity
            val recognizedText = data?.getStringExtra("recognizedText") ?: ""

            // Parse the recognized text to extract date and total amount
            val dateRegex = Regex("""\b(\d{1,2}/\d{1,2}/\d{4})\b""")
            val amountRegex = Regex("""[\$€£]?\s?\d+(\.\d{2})?""")

            // Extract date
            val dateMatch = dateRegex.find(recognizedText)
            if (dateMatch != null) {
                binding.linearLayout2.getChildAt(1).let { it as EditText }.setText(dateMatch.value)
            }

            // Extract total amount
            val amountMatch = amountRegex.find(recognizedText)
            if (amountMatch != null) {
                binding.linearLayout.getChildAt(1).let { it as EditText }.setText(amountMatch.value)
            }

            Toast.makeText(requireContext(), "Receipt data added successfully", Toast.LENGTH_SHORT).show()
        }
    }

    // Save expense data and show a Toast message
    private fun saveExpenseData() {
        val amount = binding.linearLayout.getChildAt(1) as EditText
        val date = binding.linearLayout2.getChildAt(1) as EditText
        val comment = binding.linearLayout4.getChildAt(1) as EditText
        val categorySpinner: Spinner = binding.categorySpinner

        val amountText = amount.text.toString()
        val dateText = date.text.toString()
        val commentText = comment.text.toString()
        val selectedCategory = categorySpinner.selectedItem.toString()

        // TODO: Save data to the database
        Toast.makeText(
            requireContext(),
            "Expense saved:\nAmount=$amountText\nDate=$dateText\nCategory=$selectedCategory\nComment=$commentText",
            Toast.LENGTH_SHORT
        ).show()
    }
}


