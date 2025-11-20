package com.example.assignment1.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.assignment1.data.models.Story

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey
    val storyId: String,
    val userId: String,
    val username: String,
    val userProfilePic: String?,
    val imageUrl: String?,
    val videoUrl: String?,
    val caption: String?,
    val viewsCount: Int = 0,
    val viewedByCurrentUser: Boolean = false,
    val expiresAt: Long,
    val createdAt: Long
) {
    fun toStory() = Story(
        storyId = storyId,
        userId = userId,
        username = username,
        userProfilePic = userProfilePic,
        imageUrl = imageUrl,
        videoUrl = videoUrl,
        caption = caption,
        viewsCount = viewsCount,
        viewedByCurrentUser = viewedByCurrentUser,
        expiresAt = expiresAt,
        createdAt = createdAt
    )

    companion object {
        fun fromStory(story: Story) = StoryEntity(
            storyId = story.storyId,
            userId = story.userId,
            username = story.username,
            userProfilePic = story.userProfilePic,
            imageUrl = story.imageUrl,
            videoUrl = story.videoUrl,
            caption = story.caption,
            viewsCount = story.viewsCount ?: 0,
            viewedByCurrentUser = story.viewedByCurrentUser ?: false,
            expiresAt = story.expiresAt,
            createdAt = story.createdAt
        )
    }
}
