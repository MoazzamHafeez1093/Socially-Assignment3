package com.example.assignment1.agora

/**
 * Event handler interface for Agora callbacks
 * Implement this to handle call events in your Activity
 */
interface AgoraEventHandler {
    /**
     * Called when successfully joined a channel
     */
    fun onJoinChannelSuccess(channel: String?, uid: Int)
    
    /**
     * Called when a remote user joins the channel
     */
    fun onUserJoined(uid: Int)
    
    /**
     * Called when a remote user leaves the channel
     */
    fun onUserOffline(uid: Int, reason: Int)
    
    /**
     * Called when leaving the channel
     */
    fun onLeaveChannel()
    
    /**
     * Called when an error occurs
     */
    fun onError(error: Int)
    
    /**
     * Called when remote video state changes
     */
    fun onRemoteVideoStateChanged(uid: Int, state: Int)
}
