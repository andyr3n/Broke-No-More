package com.example.broke_no_more.ui.register

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.broke_no_more.MainActivity
import com.example.broke_no_more.R

class SignInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

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

            // Mock validation
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter your email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Navigate directly to HomeFragment via MainActivity
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("navigateToHome", true)
            startActivity(intent)
            finish()
        }

        // Handle Forgot Password click
        tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Forgot Password clicked", Toast.LENGTH_SHORT).show()
        }

        // Navigate to Sign Up screen
        tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}
