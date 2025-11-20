package com.example.assignment1.data.models

data class Story(
    val storyId: String,
    val userId: String,
    val username: String,
    val userProfilePic: String?,
    val imageUrl: String?,
    val videoUrl: String?,
    val caption: String?,
    val viewsCount: Int?,
    val viewedByCurrentUser: Boolean?,
    val expiresAt: Long,
    val createdAt: Long
) {
    // Helper to convert to legacy Story model for compatibility
    fun toLegacyStory() = com.example.assignment1.models.Story(
        storyId = storyId,
        userId = userId,
        username = username,
        userProfileImage = userProfilePic ?: "",
        imageUrl = imageUrl ?: "",
        videoUrl = videoUrl ?: "",
        timestamp = createdAt,
        expiresAt = expiresAt,
        isViewed = viewedByCurrentUser ?: false
    )
}
