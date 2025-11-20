package com.example.assignment1.data.models

data class Post(
    val id: Int,
    val user_id: Int,
    val caption: String?,
    val media_url: String?,
    val created_at: String,
    val updated_at: String,
    val username: String? = null,
    val profile_image: String? = null,
    val like_count: Int = 0,
    val comment_count: Int = 0,
    val liked_by_user: Boolean = false
)
