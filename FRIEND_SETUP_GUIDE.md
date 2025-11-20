# Friend Setup Guide - Socially Assignment 3

## Prerequisites
- Android Studio installed
- Git installed
- XAMPP with PHP 8.x running
- MySQL database running

---

## STEP 1: Clone the Repository and Setup Backend

### 1.1 Clone from GitHub
```bash
git clone https://github.com/MoazzamHafeez1093/Socially-Assignment3.git
cd Socially-Assignment3
```

### 1.2 Setup Backend API
```bash
# Navigate to your XAMPP htdocs folder
cd C:\xampp\htdocs

# Clone the backend (if not already there)
# Copy the backend from the repo or clone separately
```

### 1.3 Create Database
Open phpMyAdmin (http://localhost/phpmyadmin) and:
1. Create database: `assignment_smd`
2. Import from: `Socially-Assignment3/backend/database/migrations/001_create_tables.sql` (if you have it)
3. Or manually create tables as needed

### 1.4 Test Backend
1. Navigate to: `http://localhost/socially-api/public/health`
2. Should see: `{"status":"success","message":"API is running"}`

---

## STEP 2: Fix PresenceManager (COMMIT 1)

### File: `app/src/main/java/com/example/assignment1/utils/PresenceManager.kt`

**DELETE the entire file content and replace with:**

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

### Commit Command:
```bash
git add app/src/main/java/com/example/assignment1/utils/PresenceManager.kt
git commit -m "Fix: Migrate PresenceManager from Firebase to REST API

- Replaced FirebaseDatabase with ApiClient
- Now calls /presence/update endpoint
- Uses SessionManager for user ID instead of FirebaseAuth
- Added coroutines for async API calls"
git push origin main
```

---

## STEP 3: Fix ScreenshotDetector (COMMIT 2)

### File: `app/src/main/java/com/example/assignment1/utils/ScreenshotDetector.kt`

**DELETE the entire file content and replace with:**

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
                // Silently fail - not critical
            }
        }
    }
}
```

### Commit Command:
```bash
git add app/src/main/java/com/example/assignment1/utils/ScreenshotDetector.kt
git commit -m "Fix: Migrate ScreenshotDetector from Firebase to REST API

- Replaced FirebaseDatabase with ApiClient
- Now calls /fcm/send endpoint for notifications
- Uses REST API instead of Firebase Realtime Database
- Added coroutines for async API calls"
git push origin main
```

---

## STEP 4: Delete FirebaseAuthManager (COMMIT 3)

This file is no longer needed since we migrated to REST API.

### Command:
```bash
git rm app/src/main/java/com/example/assignment1/utils/FirebaseAuthManager.kt
git commit -m "Remove: Delete FirebaseAuthManager (migrated to REST API)

- FirebaseAuthManager no longer needed
- Auth now handled by ApiClient + SessionManager
- LoginActivity and signup.kt already migrated"
git push origin main
```

---

## STEP 5: Update IP Address for Your Network (COMMIT 4)

### 5.1 Find Your Local IP
Open Command Prompt and run:
```bash
ipconfig
```
Look for "IPv4 Address" under your WiFi adapter (e.g., `192.168.1.XX`)

### 5.2 Update ApiClient

**File: `app/src/main/java/com/example/assignment1/data/network/ApiClient.kt`**

Find this line (around line 15):
```kotlin
private const val BASE_URL = "http://192.168.1.9/socially-api/public/"
```

Replace `192.168.1.9` with YOUR IP address from step 5.1:
```kotlin
private const val BASE_URL = "http://192.168.1.XX/socially-api/public/"
```

### 5.3 Commit Command:
```bash
git add app/src/main/java/com/example/assignment1/data/network/ApiClient.kt
git commit -m "Config: Update API base URL for local network

- Changed IP from 192.168.1.9 to 192.168.1.XX
- Update for local testing environment"
git push origin main
```

---

## STEP 6: Sync and Build

1. Open Android Studio
2. Click **File → Sync Project with Gradle Files**
3. Wait for sync to complete
4. Click **Build → Clean Project**
5. Click **Build → Rebuild Project**
6. Should build successfully!

---

## VERIFICATION

### Backend Check:
```bash
# Test health endpoint
curl http://localhost/socially-api/public/health

# Should return:
{"status":"success","message":"API is running"}
```

### Android Check:
1. Build should complete with 0 errors
2. Run on physical device (make sure phone is on same WiFi)
3. Try signup with new account
4. Try login with existing account

---

## TROUBLESHOOTING

### Build Errors?
- Make sure you did all 4 commits
- Sync Gradle again
- Clean + Rebuild

### Can't Connect to API?
- Check phone is on same WiFi as PC
- Check XAMPP Apache is running
- Test backend health endpoint in phone browser
- Make sure IP address in ApiClient matches your PC's IP

### Database Errors?
- Make sure `assignment_smd` database exists
- Check `.env` file has correct database credentials
- Test backend endpoints in Postman

---

## SUMMARY OF 4 COMMITS

✅ **Commit 1:** Fix PresenceManager - migrate to REST API  
✅ **Commit 2:** Fix ScreenshotDetector - migrate to REST API  
✅ **Commit 3:** Delete FirebaseAuthManager - no longer needed  
✅ **Commit 4:** Update API base URL - for your local network  

After these 4 commits, the project should build successfully!
