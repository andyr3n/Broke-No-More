package com.example.broke_no_more.assistant

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.room.Room
import com.example.broke_no_more.BuildConfig
import com.example.broke_no_more.R
import com.example.broke_no_more.database.Expense
import com.example.broke_no_more.database.ExpenseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class AIChatDialogFragment : DialogFragment() {

    private lateinit var userInput: EditText
    private lateinit var sendButton: Button
    private lateinit var chatOutput: TextView
    private lateinit var database: ExpenseDatabase

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(true) // Allows dismissing the dialog when tapping outside
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.dialog_ai_chat, container, false)

        userInput = view.findViewById(R.id.userInput)
        sendButton = view.findViewById(R.id.sendButton)
        chatOutput = view.findViewById(R.id.chatOutput)

        // Initialize Room Database
        database = Room.databaseBuilder(
            requireContext(),
            ExpenseDatabase::class.java,
            "expense_database"
        ).build()

        sendButton.setOnClickListener {
            val userMessage = userInput.text.toString().trim()
            if (userMessage.isNotEmpty()) {
                sendMessage(userMessage)
            }
        }

        return view
    }

    private fun sendMessage(message: String) {
        // Display the user's message in the chat output
        chatOutput.append("You: $message\n")
        userInput.text.clear()

        // Use OpenAI API to get a response
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val expenses = database.expenseDatabaseDao.getAllExpenses()
                val subscriptions = database.expenseDatabaseDao.getAllSubscriptions()

                val personalizedPrompt = buildPersonalizedPrompt(message, expenses, subscriptions)

                val responseText = fetchResponseFromAPI(personalizedPrompt)

                CoroutineScope(Dispatchers.Main).launch {
                    chatOutput.append("AI: $responseText\n")
                }
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    chatOutput.append("Error: Unable to fetch a response.\n")
                }
            }
        }
    }

    private fun buildPersonalizedPrompt(
        userMessage: String,
        expenses: List<Expense>,
        subscriptions: List<Expense>
    ): String {
        val totalExpense = expenses.sumOf { it.amount }
        val subscriptionDetails = subscriptions.joinToString("\n") {
            "- ${it.name}: \$${it.amount} (${if (it.isMonthly) "Monthly" else "Annually"})"
        }

        return """
            You are a financial assistant. Provide tailored financial tips for the following user:
            
            User's Message: "$userMessage"
            
            Financial Data:
            - Total Monthly Expenses: $$totalExpense
            - Subscriptions:
            $subscriptionDetails

            Give actionable advice based on this data.
        """.trimIndent()
    }

    private fun fetchResponseFromAPI(message: String): String {
        val apiKey = BuildConfig.OPENAI_API_KEY
        val url = "https://api.openai.com/v1/chat/completions"

        val messages = listOf(
            mapOf(
                "role" to "user",
                "content" to message
            )
        )

        val jsonBody = JSONObject()
        jsonBody.put("model", "gpt-3.5-turbo")
        jsonBody.put("messages", JSONArray(messages)) // Use JSONArray for proper formatting

        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody)
            .build()

        val client = OkHttpClient()

        return try {
            val response = client.newCall(request).execute()

            // Log raw response for debugging
            val rawResponse = response.body?.string()
            Log.d("OpenAI_Response", "Raw response: $rawResponse")

            if (!response.isSuccessful) {
                throw IOException("Unexpected code $response")
            }

            val jsonResponse = JSONObject(rawResponse ?: "")
            val choices = jsonResponse.getJSONArray("choices")
            val content = choices.getJSONObject(0).getJSONObject("message").getString("content")
            content
        } catch (e: Exception) {
            e.printStackTrace()
            "Error: Unable to fetch a response."
        }
    }
}



