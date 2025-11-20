package com.example.assignment1.models

import java.io.Serializable

data class Post(
    val postId: String = "",
    val userId: String = "",
    val username: String = "",
    val userProfileImage: String = "",
    val imageUrl: String = "",
    val caption: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val likes: MutableList<String> = mutableListOf(),
    val comments: MutableList<Comment> = mutableListOf(),
    val likeCount: Int = 0,
    val commentCount: Int = 0
) : Serializable

data class Comment(
    val commentId: String = "",
    val userId: String = "",
    val username: String = "",
    val userProfileImage: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
) : Serializable
