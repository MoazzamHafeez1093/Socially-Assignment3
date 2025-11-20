package com.example.assignment1.utils

import android.content.Context
import com.example.assignment1.data.network.ApiClient
import com.example.assignment1.data.prefs.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object PresenceManager {

    fun setOnline(context: Context) {
        val sessionManager = SessionManager(context)
        val userId = sessionManager.getUserId() ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = ApiClient.getApiService(context)
                apiService.updatePresence(
                    mapOf(
                        "isOnline" to true,
                        "lastSeen" to System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                // Silently fail - not critical
            }
        }
    }

    fun setOffline(context: Context) {
        val sessionManager = SessionManager(context)
        val userId = sessionManager.getUserId() ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = ApiClient.getApiService(context)
                apiService.updatePresence(
                    mapOf(
                        "isOnline" to false,
                        "lastSeen" to System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                // Silently fail - not critical
            }
        }
    }
}


