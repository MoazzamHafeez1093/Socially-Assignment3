package com.example.assignment1.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.assignment1.data.models.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val userId: String,
    val username: String,
    val email: String,
    val fullName: String?,
    val bio: String?,
    val profilePicture: String?,
    val isPrivate: Boolean = false,
    val isOnline: Boolean = false,
    val lastSeen: Long?,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val postsCount: Int = 0,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toUser() = User(
        userId = userId,
        username = username,
        email = email,
        fullName = fullName,
        bio = bio,
        profilePicture = profilePicture,
        isPrivate = isPrivate,
        isOnline = isOnline,
        lastSeen = lastSeen,
        followersCount = followersCount,
        followingCount = followingCount,
        postsCount = postsCount,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromUser(user: User) = UserEntity(
            userId = user.userId,
            username = user.username,
            email = user.email,
            fullName = user.fullName,
            bio = user.bio,
            profilePicture = user.profilePicture,
            isPrivate = user.isPrivate ?: false,
            isOnline = user.isOnline ?: false,
            lastSeen = user.lastSeen,
            followersCount = user.followersCount ?: 0,
            followingCount = user.followingCount ?: 0,
            postsCount = user.postsCount ?: 0,
            createdAt = user.createdAt ?: System.currentTimeMillis(),
            updatedAt = user.updatedAt ?: System.currentTimeMillis()
        )
    }
}
