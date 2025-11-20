package com.example.assignment1.data.models

data class AuthResponse(
    val user: User,
    val token: String,
    val requiresProfileSetup: Boolean = false
)
