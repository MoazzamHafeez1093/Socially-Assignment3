package com.example.assignment1.models

import java.io.Serializable

data class User(
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val profileImageUrl: String = "", // Can store Base64 or URL
    val profileImageBase64: String = "", // Base64 encoded profile image for Firebase free plan
    val isOnline: Boolean = false,
    val followers: MutableList<String> = mutableListOf(),
    val following: MutableList<String> = mutableListOf(),
    val stories: MutableList<String> = mutableListOf() // Story IDs
) : Serializable