package com.example.assignment1

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.content.Intent
import android.widget.LinearLayout
import android.widget.TextView
import android.view.Gravity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            // Initialize Firebase
            FirebaseApp.initializeApp(this)

            // Create a simple splash screen programmatically
            createSimpleSplashScreen()

            // Splash screen with 5-second delay - ALWAYS go to login
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    android.util.Log.d("MainActivity", "Splash complete - ALWAYS going to LoginActivity")
                    
                    // ALWAYS show login screen, never auto-login
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    android.util.Log.e("MainActivity", "Error navigating: ${e.message}", e)
                    e.printStackTrace()
                }
            }, 5000) // 5000 ms = 5 seconds (as per assignment rubric)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error in onCreate", e)
            finish()
        }
    }

    private fun createSimpleSplashScreen() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(android.graphics.Color.WHITE)
        }

        val logo = TextView(this).apply {
            text = "Socially"
            textSize = 48f
            setTextColor(android.graphics.Color.parseColor("#784A34"))
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "from SMD"
            textSize = 20f
            setTextColor(android.graphics.Color.GRAY)
            gravity = Gravity.CENTER
        }

        layout.addView(logo)
        layout.addView(subtitle)
        setContentView(layout)
    }
}
