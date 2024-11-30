package com.example.broke_no_more.ui.ocr

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.broke_no_more.R
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class ReceiptScannerActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ReceiptScannerActivity"
    }

    private lateinit var btnSelectImage: Button
    private lateinit var imageView: ImageView
    private lateinit var textView: TextView
    private lateinit var tvStoreName: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvTotalAmount: TextView

    private val REQUEST_IMAGE_PICK = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Initializing ReceiptScannerActivity")
        setContentView(R.layout.activity_receipt_scanner)

        // UI Bindings
        btnSelectImage = findViewById(R.id.btnSelectImage)
        imageView = findViewById(R.id.imageView)
        textView = findViewById(R.id.textView)
        tvStoreName = findViewById(R.id.tvStoreName)
        tvDate = findViewById(R.id.tvDate)
        tvTotalAmount = findViewById(R.id.tvTotalAmount)

        // Set click listener for selecting image
        btnSelectImage.setOnClickListener {
            Log.d(TAG, "Select Image button clicked")
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult: requestCode=$requestCode, resultCode=$resultCode")
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            Log.d(TAG, "Image URI received: $uri")
            uri?.let {
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                imageView.setImageBitmap(bitmap)
                Log.d(TAG, "Image displayed in ImageView")

                // Process the image with OCR
                processReceiptImage(bitmap)
            }
        }
    }

    private fun processReceiptImage(bitmap: Bitmap) {
        Log.d(TAG, "Starting OCR processing on the selected image")
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val recognizedText = visionText.text
                Log.d(TAG, "OCR Success: Recognized text -> $recognizedText")
                GlobalScope.launch(Dispatchers.Main) {
                    extractStructuredData(recognizedText)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "OCR Failure: ${e.message}")
                Toast.makeText(this, "Failed to recognize text: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun extractStructuredData(recognizedText: String) {
        Log.d(TAG, "Extracting structured data from recognized text")
        val lines = recognizedText.split("\n")
        var storeName = ""
        var date = ""
        var totalAmount = ""

        for (line in lines) {
            Log.d(TAG, "Processing line: '$line'")
            when {
                isDate(line) -> {
                    date = parseDate(line) ?: "Invalid Date Format"
                    Log.d(TAG, "Date extracted: $date from line: '$line'")
                }
                isTotalAmount(line) -> {
                    totalAmount = extractAmount(line)
                    Log.d(TAG, "Total amount extracted: $totalAmount from line: '$line'")
                }
                storeName.isEmpty() && isLikelyStoreName(line) -> {
                    storeName = line
                    Log.d(TAG, "Store name extracted: $storeName from line: '$line'")
                }
            }
        }

        // If totalAmount is still empty, try extracting the largest amount in the text
        if (totalAmount.isEmpty()) {
            totalAmount = extractTotalAmount(recognizedText)
            if (totalAmount.isNotEmpty()) {
                Log.d(TAG, "Total amount extracted using largest amount strategy: $totalAmount")
            }
        }

        // Update the TextViews with the extracted data
        tvStoreName.text = "Store Name: $storeName"
        tvDate.text = "Date: $date"
        tvTotalAmount.text = "Total Amount: $totalAmount"

        Log.d(TAG, "Structured Data Extracted - Store: $storeName, Date: $date, Total Amount: $totalAmount")

        // Show full recognized text as well
        textView.text = recognizedText
        Log.d(TAG, "Full recognized text displayed in TextView")
    }

    private fun isDate(text: String): Boolean {
        //  MM/dd/yyyy, MM/dd/yy, MM/dd, MM/yy, and yyyy-MM-dd formats
        val datePattern = Pattern.compile("\\b(\\d{1,2}/\\d{1,2}(?:/\\d{2,4})?|\\d{4}-\\d{2}-\\d{2})\\b")
        val matcher = datePattern.matcher(text)
        val isMatch = matcher.find()
        if (isMatch) {
            Log.d(TAG, "Date found in text: '${matcher.group()}'")
        }
        Log.d(TAG, "isDate('$text') = $isMatch")
        return isMatch
    }


    private fun parseDate(text: String): String? {
        // Define possible date formats, including MM/dd
        val dateFormats = arrayOf(
            "MM/dd/yyyy",
            "MM/dd/yy",
            "MM/dd",
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


    private fun isTotalAmount(text: String): Boolean {
        val lowerText = text.lowercase()
        // Extend keywords to include variations
        return lowerText.contains("total") || lowerText.contains("amount") || lowerText.contains("sib total") || lowerText.contains("gst") || lowerText.contains("pst")
    }

    private fun extractAmount(text: String): String {
        val amountPattern = Pattern.compile("\\$?\\s?\\d+[.,]\\d{2}")
        val matcher = amountPattern.matcher(text)
        val amounts = mutableListOf<String>()

        while (matcher.find()) {
            val amount = matcher.group()?.replace("\\s".toRegex(), "") ?: ""
            if (amount.isNotEmpty()) {
                amounts.add(amount)
                Log.d(TAG, "Found amount: $amount in line: '$text'")
            }
        }

        return if (amounts.isNotEmpty()) {
            val maxAmount = amounts.maxOf { it.toDoubleOrNull() ?: 0.0 }
            String.format("$%.2f", maxAmount)
        } else {
            ""
        }
    }

    private fun extractTotalAmount(recognizedText: String): String {
        val amountPattern = Pattern.compile("\\$?\\s?\\d+[.,]\\d{2}")
        val matcher = amountPattern.matcher(recognizedText)
        val amounts = mutableListOf<Double>()

        while (matcher.find()) {
            val amountStr = matcher.group()?.replace("[^\\d.]".toRegex(), "") ?: "0"
            val amount = amountStr.toDoubleOrNull() ?: 0.0
            amounts.add(amount)
            Log.d(TAG, "Found amount: $amount in text: '${matcher.group()}'")
        }

        return if (amounts.isNotEmpty()) {
            val maxAmount = amounts.maxOrNull() ?: 0.0
            String.format("$%.2f", maxAmount)
        } else {
            ""
        }
    }

    private fun isLikelyStoreName(text: String): Boolean {
        val keywords = listOf("shop", "store", "receipt", "market", "card", "visa", "mastercard", "amex", "credit", "cashier", "gst", "pst")
        val isStoreName = keywords.none { text.lowercase().contains(it) } && text.length in 5..40
        Log.d(TAG, "isLikelyStoreName('$text') = $isStoreName")
        return isStoreName
    }
}
