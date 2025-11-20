package com.example.assignment1.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_actions")
data class PendingActionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val actionType: String, // "upload_story", "upload_post", "send_message", "like_post", "comment_post", etc.
    val jsonData: String, // JSON string of the data to be sent
    val retryCount: Int = 0,
    val maxRetries: Int = 3,
    val status: String = "pending", // "pending", "in_progress", "failed"
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastAttemptAt: Long? = null
)
