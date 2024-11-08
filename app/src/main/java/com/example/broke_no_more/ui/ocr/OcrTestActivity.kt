package com.example.broke_no_more.ui.ocr

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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

class OcrTestActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_IMAGE_PICK = 1001
    }

    private lateinit var imageView: ImageView
    private lateinit var textView: TextView
    private lateinit var btnSelectImage: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ocr_test)

        imageView = findViewById(R.id.imageView)
        textView = findViewById(R.id.textView)
        btnSelectImage = findViewById(R.id.btnSelectImage)

        // Button click to pick an image from the gallery
        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = data?.data
            if (imageUri != null) {
                imageView.setImageURI(imageUri)
                runTextRecognition(imageUri)
            }
        }
    }

    private fun runTextRecognition(imageUri: Uri) {
        try {
            val image = InputImage.fromFilePath(this, imageUri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    Log.d("OCR_DEBUG", "Full Recognized Text:\n${visionText.text}")
                    processTextRecognitionResult(visionText.text)
                }
                .addOnFailureListener { e ->
                    runOnUiThread {
                        Toast.makeText(this, "Text recognition failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    // This is the processTextRecognitionResult method
    private fun processTextRecognitionResult(text: String) {
        var storeName: String? = null
        var totalAmount: String? = null
        var date: String? = null

        // Split the recognized text into lines
        val lines = text.split("\n")
        Log.d("OCR_DEBUG", "Number of lines: ${lines.size}")

        // Regular expressions for matching date and amounts
        val dateRegex = Regex("""\b(\d{1,2}/\d{1,2}/\d{4})\b""")
        val amountRegex = Regex("""[\$€£]?\s?\d+(\.\d{2})?""")
        val storeNameRegex = Regex("""^[A-Z\s'`]+$""") // Matches lines with uppercase letters and special characters

        // Flag to indicate that we have found the "Total" keyword
        var foundTotalKeyword = false
        val amountList = mutableListOf<Double>()

        // Loop through each line
        for (line in lines) {
            val trimmedLine = line.trim()
            Log.d("OCR_DEBUG", "Processing line: $trimmedLine")

            // Detect store name (uppercase text with no numbers, likely to be the store name)
            if (storeName == null && storeNameRegex.matches(trimmedLine) && !trimmedLine.contains("Receipt", true)
                && !trimmedLine.contains("Manager", true) && !trimmedLine.contains("Address", true)) {
                storeName = trimmedLine
                Log.d("OCR_DEBUG", "Detected Store Name: $storeName")
            }

            // Check for date using the date regex
            if (date == null) {
                val dateMatch = dateRegex.find(trimmedLine)
                if (dateMatch != null) {
                    date = dateMatch.value
                    Log.d("OCR_DEBUG", "Detected Date: $date")
                }
            }

            // Check for the "Total" keyword
            if (trimmedLine.contains("Total", true)) {
                foundTotalKeyword = true
                Log.d("OCR_DEBUG", "Found 'Total' keyword.")
                continue // Move to the next line to look for the total amount
            }

            // If we have found the "Total" keyword, look for valid monetary amounts
            if (foundTotalKeyword) {
                val amountMatch = amountRegex.find(trimmedLine)
                if (amountMatch != null) {
                    val amountString = amountMatch.value.replace("$", "").trim()
                    Log.d("OCR_DEBUG", "Detected Amount String: $amountString")

                    // Validate the detected amount
                    try {
                        val amount = amountString.toDouble()

                        // Only add the amount if it is reasonable (e.g., not a credit card number)
                        if (amount >= 0.01 && amount <= 10000) {
                            amountList.add(amount)
                            Log.d("OCR_DEBUG", "Added Valid Amount: $amount")
                        } else {
                            Log.d("OCR_DEBUG", "Ignored Invalid Amount: $amount")
                        }
                    } catch (e: NumberFormatException) {
                        Log.d("OCR_DEBUG", "Invalid Amount Format: $amountString")
                    }
                }
            }
        }

        // Determine the total amount as the maximum value in the list
        if (amountList.isNotEmpty()) {
            totalAmount = "$" + String.format("%.2f", amountList.maxOrNull())
            Log.d("OCR_DEBUG", "Final Total Amount: $totalAmount")
        }

        // Display the extracted information in the TextView
        runOnUiThread {
            textView.text = """
            Store Name: ${storeName ?: "Not found"}
            Date: ${date ?: "Not found"}
            Total Amount: ${totalAmount ?: "Not found"}
        """.trimIndent()
        }
    }






}