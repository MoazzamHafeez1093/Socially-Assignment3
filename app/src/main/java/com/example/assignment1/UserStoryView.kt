package com.example.assignment1

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.net.Uri
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.content.Intent

class UserStoryView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_story_view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Retrieve the profile image URI from the intent
        val imageUriString = intent.getStringExtra("PROFILE_IMAGE_URI")
        val profileImageView = findViewById<ImageView>(R.id.profile_image)
        if (imageUriString != null) {
            val imageUri = Uri.parse(imageUriString)
            profileImageView.setImageURI(imageUri)
        }
        val intentClose = findViewById<Button>(R.id.storyEnd)
        intentClose.setOnClickListener {
            finish()
            overridePendingTransition(0, 0)
        }

        val intentUserProfile = findViewById<ImageView>(R.id.profile_image)
        intentUserProfile.setOnClickListener {
            val intentProfile = Intent(this, UserProfile::class.java)
            startActivity(intentProfile)
            finish()
            overridePendingTransition(0, 0)
        }
    }
}