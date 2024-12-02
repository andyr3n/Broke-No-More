package com.example.broke_no_more.ui.register

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.broke_no_more.MainActivity
import com.example.broke_no_more.R
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var tvTitle: TextView
    private lateinit var btnLoginAsGuest: Button
    private val titleText = "Broke No More"
    private val delay: Long = 150
    private val dotAnimationDelay: Long = 500
    private var isDotAnimating = true

    private fun startTypingAnimation() {
        val handler = Handler()
        tvTitle.text = ""

        // Typing animation for "Broke No More"
        for (i in titleText.indices) {
            handler.postDelayed({
                tvTitle.append(titleText[i].toString())
                if (i == titleText.length - 1) {
                    // Start dot animation after text is fully typed
                    startDotAnimation(handler)
                }
            }, delay * i)
        }
    }

    private fun startDotAnimation(handler: Handler) {
        var dotCount = 0
        val maxDots = 3
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (!isDotAnimating) return

                // Update the dots
                val baseText = titleText + ".".repeat(dotCount)
                tvTitle.text = baseText

                // Increment dots or reset
                dotCount = (dotCount + 1) % (maxDots + 1)

                // Repeat the animation
                handler.postDelayed(this, dotAnimationDelay)
            }
        }, dotAnimationDelay)
    }

    override fun onDestroy() {
        super.onDestroy()
        isDotAnimating = false // Stop animation when activity is destroyed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        tvTitle = findViewById(R.id.tvTitle)
        btnLoginAsGuest = findViewById(R.id.btnLoginAsGuest) // Initialize the new button

        startTypingAnimation()

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Find views
        val etEmail: EditText = findViewById(R.id.etEmail)
        val etPassword: EditText = findViewById(R.id.etPassword)
        val btnSignIn: Button = findViewById(R.id.btnSignIn)
        val tvForgotPassword: TextView = findViewById(R.id.tvForgotPassword)
        val tvSignUp: TextView = findViewById(R.id.tvSignUp)

        // Handle Sign In Button click
        btnSignIn.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter your email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Authenticate user with Firebase
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null && user.isEmailVerified) {
                            // Navigate to MainActivity
                            val intent = Intent(this, MainActivity::class.java)
                            intent.putExtra("navigateToHome", true)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                this,
                                "Please verify your email address.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this,
                            "Authentication failed: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        // Handle Forgot Password click
        tvForgotPassword.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email address.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Password reset email sent.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            "Failed to send reset email: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        // Navigate to Sign Up screen
        tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        // Handle "Login as Guest" Button click
        btnLoginAsGuest.setOnClickListener {
            // Navigate to MainActivity without authentication
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("isGuest", true) // Optional: Pass an extra to indicate guest login
            startActivity(intent)
            finish()
        }
    }
}
