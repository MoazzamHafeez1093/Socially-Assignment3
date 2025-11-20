package com.example.assignment1.data.models

data class Story(
    val id: Int,
    val user_id: Int,
    val media_url: String,
    val media_type: String,
    val created_at: String,
    val expires_at: String,
    val username: String? = null,
    val profile_image: String? = null
)
