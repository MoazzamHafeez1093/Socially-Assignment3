package com.example.assignment1

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class call : AppCompatActivity() {
    
    private lateinit var callStatusText: TextView
    
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference
    private var receiverUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_call)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get receiver user ID from intent (if calling from chat or profile)
        receiverUserId = intent.getStringExtra("userId")

        initializeViews()
        
        // Auto-start video call (you can customize this behavior)
        startCall("video")
    }

    private fun initializeViews() {
        callStatusText = findViewById(R.id.callStatusText)
    }

    private fun startCall(callType: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        val channelName = generateChannelName(currentUserId)
        
        // Send call invitation via Firebase if we have receiver ID
        receiverUserId?.let { receiverId ->
            sendCallInvitation(receiverId, callType, channelName)
        }
        
        // Start call activity
        val intent = Intent(this, CallActivity::class.java).apply {
            putExtra("channelName", channelName)
            putExtra("callType", callType)
            putExtra("isIncomingCall", false)
            putExtra("receiverId", receiverUserId)
        }
        startActivity(intent)
    }
    
    private fun sendCallInvitation(receiverId: String, callType: String, channelName: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        
        val invitationData = mapOf(
            "callerId" to currentUserId,
            "callType" to callType,
            "channelName" to channelName,
            "timestamp" to System.currentTimeMillis()
        )
        
        // Write to Firebase - Cloud Function will send push notification
        database.child("callInvitations")
            .child(receiverId)
            .push()
            .setValue(invitationData)
            .addOnSuccessListener {
                Toast.makeText(this, "Calling...", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to send invitation: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun generateChannelName(userId: String): String {
        val timestamp = System.currentTimeMillis()
        return "socially_${userId}_${timestamp}"
    }
}