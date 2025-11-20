package com.example.assignment1.utils

import android.app.Activity
import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.example.assignment1.data.models.User
import com.example.assignment1.data.network.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScreenshotDetector(private val activity: Activity) {
    private val contentResolver: ContentResolver = activity.contentResolver
    private var contentObserver: ContentObserver? = null

    fun startDetection(currentUser: User, chatPartnerId: String) {
        contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                if (uri != null && uri.toString().contains("screenshot")) {
                    // Screenshot detected
                    notifyScreenshotTaken(currentUser, chatPartnerId)
                }
            }
        }

        // Monitor media store for screenshots
        contentResolver.registerContentObserver(
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            contentObserver!!
        )
    }

    fun stopDetection() {
        contentObserver?.let {
            contentResolver.unregisterContentObserver(it)
            contentObserver = null
        }
    }

    private fun notifyScreenshotTaken(currentUser: User, chatPartnerId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = ApiClient.getApiService(activity)

                // Send screenshot notification via FCM
                apiService.sendFcmNotification(
                    mapOf(
                        "userId" to chatPartnerId,
                        "title" to "Screenshot Alert",
                        "body" to "${currentUser.username} took a screenshot of your chat",
                        "data" to mapOf(
                            "type" to "screenshot_alert",
                            "fromUserId" to currentUser.userId,
                            "fromUsername" to currentUser.username
                        )
                    )
                )
            } catch (e: Exception) {
                // Silently fail - not critical
            }
        }
    }
}
