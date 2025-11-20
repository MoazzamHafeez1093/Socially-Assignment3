package com.example.assignment1

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.assignment1.agora.AgoraEventHandler
import com.example.assignment1.agora.AgoraManager

class CallActivity : AppCompatActivity(), AgoraEventHandler {
    
    private lateinit var localVideoContainer: FrameLayout
    private lateinit var remoteVideoContainer: FrameLayout
    private lateinit var callStatusText: TextView
    private lateinit var endCallBtn: ImageButton
    
    private var agoraManager: AgoraManager? = null
    private var channelName: String = ""
    private var callType: String = "voice"
    private var isIncomingCall: Boolean = false
    private var isMuted: Boolean = false
    private var isVideoEnabled: Boolean = true
    private var isSpeakerOn: Boolean = true
    private var remoteUid: Int = 0
    
    private val PERMISSION_REQ_ID = 22
    private val REQUESTED_PERMISSIONS = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_call)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Get intent extras
        channelName = intent.getStringExtra("channelName") ?: "default_channel"
        callType = intent.getStringExtra("callType") ?: "voice"
        isIncomingCall = intent.getBooleanExtra("isIncomingCall", false)
        
        initializeViews()
        
        // Check permissions first
        if (checkSelfPermission()) {
            initializeAgoraAndJoinChannel()
        } else {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID)
        }
    }
    
    private fun initializeViews() {
        // Find views from layout
        callStatusText = findViewById(R.id.callStatusText)
        endCallBtn = findViewById(R.id.callEnd)
        
        // Find video containers from layout
        localVideoContainer = findViewById(R.id.localVideoContainer)
        remoteVideoContainer = findViewById(R.id.remoteVideoContainer)
        
        // Set visibility based on call type
        if (callType == "video") {
            localVideoContainer.visibility = View.VISIBLE
            remoteVideoContainer.visibility = View.VISIBLE
        } else {
            localVideoContainer.visibility = View.GONE
            remoteVideoContainer.visibility = View.GONE
        }
        
        setupClickListeners()
        
        // Set initial status
        callStatusText.text = if (isIncomingCall) "Connecting..." else "Calling..."
    }
    
    private fun setupClickListeners() {
        endCallBtn.setOnClickListener {
            endCall()
        }
        
        // Setup control button listeners
        findViewById<ImageButton?>(R.id.muteBtn)?.setOnClickListener {
            toggleMute()
        }
        
        findViewById<ImageButton?>(R.id.videoBtn)?.setOnClickListener {
            toggleVideo()
        }
        
        findViewById<ImageButton?>(R.id.switchCameraBtn)?.setOnClickListener {
            agoraManager?.switchCamera()
            Toast.makeText(this, "Camera switched", Toast.LENGTH_SHORT).show()
        }
        
        findViewById<ImageButton?>(R.id.speakerBtn)?.setOnClickListener {
            toggleSpeaker()
        }
    }
    
    private fun checkSelfPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[1]) == PackageManager.PERMISSION_GRANTED
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQ_ID) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeAgoraAndJoinChannel()
            } else {
                Toast.makeText(this, "Permissions required for call", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    
    private fun initializeAgoraAndJoinChannel() {
        // Initialize Agora
        agoraManager = AgoraManager(this, this)
        
        if (!agoraManager!!.initialize()) {
            Toast.makeText(this, "Failed to initialize Agora", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Setup video if video call
        if (callType == "video") {
            agoraManager?.setupLocalVideo(localVideoContainer)
            isVideoEnabled = true
        } else {
            isVideoEnabled = false
        }
        
        // Join channel
        val success = agoraManager?.joinChannel(channelName, callType == "video")
        if (success == false) {
            Toast.makeText(this, "Failed to join call", Toast.LENGTH_SHORT).show()
            finish()
        }
        
        // Enable speaker by default for video calls
        isSpeakerOn = callType == "video"
        agoraManager?.setSpeakerphoneOn(isSpeakerOn)
    }
    
    // Agora event callbacks
    override fun onJoinChannelSuccess(channel: String?, uid: Int) {
        runOnUiThread {
            callStatusText.text = "Connected"
        }
    }
    
    override fun onUserJoined(uid: Int) {
        runOnUiThread {
            remoteUid = uid
            callStatusText.text = "Call in progress"
            
            // Setup remote video if video call
            if (callType == "video") {
                agoraManager?.setupRemoteVideo(remoteVideoContainer, uid)
            }
        }
    }
    
    override fun onUserOffline(uid: Int, reason: Int) {
        runOnUiThread {
            if (uid == remoteUid) {
                callStatusText.text = "Call ended"
                Toast.makeText(this, "Other user left the call", Toast.LENGTH_SHORT).show()
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    finish()
                }, 1500)
            }
        }
    }
    
    override fun onLeaveChannel() {
        runOnUiThread {
            callStatusText.text = "Call ended"
        }
    }
    
    override fun onError(error: Int) {
        runOnUiThread {
            Toast.makeText(this, "Call error: $error", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onRemoteVideoStateChanged(uid: Int, state: Int) {
        runOnUiThread {
            // Handle remote video state changes if needed
        }
    }
    
    private fun endCall() {
        agoraManager?.leaveChannel()
        finish()
    }
    
    private fun toggleMute() {
        isMuted = !isMuted
        agoraManager?.muteLocalAudio(isMuted)
        // muteBtn.setImageResource(if (isMuted) R.drawable.mic_off else R.drawable.mic_on)
        Toast.makeText(this, if (isMuted) "Muted" else "Unmuted", Toast.LENGTH_SHORT).show()
    }
    
    private fun toggleVideo() {
        if (callType == "video") {
            isVideoEnabled = !isVideoEnabled
            agoraManager?.enableLocalVideo(isVideoEnabled)
            localVideoContainer.visibility = if (isVideoEnabled) View.VISIBLE else View.GONE
            // videoBtn.setImageResource(if (isVideoEnabled) R.drawable.video_on else R.drawable.video_off)
            Toast.makeText(this, if (isVideoEnabled) "Video on" else "Video off", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun toggleSpeaker() {
        isSpeakerOn = !isSpeakerOn
        agoraManager?.setSpeakerphoneOn(isSpeakerOn)
        // speakerBtn.setImageResource(if (isSpeakerOn) R.drawable.speaker_on else R.drawable.speaker_off)
        Toast.makeText(this, if (isSpeakerOn) "Speaker on" else "Speaker off", Toast.LENGTH_SHORT).show()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        agoraManager?.destroy()
    }
}