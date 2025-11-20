package com.example.assignment1.data.models

data class Message(
    val id: Int,
    val sender_id: Int,
    val receiver_id: Int,
    val message: String?,
    val media_url: String? = null,
    val media_type: String? = null,
    val vanish_mode: Boolean = false,
    val created_at: String,
    val updated_at: String,
    val read_at: String? = null,
    val deleted_at: String? = null
)
