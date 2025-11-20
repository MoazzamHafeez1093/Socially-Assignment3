package com.example.assignment1.utils

import android.content.Context
import android.net.Uri
import com.example.assignment1.data.network.ApiClient
import com.example.assignment1.data.prefs.SessionManager
import com.example.assignment1.data.local.AppDatabase
import com.example.assignment1.data.local.entities.MessageEntity
import com.example.assignment1.data.local.entities.PendingActionEntity
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.Serializable

data class ChatMessage(
    val messageId: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val type: String = "text", // text | image | post
    val content: String = "",   // text or imageUrl or postId
    val timestamp: Long = System.currentTimeMillis(),
    val editableUntil: Long = timestamp + 5 * 60 * 1000,
    val isVanishMode: Boolean = false
) : Serializable

class ChatRepository(private val context: Context) {
    private val sessionManager = SessionManager(context)
    private val database = AppDatabase.getInstance(context)

    fun sendText(chatId: String, text: String, isVanishMode: Boolean = false, onComplete: (Boolean) -> Unit) {
        val currentUserId = sessionManager.getUserId() ?: return onComplete(false)
        val receiverId = chatId.split("_").firstOrNull { it != currentUserId } ?: return onComplete(false)
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = ApiClient.getApiService(context)
                val receiverIdBody = receiverId.toRequestBody("text/plain".toMediaTypeOrNull())
                val messageBody = text.toRequestBody("text/plain".toMediaTypeOrNull())
                val vanishModeBody = isVanishMode.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                
                val response = apiService.sendMessage(receiverIdBody, messageBody, vanishModeBody, null)
                
                if (response.isSuccessful && response.body()?.status == "success") {
                    // Cache message locally
                    response.body()?.data?.let { message ->
                        database.messageDao().insertMessage(MessageEntity.fromMessage(message))
                    }
                    withContext(Dispatchers.Main) { onComplete(true) }
                } else {
                    // Queue for offline send
                    queueMessage(chatId, "text", text, isVanishMode)
                    withContext(Dispatchers.Main) { onComplete(true) }
                }
            } catch (e: Exception) {
                // Queue for offline send
                queueMessage(chatId, "text", text, isVanishMode)
                withContext(Dispatchers.Main) { onComplete(true) }
            }
        }
    }

    fun sendImage(context: Context, chatId: String, imageUri: Uri, isVanishMode: Boolean = false, onComplete: (Boolean) -> Unit) {
        val currentUserId = sessionManager.getUserId() ?: return onComplete(false)
        val receiverId = chatId.split("_").firstOrNull { it != currentUserId } ?: return onComplete(false)
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Create temp file from URI
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val tempFile = File(context.cacheDir, "msg_${System.currentTimeMillis()}.jpg")
                FileOutputStream(tempFile).use { output ->
                    inputStream?.copyTo(output)
                }
                inputStream?.close()
                
                val apiService = ApiClient.getApiService(context)
                val receiverIdBody = receiverId.toRequestBody("text/plain".toMediaTypeOrNull())
                val vanishModeBody = isVanishMode.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val requestBody = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val mediaPart = MultipartBody.Part.createFormData("media", tempFile.name, requestBody)
                
                val response = apiService.sendMessage(receiverIdBody, null, vanishModeBody, mediaPart)
                
                tempFile.delete()
                
                if (response.isSuccessful && response.body()?.status == "success") {
                    response.body()?.data?.let { message ->
                        database.messageDao().insertMessage(MessageEntity.fromMessage(message))
                    }
                    withContext(Dispatchers.Main) { onComplete(true) }
                } else {
                    queueMessage(chatId, "image", imageUri.toString(), isVanishMode)
                    withContext(Dispatchers.Main) { onComplete(true) }
                }
            } catch (e: Exception) {
                queueMessage(chatId, "image", imageUri.toString(), isVanishMode)
                withContext(Dispatchers.Main) { onComplete(true) }
            }
        }
    }

    fun sendPost(chatId: String, postId: String, onComplete: (Boolean) -> Unit) {
        // Post sharing - queue for now
        queueMessage(chatId, "post", postId, false)
        onComplete(true)
    }

    fun editMessage(chatId: String, messageId: String, newText: String, onComplete: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = ApiClient.getApiService(context)
                val response = apiService.editMessage(messageId.toInt(), mapOf("message" to newText))
                
                if (response.isSuccessful && response.body()?.status == "success") {
                    withContext(Dispatchers.Main) { onComplete(true) }
                } else {
                    withContext(Dispatchers.Main) { onComplete(false) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onComplete(false) }
            }
        }
    }

    fun deleteMessage(chatId: String, messageId: String, onComplete: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = ApiClient.getApiService(context)
                val response = apiService.deleteMessage(messageId.toInt())
                
                if (response.isSuccessful && response.body()?.status == "success") {
                    database.messageDao().deleteMessage(messageId)
                    withContext(Dispatchers.Main) { onComplete(true) }
                } else {
                    withContext(Dispatchers.Main) { onComplete(false) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onComplete(false) }
            }
        }
    }
    
    private fun queueMessage(chatId: String, type: String, content: String, isVanishMode: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            val messageData = mapOf(
                "chatId" to chatId,
                "type" to type,
                "content" to content,
                "isVanishMode" to isVanishMode,
                "timestamp" to System.currentTimeMillis()
            )
            
            val pendingAction = PendingActionEntity(
                actionType = "send_message",
                jsonData = Gson().toJson(messageData),
                retryCount = 0,
                status = "pending"
            )
            
            database.pendingActionDao().insertAction(pendingAction)
        }
    }
}


