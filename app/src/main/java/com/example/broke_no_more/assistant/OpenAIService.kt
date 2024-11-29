package com.example.broke_no_more.assistant

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import java.util.concurrent.TimeUnit

// Data class for OpenAI API request
data class OpenAIRequest(
    @SerializedName("model") val model: String,
    @SerializedName("messages") val messages: List<Message>
)

// Data class for OpenAI API response
data class OpenAIResponse(
    @SerializedName("choices") val choices: List<Choice>
)

// Data class for choices in the response
data class Choice(
    @SerializedName("message") val message: Message
)

// Data class for messages (request and response)
data class Message(
    @SerializedName("role") val role: String,  // e.g., "user", "assistant"
    @SerializedName("content") val content: String
)

// Retrofit interface for OpenAI API
interface OpenAIService {
    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    suspend fun getChatResponse(@Body request: OpenAIRequest): Response<OpenAIResponse>

    companion object {
        private const val BASE_URL = "https://api.openai.com/"

        fun create(apiKey: String): OpenAIService {
            // Logging for debugging purposes
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            // Configure OkHttpClient with timeouts and logging
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $apiKey")
                        .build()
                    chain.proceed(request)
                }
                .connectTimeout(30, TimeUnit.SECONDS) // Connection timeout
                .readTimeout(30, TimeUnit.SECONDS)    // Read timeout
                .writeTimeout(30, TimeUnit.SECONDS)   // Write timeout
                .build()

            // Configure Retrofit
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(OpenAIService::class.java)
        }
    }
}


