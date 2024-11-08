package com.example.broke_no_more.ui.add_expense

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.broke_no_more.databinding.FragmentAddExpenseBinding
import com.example.broke_no_more.ui.ocr.OcrTestActivity

class AddExpenseFragment : Fragment() {

    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!

    // Request code for OCR scan
    private val REQUEST_OCR_SCAN = 2001

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val addExpenseViewModel =
            ViewModelProvider(this).get(AddExpenseViewModel::class.java)

        _binding = FragmentAddExpenseBinding.inflate(inflater, container, false)
        val root: View = binding.root

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

    // saving the data ?
    private fun saveExpenseData() {
        val amount = binding.linearLayout.getChildAt(1) as EditText
        val date = binding.linearLayout2.getChildAt(1) as EditText
        val comment = binding.linearLayout4.getChildAt(1) as EditText

        val amountText = amount.text.toString()
        val dateText = date.text.toString()
        val commentText = comment.text.toString()

        // TODO : ADD DATABASE
        Toast.makeText(
            requireContext(),
            "Expense saved: Amount=$amountText, Date=$dateText, Comment=$commentText",
            Toast.LENGTH_SHORT
        ).show()
    }
}
