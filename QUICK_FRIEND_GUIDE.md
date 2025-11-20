# Quick Reference - 4 Commits for Friend

## COMMIT 1: Fix PresenceManager

**File:** `app/src/main/java/com/example/assignment1/utils/PresenceManager.kt`

**Replace entire file with:**
```kotlin
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
                apiService.updatePresence(mapOf(
                    "isOnline" to true,
                    "lastSeen" to System.currentTimeMillis()
                ))
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
                apiService.updatePresence(mapOf(
                    "isOnline" to false,
                    "lastSeen" to System.currentTimeMillis()
                ))
            } catch (e: Exception) {
                // Silently fail - not critical
            }
        }
    }
}
```

**Commit:**
```bash
git add app/src/main/java/com/example/assignment1/utils/PresenceManager.kt
git commit -m "Fix: Migrate PresenceManager from Firebase to REST API"
git push origin main
```

---

## COMMIT 2: Fix ScreenshotDetector

**File:** `app/src/main/java/com/example/assignment1/utils/ScreenshotDetector.kt`

**Replace entire file with:**
```kotlin
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
                    notifyScreenshotTaken(currentUser, chatPartnerId)
                }
            }
        }

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
                
                apiService.sendFcmNotification(mapOf(
                    "userId" to chatPartnerId,
                    "title" to "Screenshot Alert",
                    "body" to "${currentUser.username} took a screenshot of your chat",
                    "data" to mapOf(
                        "type" to "screenshot_alert",
                        "fromUserId" to currentUser.userId,
                        "fromUsername" to currentUser.username
                    )
                ))
            } catch (e: Exception) {
                // Silently fail
            }
        }
    }
}
```

**Commit:**
```bash
git add app/src/main/java/com/example/assignment1/utils/ScreenshotDetector.kt
git commit -m "Fix: Migrate ScreenshotDetector from Firebase to REST API"
git push origin main
```

---

## COMMIT 3: Delete FirebaseAuthManager

**Command:**
```bash
git rm app/src/main/java/com/example/assignment1/utils/FirebaseAuthManager.kt
git commit -m "Remove: Delete FirebaseAuthManager (migrated to REST API)"
git push origin main
```

---

## COMMIT 4: Update IP Address

**File:** `app/src/main/java/com/example/assignment1/data/network/ApiClient.kt`

**Find line 15 (approximately):**
```kotlin
private const val BASE_URL = "http://192.168.1.9/socially-api/public/"
```

**Change to YOUR IP address:**
1. Run `ipconfig` in Command Prompt
2. Find "IPv4 Address" (e.g., `192.168.1.25`)
3. Replace:
```kotlin
private const val BASE_URL = "http://192.168.1.25/socially-api/public/"
```

**Commit:**
```bash
git add app/src/main/java/com/example/assignment1/data/network/ApiClient.kt
git commit -m "Config: Update API base URL for local network (192.168.1.XX)"
git push origin main
```

---

## After All 4 Commits

1. Open Android Studio
2. **File → Sync Project with Gradle Files**
3. **Build → Clean Project**
4. **Build → Rebuild Project**
5. ✅ Should build successfully!

---

## Then YOU Pull These Changes

On your PC:
```bash
cd D:\Project_Source\Socially-Assignment3
git pull origin main
```

Then sync Gradle and continue working!
