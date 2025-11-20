package com.example.assignment1.data.local.dao

import androidx.room.*
import com.example.assignment1.data.local.entities.PostEntity

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getPosts(limit: Int, offset: Int): List<PostEntity>

    @Query("SELECT * FROM posts WHERE postId = :postId")
    suspend fun getPostById(postId: String): PostEntity?

    @Query("SELECT * FROM posts WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getUserPosts(userId: String): List<PostEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<PostEntity>)

    @Update
    suspend fun updatePost(post: PostEntity)

    @Query("DELETE FROM posts WHERE postId = :postId")
    suspend fun deletePost(postId: String)

    @Query("DELETE FROM posts")
    suspend fun deleteAllPosts()

    @Query("UPDATE posts SET likesCount = :count, likedByCurrentUser = :liked WHERE postId = :postId")
    suspend fun updatePostLike(postId: String, count: Int, liked: Boolean)

    @Query("UPDATE posts SET commentsCount = :count WHERE postId = :postId")
    suspend fun updatePostCommentCount(postId: String, count: Int)
}
