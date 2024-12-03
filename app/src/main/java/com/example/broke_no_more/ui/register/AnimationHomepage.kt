package com.example.broke_no_more.ui.register

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.broke_no_more.MainActivity
import com.example.broke_no_more.R

class AnimationHomepage : AppCompatActivity() {

    companion object {
        private const val TAG = "AnimationHome"
        const val ANIMATION_DURATION = 800L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_animation)

        // Retrieve extras to determine navigation path
        val isGuest = intent.getBooleanExtra("isGuest", false)
        val navigateToHome = intent.getBooleanExtra("navigateToHome", false)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)

            if (isGuest) {
                intent.putExtra("isGuest", true)
            } else if (navigateToHome) {
                intent.putExtra("navigateToHome", true)
            }

            startActivity(intent)
            finish()
        }, ANIMATION_DURATION)
    }
}
