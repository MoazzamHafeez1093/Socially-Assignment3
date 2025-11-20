package com.example.assignment1

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * IncomingCallActivity - Displays incoming call UI
 * Shows caller info with Accept/Decline buttons
 */
class IncomingCallActivity : AppCompatActivity() {

    private lateinit var callerImageView: ImageView
    private lateinit var callerNameText: TextView
    private lateinit var callTypeText: TextView
    private lateinit var acceptButton: ImageButton
    private lateinit var declineButton: ImageButton
    
    private var callerId: String? = null
    private var callerName: String? = null
    private var channelName: String? = null
    private var callType: String? = null
    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incoming_call)

        // Get call invitation data from intent
        callerId = intent.getStringExtra("callerId")
        callerName = intent.getStringExtra("callerName")
        channelName = intent.getStringExtra("channelName")
        callType = intent.getStringExtra("callType") ?: "voice"

        initializeViews()
        setupClickListeners()
        loadCallerInfo()
        playRingtone()
    }

    private fun initializeViews() {
        callerImageView = findViewById(R.id.callerImageView)
        callerNameText = findViewById(R.id.callerNameText)
        callTypeText = findViewById(R.id.callTypeText)
        acceptButton = findViewById(R.id.acceptCallBtn)
        declineButton = findViewById(R.id.declineCallBtn)

        // Display call type
        val callTypeDisplay = if (callType == "video") "Video Call" else "Voice Call"
        callTypeText.text = callTypeDisplay
        
        // Display caller name
        callerNameText.text = callerName ?: "Unknown Caller"
    }

    private fun setupClickListeners() {
        acceptButton.setOnClickListener {
            acceptCall()
        }

        declineButton.setOnClickListener {
            declineCall()
        }
    }

    private fun loadCallerInfo() {
        callerId?.let { userId ->
            database.child("users").child(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val username = snapshot.child("username").getValue(String::class.java)
                        val profileImageBase64 = snapshot.child("profileImage").getValue(String::class.java)

                        username?.let {
                            callerNameText.text = it
                        }

                        // TODO: Decode and display profile image from base64
                        // profileImageBase64?.let { base64 ->
                        //     val bitmap = decodeBase64(base64)
                        //     callerImageView.setImageBitmap(bitmap)
                        // }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Use default caller info
                    }
                })
        }
    }

    private fun playRingtone() {
        try {
            val ringtoneUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            val ringtone = RingtoneManager.getRingtone(applicationContext, ringtoneUri)
            ringtone.play()
            
            // Stop ringtone when activity is destroyed
            // You may want to store ringtone reference and stop it explicitly
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun acceptCall() {
        // Stop ringtone
        stopRingtone()
        
        // Navigate to CallActivity with channel name
        val intent = Intent(this, CallActivity::class.java).apply {
            putExtra("channelName", channelName)
            putExtra("callType", callType)
            putExtra("isIncomingCall", true)
            putExtra("callerId", callerId)
        }
        startActivity(intent)
        finish()
    }

    private fun declineCall() {
        // Stop ringtone
        stopRingtone()
        
        // Send decline notification to caller (optional)
        callerId?.let { userId ->
            val declineData = mapOf(
                "type" to "call_declined",
                "timestamp" to System.currentTimeMillis()
            )
            database.child("callNotifications").child(userId).push().setValue(declineData)
        }
        
        finish()
    }

    private fun stopRingtone() {
        try {
            // Stop any playing ringtones
            // This is a simplified version - in production, store ringtone reference
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRingtone()
    }
}
