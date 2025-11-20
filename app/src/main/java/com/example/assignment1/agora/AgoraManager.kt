package com.example.assignment1.agora

import android.content.Context
import android.util.Log
import android.view.SurfaceView
import android.widget.FrameLayout
import com.example.assignment1.BuildConfig
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoCanvas

/**
 * Manager class for Agora RTC Engine
 * Handles all Agora-related functionality: joining/leaving calls, video, audio controls
 */
class AgoraManager(
    private val context: Context,
    private val eventHandler: AgoraEventHandler
) {
    private var rtcEngine: RtcEngine? = null
    private var isInitialized = false
    private var localVideoView: SurfaceView? = null
    private var remoteVideoView: SurfaceView? = null
    
    companion object {
        private const val TAG = "AgoraManager"
    }

    /**
     * Initialize Agora RTC Engine
     */
    fun initialize(): Boolean {
        if (isInitialized) {
            Log.w(TAG, "Agora already initialized")
            return true
        }

        try {
            val appId = BuildConfig.AGORA_APP_ID
            if (appId.isEmpty()) {
                Log.e(TAG, "Agora App ID is empty! Check local.properties")
                return false
            }

            val config = RtcEngineConfig().apply {
                mContext = context
                mAppId = appId
                mEventHandler = object : IRtcEngineEventHandler() {
                    override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                        Log.d(TAG, "Joined channel: $channel with uid: $uid")
                        eventHandler.onJoinChannelSuccess(channel, uid)
                    }

                    override fun onUserJoined(uid: Int, elapsed: Int) {
                        Log.d(TAG, "Remote user joined: $uid")
                        eventHandler.onUserJoined(uid)
                    }

                    override fun onUserOffline(uid: Int, reason: Int) {
                        Log.d(TAG, "Remote user offline: $uid, reason: $reason")
                        eventHandler.onUserOffline(uid, reason)
                    }

                    override fun onLeaveChannel(stats: RtcStats?) {
                        Log.d(TAG, "Left channel")
                        eventHandler.onLeaveChannel()
                    }

                    override fun onError(err: Int) {
                        Log.e(TAG, "Agora error: $err")
                        eventHandler.onError(err)
                    }

                    override fun onRemoteVideoStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
                        Log.d(TAG, "Remote video state changed: uid=$uid, state=$state")
                        eventHandler.onRemoteVideoStateChanged(uid, state)
                    }
                }
            }

            rtcEngine = RtcEngine.create(config)
            
            // Enable video module by default
            rtcEngine?.enableVideo()
            
            isInitialized = true
            Log.d(TAG, "Agora initialized successfully with App ID: ${appId.take(8)}...")
            return true
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Agora: ${e.message}", e)
            return false
        }
    }

    /**
     * Join a video/voice call channel
     */
    fun joinChannel(channelName: String, isVideoCall: Boolean = true, token: String = ""): Boolean {
        if (!isInitialized) {
            Log.e(TAG, "Cannot join channel: Agora not initialized")
            return false
        }

        try {
            val options = ChannelMediaOptions().apply {
                channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
                clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
                
                // Enable audio for all calls
                autoSubscribeAudio = true
                publishMicrophoneTrack = true
                
                // Enable video only for video calls
                if (isVideoCall) {
                    autoSubscribeVideo = true
                    publishCameraTrack = true
                } else {
                    autoSubscribeVideo = false
                    publishCameraTrack = false
                }
            }

            // Join the channel (use 0 for auto-assigned uid)
            val result = rtcEngine?.joinChannel(token, channelName, 0, options)
            
            if (result == 0) {
                Log.d(TAG, "Joining channel: $channelName (video: $isVideoCall)")
                return true
            } else {
                Log.e(TAG, "Failed to join channel, error code: $result")
                return false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception joining channel: ${e.message}", e)
            return false
        }
    }

    /**
     * Leave the current channel
     */
    fun leaveChannel() {
        try {
            rtcEngine?.leaveChannel()
            Log.d(TAG, "Left channel")
        } catch (e: Exception) {
            Log.e(TAG, "Error leaving channel: ${e.message}", e)
        }
    }

    /**
     * Setup local video view
     */
    fun setupLocalVideo(container: FrameLayout): SurfaceView? {
        if (!isInitialized) {
            Log.e(TAG, "Cannot setup local video: Agora not initialized")
            return null
        }

        try {
            // Create surface view for local video
            val surfaceView = RtcEngine.CreateRendererView(context)
            surfaceView.setZOrderMediaOverlay(true)
            container.addView(surfaceView)

            // Setup local video canvas
            rtcEngine?.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0))
            
            // Start local preview
            rtcEngine?.startPreview()
            
            localVideoView = surfaceView
            Log.d(TAG, "Local video setup complete")
            return surfaceView
            
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up local video: ${e.message}", e)
            return null
        }
    }

    /**
     * Setup remote video view
     */
    fun setupRemoteVideo(container: FrameLayout, uid: Int): SurfaceView? {
        if (!isInitialized) {
            Log.e(TAG, "Cannot setup remote video: Agora not initialized")
            return null
        }

        try {
            // Create surface view for remote video
            val surfaceView = RtcEngine.CreateRendererView(context)
            container.addView(surfaceView)

            // Setup remote video canvas
            rtcEngine?.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid))
            
            remoteVideoView = surfaceView
            Log.d(TAG, "Remote video setup complete for uid: $uid")
            return surfaceView
            
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up remote video: ${e.message}", e)
            return null
        }
    }

    /**
     * Mute/unmute local audio
     */
    fun muteLocalAudio(muted: Boolean) {
        try {
            rtcEngine?.muteLocalAudioStream(muted)
            Log.d(TAG, "Local audio ${if (muted) "muted" else "unmuted"}")
        } catch (e: Exception) {
            Log.e(TAG, "Error muting audio: ${e.message}", e)
        }
    }

    /**
     * Enable/disable local video
     */
    fun enableLocalVideo(enabled: Boolean) {
        try {
            rtcEngine?.muteLocalVideoStream(!enabled)
            if (enabled) {
                rtcEngine?.startPreview()
            } else {
                rtcEngine?.stopPreview()
            }
            Log.d(TAG, "Local video ${if (enabled) "enabled" else "disabled"}")
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling video: ${e.message}", e)
        }
    }

    /**
     * Switch between front and back camera
     */
    fun switchCamera() {
        try {
            rtcEngine?.switchCamera()
            Log.d(TAG, "Camera switched")
        } catch (e: Exception) {
            Log.e(TAG, "Error switching camera: ${e.message}", e)
        }
    }

    /**
     * Enable/disable speaker
     */
    fun setSpeakerphoneOn(enabled: Boolean) {
        try {
            rtcEngine?.setEnableSpeakerphone(enabled)
            Log.d(TAG, "Speaker ${if (enabled) "on" else "off"}")
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling speaker: ${e.message}", e)
        }
    }

    /**
     * Destroy Agora engine and clean up resources
     */
    fun destroy() {
        try {
            localVideoView = null
            remoteVideoView = null
            rtcEngine?.leaveChannel()
            rtcEngine?.stopPreview()
            RtcEngine.destroy()
            rtcEngine = null
            isInitialized = false
            Log.d(TAG, "Agora destroyed")
        } catch (e: Exception) {
            Log.e(TAG, "Error destroying Agora: ${e.message}", e)
        }
    }
}
