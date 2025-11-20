package com.example.assignment1.utils

import android.content.Context
import android.net.Uri
import com.example.assignment1.models.Post
import com.example.assignment1.models.Comment
import com.example.assignment1.data.network.ApiClient
import com.example.assignment1.data.prefs.SessionManager
import com.example.assignment1.data.local.AppDatabase
import com.example.assignment1.data.local.entities.PostEntity
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

class PostRepository(private val context: Context) {
    private val sessionManager = SessionManager(context)
    private val database = AppDatabase.getInstance(context)

    fun createPost(context: Context, imageUri: Uri, caption: String, onComplete: (Boolean, String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Create temp file from URI
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val tempFile = File(context.cacheDir, "post_${System.currentTimeMillis()}.jpg")
                FileOutputStream(tempFile).use { output ->
                    inputStream?.copyTo(output)
                }
                inputStream?.close()
                
                val apiService = ApiClient.getApiService(context)
                val requestBody = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", tempFile.name, requestBody)
                val captionBody = caption.toRequestBody("text/plain".toMediaTypeOrNull())
                
                val response = apiService.createPost(imagePart, captionBody)
                tempFile.delete()
                
                if (response.isSuccessful && response.body()?.status == "success") {
                    val postId = response.body()?.data?.id?.toString()
                    withContext(Dispatchers.Main) { onComplete(true, postId) }
                } else {
                    // Queue for offline upload
                    queuePostUpload(imageUri.toString(), caption)
                    withContext(Dispatchers.Main) { onComplete(true, null) }
                }
            } catch (e: Exception) {
                // Queue for offline upload
                queuePostUpload(imageUri.toString(), caption)
                withContext(Dispatchers.Main) { onComplete(true, null) }
            }
        }
    }
    
    private fun queuePostUpload(imageUri: String, caption: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val postData = mapOf(
                "imageUri" to imageUri,
                "caption" to caption,
                "timestamp" to System.currentTimeMillis()
            )
            
            val pendingAction = PendingActionEntity(
                actionType = "upload_post",
                jsonData = Gson().toJson(postData),
                retryCount = 0,
                status = "pending"
            )
            
            database.pendingActionDao().insertAction(pendingAction)
        }
    }

    fun likePost(postId: String, onComplete: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = ApiClient.getApiService(context)
                val response = apiService.likePost(postId.toInt())
                
                if (response.isSuccessful && response.body()?.status == "success") {
                    withContext(Dispatchers.Main) { onComplete(true) }
                } else {
                    // Queue for offline
                    queueLikePost(postId)
                    withContext(Dispatchers.Main) { onComplete(true) }
                }
            } catch (e: Exception) {
                // Queue for offline
                queueLikePost(postId)
                withContext(Dispatchers.Main) { onComplete(true) }
            }
        }
    }
    
    private fun queueLikePost(postId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val likeData = mapOf(
                "postId" to postId.toInt(),
                "timestamp" to System.currentTimeMillis()
            )
            
            val pendingAction = PendingActionEntity(
                actionType = "like_post",
                jsonData = Gson().toJson(likeData),
                retryCount = 0,
                status = "pending"
            )
            
            database.pendingActionDao().insertAction(pendingAction)
        }
    }

    fun addComment(postId: String, text: String, onComplete: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = ApiClient.getApiService(context)
                val response = apiService.addComment(postId.toInt(), mapOf("comment" to text))
                
                if (response.isSuccessful && response.body()?.status == "success") {
                    withContext(Dispatchers.Main) { onComplete(true) }
                } else {
                    // Queue for offline
                    queueComment(postId, text)
                    withContext(Dispatchers.Main) { onComplete(true) }
                }
            } catch (e: Exception) {
                // Queue for offline
                queueComment(postId, text)
                withContext(Dispatchers.Main) { onComplete(true) }
            }
        }
    }
    
    private fun queueComment(postId: String, comment: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val commentData = mapOf(
                "postId" to postId.toInt(),
                "comment" to comment,
                "timestamp" to System.currentTimeMillis()
            )
            
            val pendingAction = PendingActionEntity(
                actionType = "comment_post",
                jsonData = Gson().toJson(commentData),
                retryCount = 0,
                status = "pending"
            )
            
            database.pendingActionDao().insertAction(pendingAction)
        }
    }

    fun getPosts(onPostsLoaded: (List<Post>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Load from cache first
                val cachedPosts = database.postDao().getAllPosts()
                withContext(Dispatchers.Main) {
                    if (cachedPosts.isNotEmpty()) {
                        onPostsLoaded(cachedPosts.map { it.toPost() })
                    }
                }
                
                // Fetch from API
                val apiService = ApiClient.getApiService(context)
                val response = apiService.getFeed()
                
                if (response.isSuccessful && response.body()?.status == "success") {
                    val posts = response.body()?.data ?: emptyList()
                    
                    // Cache in Room
                    posts.forEach { post ->
                        database.postDao().insertPost(PostEntity.fromPost(post))
                    }
                    
                    withContext(Dispatchers.Main) {
                        onPostsLoaded(posts.map { it.toPost() })
                    }
                } else if (cachedPosts.isEmpty()) {
                    withContext(Dispatchers.Main) { onPostsLoaded(emptyList()) }
                }
            } catch (e: Exception) {
                // Use cached posts on error
                val cachedPosts = database.postDao().getAllPosts()
                withContext(Dispatchers.Main) {
                    onPostsLoaded(cachedPosts.map { it.toPost() })
                }
            }
        }
    }
    
    fun getUserPosts(userId: String, onComplete: (List<Post>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = ApiClient.getApiService(context)
                val response = apiService.getUserPosts(userId.toInt())
                
                if (response.isSuccessful && response.body()?.status == "success") {
                    val posts = response.body()?.data ?: emptyList()
                    withContext(Dispatchers.Main) {
                        onComplete(posts.map { it.toPost() })
                    }
                } else {
                    withContext(Dispatchers.Main) { onComplete(emptyList()) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onComplete(emptyList()) }
            }
        }
    }
    
    fun getComments(postId: String, onComplete: (List<Comment>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = ApiClient.getApiService(context)
                val response = apiService.getComments(postId.toInt())
                
                if (response.isSuccessful && response.body()?.status == "success") {
                    val comments = response.body()?.data ?: emptyList()
                    withContext(Dispatchers.Main) {
                        onComplete(comments.map { it.toComment() })
                    }
                } else {
                    withContext(Dispatchers.Main) { onComplete(emptyList()) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onComplete(emptyList()) }
            }
        }
    }
}
