package com.example.assignment1.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.assignment1.data.local.converters.StringListConverter
import com.example.assignment1.data.models.Post

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey
    val postId: String,
    val userId: String,
    val username: String,
    val userProfilePic: String?,
    val caption: String?,
    @TypeConverters(StringListConverter::class)
    val images: List<String>,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val likedByCurrentUser: Boolean = false,
    val createdAt: Long
) {
    fun toPost() = Post(
        postId = postId,
        userId = userId,
        username = username,
        userProfilePic = userProfilePic,
        caption = caption,
        images = images,
        likesCount = likesCount,
        commentsCount = commentsCount,
        likedByCurrentUser = likedByCurrentUser,
        createdAt = createdAt
    )

    companion object {
        fun fromPost(post: Post) = PostEntity(
            postId = post.postId,
            userId = post.userId,
            username = post.username,
            userProfilePic = post.userProfilePic,
            caption = post.caption,
            images = post.images,
            likesCount = post.likesCount ?: 0,
            commentsCount = post.commentsCount ?: 0,
            likedByCurrentUser = post.likedByCurrentUser ?: false,
            createdAt = post.createdAt
        )
    }
}
