package com.example.assignment1

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.text.InputType
import android.widget.ImageButton
import com.example.assignment1.utils.Base64Image
import com.example.assignment1.utils.FirebaseAuthManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class signup : AppCompatActivity() {

    private var selectedImageUri: Uri? = null
    private lateinit var authManager: FirebaseAuthManager
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private lateinit var cameraButton: ImageButton

    // Launcher for image picker
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            // Update camera button with selected image
            try {
                cameraButton.setImageURI(it)
                cameraButton.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
            } catch (e: Exception) {
                Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            authManager = FirebaseAuthManager()
        } catch (e: Exception) {
            Toast.makeText(this, "Firebase initialization failed", Toast.LENGTH_SHORT).show()
        }

        try {
            setContentView(R.layout.activity_signup)
            setupSignupUI()
        } catch (e: Exception) {
            // If layout fails, create programmatic view
            createProgrammaticSignupScreen()
        }
    }

    private fun setupSignupUI() {
        try {
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        } catch (e: Exception) {
            // Continue without window insets
        }

        // Use the correct IDs from the layout file
        val usernameInput = findViewById<EditText?>(R.id.userName1)
        val emailInput = findViewById<EditText?>(R.id.emailEditText)
        val passwordInput = findViewById<EditText?>(R.id.passwordEditText)
        val createButton = findViewById<Button?>(R.id.createAccountBtn)
        cameraButton = findViewById<ImageButton?>(R.id.cameraButton) ?: run {
            createProgrammaticSignupScreen()
            return
        }
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar?>(R.id.toolbar)

        // If any required element is missing, use programmatic layout
        if (usernameInput == null || emailInput == null || passwordInput == null || createButton == null) {
            createProgrammaticSignupScreen()
            return
        }

        // Camera button opens gallery
        cameraButton.setOnClickListener {
            pickImage.launch("image/*")
        }
        
        // Toolbar back button
        toolbar?.setNavigationOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        createButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val username = usernameInput.text.toString().trim()

            if (username.isEmpty()) {
                Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty() || password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Sign up with Firebase
            authManager.signUp(email, password, username, this) { success, message ->
                if (success) {
                    // Save profile image as Base64 if selected
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if (currentUser != null && selectedImageUri != null) {
                        val base64Image = Base64Image.uriToBase64(this, selectedImageUri!!, 70)
                        if (base64Image != null) {
                            database.reference.child("users").child(currentUser.uid)
                                .child("profileImageBase64").setValue(base64Image)
                        }
                    }
                    
                    Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                    
                    // Navigate to home screen
                    val intent = Intent(this, HomeScreen::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, message ?: "Signup failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun createProgrammaticSignupScreen() {
        val scrollView = android.widget.ScrollView(this).apply {
            setBackgroundColor(android.graphics.Color.parseColor("#784A34"))
        }
        
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setPadding(50, 100, 50, 50)
        }
        
        val logo = android.widget.TextView(this).apply {
            text = "Socially"
            textSize = 48f
            setTextColor(android.graphics.Color.WHITE)
            gravity = android.view.Gravity.CENTER
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 30
            }
        }
        
        val subtitle = android.widget.TextView(this).apply {
            text = "Create An Account and Sign Up"
            textSize = 18f
            setTextColor(android.graphics.Color.WHITE)
            gravity = android.view.Gravity.CENTER
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 20
            }
        }
        
        cameraButton = ImageButton(this).apply {
            setImageResource(R.drawable.camera)
            layoutParams = android.widget.LinearLayout.LayoutParams(200, 200).apply {
                bottomMargin = 30
                gravity = android.view.Gravity.CENTER_HORIZONTAL
            }
            setBackgroundColor(android.graphics.Color.parseColor("#A0784A34"))
            scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
            setOnClickListener {
                pickImage.launch("image/*")
            }
        }
        
        val usernameInput = EditText(this).apply {
            hint = "Username"
            setTextColor(android.graphics.Color.WHITE)
            setHintTextColor(android.graphics.Color.parseColor("#CCFFFFFF"))
            setPadding(20, 20, 20, 20)
            setBackgroundColor(android.graphics.Color.parseColor("#A0FFFFFF"))
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 20
            }
        }
        
        val emailInput = EditText(this).apply {
            hint = "Email"
            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            setTextColor(android.graphics.Color.WHITE)
            setHintTextColor(android.graphics.Color.parseColor("#CCFFFFFF"))
            setPadding(20, 20, 20, 20)
            setBackgroundColor(android.graphics.Color.parseColor("#A0FFFFFF"))
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 20
            }
        }
        
        val passwordInput = EditText(this).apply {
            hint = "Password (min 6 characters)"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            setTextColor(android.graphics.Color.WHITE)
            setHintTextColor(android.graphics.Color.parseColor("#CCFFFFFF"))
            setPadding(20, 20, 20, 20)
            setBackgroundColor(android.graphics.Color.parseColor("#A0FFFFFF"))
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 30
            }
        }
        
        val createButton = android.widget.Button(this).apply {
            text = "Create Account"
            setBackgroundColor(android.graphics.Color.WHITE)
            setTextColor(android.graphics.Color.parseColor("#784A34"))
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 20
            }
            setOnClickListener {
                val email = emailInput.text.toString().trim()
                val password = passwordInput.text.toString().trim()
                val username = usernameInput.text.toString().trim()

                if (username.isEmpty()) {
                    Toast.makeText(this@signup, "Please enter a username", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (email.isEmpty()) {
                    Toast.makeText(this@signup, "Please enter an email", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (password.isEmpty() || password.length < 6) {
                    Toast.makeText(this@signup, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                authManager.signUp(email, password, username, this@signup) { success, message ->
                    if (success) {
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        if (currentUser != null && selectedImageUri != null) {
                            val base64Image = Base64Image.uriToBase64(this@signup, selectedImageUri!!, 70)
                            if (base64Image != null) {
                                database.reference.child("users").child(currentUser.uid)
                                    .child("profileImageBase64").setValue(base64Image)
                            }
                        }
                        
                        Toast.makeText(this@signup, "Account created successfully!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@signup, HomeScreen::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                    } else {
                        Toast.makeText(this@signup, message ?: "Signup failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        
        layout.addView(logo)
        layout.addView(subtitle)
        layout.addView(cameraButton)
        layout.addView(usernameInput)
        layout.addView(emailInput)
        layout.addView(passwordInput)
        layout.addView(createButton)
        scrollView.addView(layout)
        setContentView(scrollView)
    }
}
