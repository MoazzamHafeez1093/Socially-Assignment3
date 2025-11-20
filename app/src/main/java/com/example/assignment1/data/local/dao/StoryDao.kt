package com.example.assignment1.data.local.dao

import androidx.room.*
import com.example.assignment1.data.local.entities.StoryEntity

@Dao
interface StoryDao {
    @Query("SELECT * FROM stories WHERE expiresAt > :currentTime ORDER BY createdAt DESC")
    suspend fun getActiveStories(currentTime: Long = System.currentTimeMillis()): List<StoryEntity>

    @Query("SELECT * FROM stories WHERE userId = :userId AND expiresAt > :currentTime ORDER BY createdAt DESC")
    suspend fun getUserStories(userId: String, currentTime: Long = System.currentTimeMillis()): List<StoryEntity>

    @Query("SELECT * FROM stories WHERE storyId = :storyId")
    suspend fun getStoryById(storyId: String): StoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: StoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStories(stories: List<StoryEntity>)

    @Update
    suspend fun updateStory(story: StoryEntity)

    @Query("DELETE FROM stories WHERE storyId = :storyId")
    suspend fun deleteStory(storyId: String)

    @Query("DELETE FROM stories WHERE expiresAt <= :currentTime")
    suspend fun deleteExpiredStories(currentTime: Long = System.currentTimeMillis())

    @Query("DELETE FROM stories")
    suspend fun deleteAllStories()

    @Query("UPDATE stories SET viewedByCurrentUser = 1 WHERE storyId = :storyId")
    suspend fun markStoryAsViewed(storyId: String)
}
