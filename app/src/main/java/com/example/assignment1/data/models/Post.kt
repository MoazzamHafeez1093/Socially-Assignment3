package com.example.assignment1.data.models

data class Post(
    val postId: String,
    val userId: String,
    val username: String,
    val userProfilePic: String?,
    val caption: String?,
    val images: List<String>,
    val likesCount: Int?,
    val commentsCount: Int?,
    val likedByCurrentUser: Boolean?,
    val createdAt: Long
) {
    // Helper to convert to legacy Post model for compatibility
    fun toLegacyPost() = com.example.assignment1.models.Post(
        postId = postId,
        userId = userId,
        username = username,
        userProfileImage = userProfilePic ?: "",
        imageUrl = images.firstOrNull() ?: "",
        caption = caption ?: "",
        timestamp = createdAt,
        likeCount = likesCount ?: 0,
        commentCount = commentsCount ?: 0
    )
}
