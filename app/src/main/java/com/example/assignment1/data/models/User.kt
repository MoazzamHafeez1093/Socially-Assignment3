package com.example.assignment1.data.models

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val profile_image: String? = null,
    val cover_image: String? = null,
    val created_at: String? = null
)
