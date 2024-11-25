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
import java.util.regex.Pattern

class OcrTestActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "OcrTestActivity"
    }

    private lateinit var btnSelectImage: Button
    private lateinit var imageView: ImageView
    private lateinit var textView: TextView
    private val REQUEST_IMAGE_PICK = 101
    private lateinit var btnDone: Button
    private var recognizedText: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Initializing OcrTestActivity")
        setContentView(R.layout.activity_ocr_test)

        // UI Bindings
        btnSelectImage = findViewById(R.id.btnSelectImage)
        imageView = findViewById(R.id.imageView)
        textView = findViewById(R.id.textView)
        btnDone = findViewById(R.id.btnDone)

        recognizedText = "Sample OCR recognized text"
        textView.text = recognizedText
        Log.d(TAG, "Initial recognizedText set to: $recognizedText")

        // Set click listener for selecting image
        btnSelectImage.setOnClickListener {
            Log.d(TAG, "Select Image button clicked")
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
        }

        // Set click listener for Done button
        btnDone.setOnClickListener {
            Log.d(TAG, "Done button clicked. Returning recognized text.")
            returnResultToFragment(recognizedText)
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
                recognizedText = visionText.text
                Log.d(TAG, "OCR Success: Recognized text -> $recognizedText")
                extractRelevantData(recognizedText)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "OCR Failure: ${e.message}")
                Toast.makeText(this, "Failed to recognize text: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun extractRelevantData(text: String) {
        // Extract Store Name (Assuming it's the first non-empty line)
        val storeName = text.lines().firstOrNull { it.isNotBlank() && !it.contains("Receipt", ignoreCase = true) } ?: "Unknown Store"
        Log.d(TAG, "Store Name extracted: $storeName")

        // Extract Date using regex
        val datePattern = Pattern.compile("\\b(\\d{1,2}/\\d{1,2}/\\d{2,4}|\\d{4}-\\d{2}-\\d{2})\\b")
        val dateMatcher = datePattern.matcher(text)
        val date = if (dateMatcher.find()) dateMatcher.group() else "Not Found"
        Log.d(TAG, "Date extracted: $date")

        // Extract Total Amount by finding the largest amount with currency symbol
        // Updated amountPattern to match amountRegex: Optional '$', optional space, digits, followed by '.' or ',' and two digits
        val amountPattern = Pattern.compile("\\$?\\s?\\d+[.,]\\d{2}")
        val amountMatcher = amountPattern.matcher(text)
        val amounts = mutableListOf<Double>()
        while (amountMatcher.find()) {
            val amountStr = amountMatcher.group()?.replace("[^0-9.]".toRegex(), "")
            amountStr?.toDoubleOrNull()?.let { amounts.add(it) }
        }
        val totalAmount = if (amounts.isNotEmpty()) "$${"%.2f".format(amounts.maxOrNull()!!)}" else "Not Found"
        Log.d(TAG, "Total Amount extracted: $totalAmount")

        // Create a summarized string
        val summarizedText = """
        Store Name: $storeName
        Date: $date
        Total Amount: $totalAmount
    """.trimIndent()

        // Update the TextView with summarized data
        textView.text = summarizedText
        Log.d(TAG, "Summarized text set: $summarizedText")
    }


    private fun returnResultToFragment(recognizedText: String) {
        Log.d(TAG, "Returning recognized text to fragment: $recognizedText")
        val resultIntent = Intent()
        resultIntent.putExtra("recognizedText", recognizedText)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}
