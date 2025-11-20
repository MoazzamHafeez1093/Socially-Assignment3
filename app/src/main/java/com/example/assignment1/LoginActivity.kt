package com.example.assignment1

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.assignment1.data.network.ApiClient
import com.example.assignment1.data.prefs.SessionManager
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        
        // Handle status bar and navigation bar padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize session manager
        sessionManager = SessionManager(this)

        setupLoginButton()
        setupSignupButton()
        setupForgotPasswordButton()

        // Ask for notifications permission on Android 13+
        requestNotificationPermissionIfNeeded()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            val granted = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                ActivityCompat.requestPermissions(this, arrayOf(permission), 1001)
            }
        }
    }
    
    private fun setupLoginButton() {
        val emailTextBox = findViewById<TextInputEditText>(R.id.emailTextBox)
        val passwordTextBox = findViewById<TextInputEditText>(R.id.passwordTextBox)
        val loginButton = findViewById<Button>(R.id.btnLogin2)
        
        loginButton?.setOnClickListener {
            val email = emailTextBox?.text.toString().trim()
            val password = passwordTextBox?.text.toString().trim()
            
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (password.isEmpty()) {
                Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Disable button during login
            loginButton.isEnabled = false
            
            // Call API to login
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val apiService = ApiClient.getApiService(this@LoginActivity)
                    val request = mapOf(
                        "email" to email,
                        "password" to password
                    )
                    
                    val response = apiService.login(request)
                    
                    withContext(Dispatchers.Main) {
                        if (response.status == "success" && response.data != null) {
                            // Save auth data to session
                            sessionManager.saveAuthData(
                                token = response.data.token,
                                user = response.data.user
                            )
                            
                            Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()
                            
                            // Navigate to home screen
                            val intent = Intent(this@LoginActivity, HomeScreen::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@LoginActivity, response.message ?: "Login failed", Toast.LENGTH_SHORT).show()
                            loginButton.isEnabled = true
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        loginButton.isEnabled = true
                    }
                }
            }
        }
    }
    
    private fun setupSignupButton() {
        val signupButton = findViewById<Button>(R.id.signupBtn)
        signupButton?.setOnClickListener {
            val intent = Intent(this, signup::class.java)
            startActivity(intent)
        }
    }
    
    private fun setupForgotPasswordButton() {
        val forgotPasswordButton = findViewById<Button>(R.id.forgotPassword)
        forgotPasswordButton?.setOnClickListener {
            Toast.makeText(this, "Forgot password functionality coming soon", Toast.LENGTH_SHORT).show()
        }
    }
}