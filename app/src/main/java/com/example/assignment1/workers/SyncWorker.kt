package com.example.assignment1.workers

import android.content.Context
import android.net.Uri
import androidx.work.*
import com.example.assignment1.data.local.AppDatabase
import com.example.assignment1.data.network.ApiClient
import com.example.assignment1.data.prefs.SessionManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker for syncing pending actions when device comes online
 * Processes offline queue for:
 * - Story uploads
 * - Message sends
 * - Post uploads
 * - Post likes
 * - Comments
 * - Follow actions
 */
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val database = AppDatabase.getInstance(context)
    private val sessionManager = SessionManager(context)
    private val gson = Gson()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Get all pending actions
            val pendingActions = database.pendingActionDao().getPendingActions()
            
            if (pendingActions.isEmpty()) {
                return@withContext Result.success()
            }

            var successCount = 0
            var failCount = 0

            pendingActions.forEach { action ->
                try {
                    val success = when (action.actionType) {
                        "upload_story" -> processStoryUpload(action.jsonData)
                        "send_message" -> processSendMessage(action.jsonData)
                        "upload_post" -> processPostUpload(action.jsonData)
                        "like_post" -> processLikePost(action.jsonData)
                        "comment_post" -> processCommentPost(action.jsonData)
                        "follow_user" -> processFollowUser(action.jsonData)
                        else -> false
                    }

                    if (success) {
                        // Delete successful action
                        database.pendingActionDao().deleteAction(action.id)
                        successCount++
                    } else {
                        // Update retry count
                        database.pendingActionDao().updateAction(
                            action.copy(
                                retryCount = action.retryCount + 1,
                                status = if (action.retryCount >= 3) "failed" else "pending"
                            )
                        )
                        failCount++
                    }
                } catch (e: Exception) {
                    // Update retry count on exception
                    database.pendingActionDao().updateAction(
                        action.copy(
                            retryCount = action.retryCount + 1,
                            status = if (action.retryCount >= 3) "failed" else "pending"
                        )
                    )
                    failCount++
                }
            }

            // Schedule next sync if there are still pending actions
            if (failCount > 0) {
                return@withContext Result.retry()
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private suspend fun processStoryUpload(jsonData: String): Boolean {
        return try {
            val data = gson.fromJson<Map<String, Any>>(
                jsonData,
                object : TypeToken<Map<String, Any>>() {}.type
            )
            
            val imageUri = Uri.parse(data["imageUri"] as String)
            
            // Create temp file from URI
            val inputStream = applicationContext.contentResolver.openInputStream(imageUri)
            val tempFile = File(applicationContext.cacheDir, "story_${System.currentTimeMillis()}.jpg")
            FileOutputStream(tempFile).use { output ->
                inputStream?.copyTo(output)
            }
            inputStream?.close()
            
            val apiService = ApiClient.getApiService(applicationContext)
            val requestBody = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", tempFile.name, requestBody)
            
            val response = apiService.uploadStory(imagePart)
            tempFile.delete()
            
            response.isSuccessful && response.body()?.status == "success"
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun processSendMessage(jsonData: String): Boolean {
        return try {
            val data = gson.fromJson<Map<String, Any>>(
                jsonData,
                object : TypeToken<Map<String, Any>>() {}.type
            )
            
            val chatId = data["chatId"] as String
            val type = data["type"] as String
            val content = data["content"] as String
            val isVanishMode = data["isVanishMode"] as Boolean
            val currentUserId = sessionManager.getUserId() ?: return false
            val receiverId = chatId.split("_").firstOrNull { it != currentUserId } ?: return false
            
            val apiService = ApiClient.getApiService(applicationContext)
            
            when (type) {
                "text" -> {
                    val receiverIdBody = receiverId.toRequestBody("text/plain".toMediaTypeOrNull())
                    val messageBody = content.toRequestBody("text/plain".toMediaTypeOrNull())
                    val vanishModeBody = isVanishMode.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    
                    val response = apiService.sendMessage(receiverIdBody, messageBody, vanishModeBody, null)
                    response.isSuccessful && response.body()?.status == "success"
                }
                "image" -> {
                    val imageUri = Uri.parse(content)
                    val inputStream = applicationContext.contentResolver.openInputStream(imageUri)
                    val tempFile = File(applicationContext.cacheDir, "msg_${System.currentTimeMillis()}.jpg")
                    FileOutputStream(tempFile).use { output ->
                        inputStream?.copyTo(output)
                    }
                    inputStream?.close()
                    
                    val receiverIdBody = receiverId.toRequestBody("text/plain".toMediaTypeOrNull())
                    val vanishModeBody = isVanishMode.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val requestBody = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    val mediaPart = MultipartBody.Part.createFormData("media", tempFile.name, requestBody)
                    
                    val response = apiService.sendMessage(receiverIdBody, null, vanishModeBody, mediaPart)
                    tempFile.delete()
                    
                    response.isSuccessful && response.body()?.status == "success"
                }
                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun processPostUpload(jsonData: String): Boolean {
        return try {
            val data = gson.fromJson<Map<String, Any>>(
                jsonData,
                object : TypeToken<Map<String, Any>>() {}.type
            )
            
            // Will implement with post upload migration
            false
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun processLikePost(jsonData: String): Boolean {
        return try {
            val data = gson.fromJson<Map<String, Any>>(
                jsonData,
                object : TypeToken<Map<String, Any>>() {}.type
            )
            
            val postId = (data["postId"] as Double).toInt()
            val apiService = ApiClient.getApiService(applicationContext)
            val response = apiService.likePost(postId)
            
            response.isSuccessful && response.body()?.status == "success"
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun processCommentPost(jsonData: String): Boolean {
        return try {
            val data = gson.fromJson<Map<String, Any>>(
                jsonData,
                object : TypeToken<Map<String, Any>>() {}.type
            )
            
            val postId = (data["postId"] as Double).toInt()
            val comment = data["comment"] as String
            val apiService = ApiClient.getApiService(applicationContext)
            val response = apiService.addComment(postId, mapOf("comment" to comment))
            
            response.isSuccessful && response.body()?.status == "success"
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun processFollowUser(jsonData: String): Boolean {
        return try {
            val data = gson.fromJson<Map<String, Any>>(
                jsonData,
                object : TypeToken<Map<String, Any>>() {}.type
            )
            
            val followeeId = (data["followeeId"] as Double).toInt()
            val apiService = ApiClient.getApiService(applicationContext)
            val response = apiService.followUser(followeeId)
            
            response.isSuccessful && response.body()?.status == "success"
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        const val WORK_NAME = "sync_pending_actions"

        /**
         * Schedule periodic sync every 15 minutes
         */
        fun schedulePeriodic(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                15, TimeUnit.MINUTES,
                5, TimeUnit.MINUTES // Flex interval
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }

        /**
         * Trigger immediate one-time sync
         */
        fun triggerImmediateSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(syncRequest)
        }
    }
}
