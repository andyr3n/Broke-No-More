package com.example.broke_no_more.ui.add_expense

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.broke_no_more.databinding.FragmentAddExpenseBinding
import com.example.broke_no_more.ui.home.Expense
import com.example.broke_no_more.ui.home.ExpenseDatabase
import com.example.broke_no_more.ui.home.ExpenseRepository
import com.example.broke_no_more.ui.home.ExpenseViewModel
import com.example.broke_no_more.ui.home.ExpenseViewModelFactory
import com.example.broke_no_more.ui.ocr.OcrTestActivity
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseFragment : Fragment() {

    companion object {
        private const val TAG = "AddExpenseFragment"
    }

    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!

    // Request code for OCR scan
    private val REQUEST_OCR_SCAN = 2001

    private lateinit var viewModel: ExpenseViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView: Initializing AddExpenseFragment")
        _binding = FragmentAddExpenseBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Set up the "Add Receipt" button to launch the OCR test activity
        binding.button.setOnClickListener {
            Log.d(TAG, "Add Receipt button clicked. Launching OcrTestActivity.")
            val intent = Intent(requireContext(), OcrTestActivity::class.java)
            startActivityForResult(intent, REQUEST_OCR_SCAN)
        }

        // Set up the "Save" button click listener
        binding.button2.setOnClickListener {
            Log.d(TAG, "Save button clicked.")
            saveExpenseData()
        }

        // Initialize ViewModel
        val database = ExpenseDatabase.getInstance(requireActivity())
        val databaseDao = database.expenseDatabaseDao
        val repository = ExpenseRepository(databaseDao)
        val viewModelFactory = ExpenseViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ExpenseViewModel::class.java)
        Log.d(TAG, "ViewModel initialized.")

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d(TAG, "onDestroyView: AddExpenseFragment destroyed")
    }

    // Handle the result from the OCR test activity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult: requestCode=$requestCode, resultCode=$resultCode")
        if (requestCode == REQUEST_OCR_SCAN && resultCode == Activity.RESULT_OK) {
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
        val dateEditText = binding.linearLayout2.getChildAt(1) as EditText
        val commentEditText = binding.linearLayout4.getChildAt(1) as EditText

        val amountText = amountEditText.text.toString()
        val dateText = dateEditText.text.toString()
        val commentText = commentEditText.text.toString()

        Log.d(TAG, "Saving Expense - Amount: $amountText, Date: $dateText, Comment: $commentText")

        // Validate inputs
        if (amountText.isBlank() || dateText.isBlank()) {
            Log.w(TAG, "Validation failed: Amount or Date is blank.")
            Toast.makeText(requireContext(), "Amount and Date are required", Toast.LENGTH_SHORT).show()
            return
        }

        val expense = try {
            Expense(
                date = dateText,
                amount = amountText.removePrefix("$").toDouble(),
                comment = commentText,
            )
        } catch (e: NumberFormatException) {
            Log.e(TAG, "NumberFormatException: ${e.message}")
            Toast.makeText(requireContext(), "Invalid amount format", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.insert(expense)
        Log.d(TAG, "Expense inserted into database: $expense")

        Toast.makeText(
            requireContext(),
            "Expense saved: Amount=$amountText, Date=$dateText, Comment=$commentText",
            Toast.LENGTH_SHORT
        ).show()
        Log.d(TAG, "Expense data saved successfully.")

        // Navigate back
        requireActivity().onBackPressedDispatcher.onBackPressed()
        Log.d(TAG, "Navigated back after saving expense.")
    }
}
