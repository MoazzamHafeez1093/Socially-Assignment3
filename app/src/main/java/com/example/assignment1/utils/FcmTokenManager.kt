package com.example.assignment1.utils

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Manages FCM token registration and updates
 */
object FcmTokenManager {
    
    private const val TAG = "FcmTokenManager"
    
    /**
     * Request and save the current FCM token for the logged-in user
     * Call this after successful login/signup
     */
    fun registerFcmToken() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            Log.w(TAG, "Cannot register FCM token: user not logged in")
            return
        }
        
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                Log.d(TAG, "FCM Token retrieved: $token")
                saveFcmToken(currentUserId, token)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to get FCM token", e)
            }
    }
    
    /**
     * Save FCM token to Firebase Database under user profile
     */
    private fun saveFcmToken(userId: String, token: String) {
        val database = FirebaseDatabase.getInstance()
        database.reference.child("users").child(userId).child("fcmToken")
            .setValue(token)
            .addOnSuccessListener {
                Log.d(TAG, "FCM token saved successfully for user: $userId")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to save FCM token", e)
            }
    }
    
    /**
     * Remove FCM token on logout
     */
    fun clearFcmToken() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        
        val database = FirebaseDatabase.getInstance()
        database.reference.child("users").child(currentUserId).child("fcmToken")
            .removeValue()
            .addOnSuccessListener {
                Log.d(TAG, "FCM token cleared successfully")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to clear FCM token", e)
            }
    }
}
