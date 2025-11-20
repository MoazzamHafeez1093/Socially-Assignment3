package com.example.assignment1.data.models

data class ApiResponse<T>(
    val status: String,
    val data: T? = null,
    val message: String? = null
)
