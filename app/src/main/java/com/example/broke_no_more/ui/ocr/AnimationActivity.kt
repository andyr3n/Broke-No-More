package com.example.broke_no_more.ui.ocr

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.broke_no_more.R

class AnimationActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "AnimationActivity"
        const val ANIMATION_DURATION = 3000L // 3s
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animation_screen)

        Log.d(TAG, "Animation started")

        Handler().postDelayed({
            Log.d(TAG, "Animation finished, returning to OCR activity")
            finish()
        }, ANIMATION_DURATION)
    }
}
