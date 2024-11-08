package com.example.broke_no_more.ui.register


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.broke_no_more.R

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Find views by ID
        val etEmail: EditText = findViewById(R.id.etEmail)
        val etPassword: EditText = findViewById(R.id.etPassword)
        val etConfirmPassword: EditText = findViewById(R.id.etConfirmPassword)
        val btnSignUp: Button = findViewById(R.id.btnSignUp)
        val tvSignIn: TextView = findViewById(R.id.tvSignIn)

        // Handle Sign Up Button click
        btnSignUp.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            // Validate input fields
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please confirm your password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Mock sign-up process (replace with real backend API call later)
            Toast.makeText(this, "Sign Up Successful!", Toast.LENGTH_SHORT).show()

            // Navigate to SignInActivity
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Handle navigation to Sign In screen
        tvSignIn.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
    }
}
