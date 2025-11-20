package com.example.assignment1

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.net.Uri
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.assignment1.utils.Base64Image
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class storyViewOwn : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance()
    private var currentStoryIndex = 0
    private val myStories = mutableListOf<Map<String, Any>>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_story_view_own)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    // Load user's stories from Firebase (realtime listener)
    listenMyStoriesRealtime()
        
        // Close on click
        val intentClose = findViewById<ConstraintLayout>(R.id.main)
        intentClose.setOnClickListener {
            finish()
            overridePendingTransition(0, 0)
        }
    }
    
    private fun listenMyStoriesRealtime() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        val currentTime = System.currentTimeMillis()
        database.reference.child("stories")
            .orderByChild("userId")
            .equalTo(currentUserId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    myStories.clear()
                    for (storySnapshot in snapshot.children) {
                        val expiresAt = storySnapshot.child("expiresAt").getValue(Long::class.java) ?: 0
                        if (expiresAt > currentTime) {
                            val storyData = storySnapshot.value as? Map<String, Any>
                            if (storyData != null) {
                                myStories.add(storyData)
                            }
                        }
                    }
                    // Sort by timestamp ascending so newest are last
                    myStories.sortBy { (it["timestamp"] as? Long) ?: 0L }
                    
                    if (myStories.isNotEmpty()) {
                        currentStoryIndex = 0
                        displayStory(currentStoryIndex)
                    } else {
                        Toast.makeText(this@storyViewOwn, "No active stories", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@storyViewOwn, "Failed to load stories", Toast.LENGTH_SHORT).show()
                    finish()
                }
            })
    }
    
    private fun displayStory(index: Int) {
        if (index >= myStories.size) {
            finish()
            return
        }
        
    val story = myStories[index]
        val storyImageView = findViewById<ImageView>(R.id.story_image)
        val profileImageView = findViewById<ImageView>(R.id.profile_image)
        val usernameTextView = findViewById<TextView>(R.id.username)
        
        // Display story image from Base64
        val imageBase64 = story["imageBase64"] as? String
        if (!imageBase64.isNullOrEmpty()) {
            try {
                val bitmap = Base64Image.base64ToBitmap(imageBase64)
                storyImageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                Toast.makeText(this, "Error loading story image", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Display profile image from Base64
        val profileImageBase64 = story["userProfileImageBase64"] as? String
        if (!profileImageBase64.isNullOrEmpty()) {
            try {
                val bitmap = Base64Image.base64ToBitmap(profileImageBase64)
                profileImageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                // Keep default profile image
            }
        }
        
        // Display username and time
        val username = story["username"] as? String ?: "You"
        val timestamp = story["timestamp"] as? Long ?: System.currentTimeMillis()
        val timeAgo = getTimeAgo(timestamp)
        usernameTextView.text = "$username • $timeAgo"

        // Tap to go next story
        val root = findViewById<ConstraintLayout>(R.id.main)
        root.setOnClickListener {
            currentStoryIndex += 1
            if (currentStoryIndex >= myStories.size) {
                finish()
            } else {
                displayStory(currentStoryIndex)
            }
        }
    }
    
    private fun getTimeAgo(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        val hours = diff / (1000 * 60 * 60)
        return if (hours < 1) "Just now" else "${hours}h"
    }
}