package com.example.assignment1.data.network

import com.example.assignment1.data.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // ==================== AUTH ====================
    @POST("api/auth/signup")
    suspend fun signup(
        @Body request: Map<String, String>
    ): Response<ApiResponse<AuthResponse>>
    
    @POST("api/auth/login")
    suspend fun login(
        @Body credentials: Map<String, String>
    ): Response<ApiResponse<AuthResponse>>
    
    @POST("api/auth/logout")
    suspend fun logout(): Response<ApiResponse<Any>>
    
    @GET("api/auth/me")
    suspend fun getCurrentUser(): Response<ApiResponse<User>>
    
    // ==================== STORIES ====================
    @GET("api/stories")
    suspend fun getStories(): Response<ApiResponse<List<Story>>>
    
    @Multipart
    @POST("api/stories")
    suspend fun uploadStory(
        @Part media: MultipartBody.Part
    ): Response<ApiResponse<Story>>
    
    // ==================== POSTS ====================
    @GET("api/posts")
    suspend fun getPosts(
        @Query("limit") limit: Int = 50
    ): Response<ApiResponse<List<Post>>>
    
    @Multipart
    @POST("api/posts")
    suspend fun createPost(
        @Part("caption") caption: RequestBody?,
        @Part media: MultipartBody.Part?
    ): Response<ApiResponse<Post>>
    
    @POST("api/posts/{id}/likes")
    suspend fun likePost(@Path("id") postId: Int): Response<ApiResponse<Any>>
    
    @DELETE("api/posts/{id}/likes")
    suspend fun unlikePost(@Path("id") postId: Int): Response<ApiResponse<Any>>
    
    @POST("api/posts/{id}/comments")
    suspend fun addComment(
        @Path("id") postId: Int,
        @Body comment: Map<String, String>
    ): Response<ApiResponse<Any>>
    
    @GET("api/posts/{id}/comments")
    suspend fun getComments(@Path("id") postId: Int): Response<ApiResponse<List<Any>>>
    
    // ==================== MESSAGES ====================
    @GET("api/messages/{userId}")
    suspend fun getConversation(
        @Path("userId") userId: Int,
        @Query("limit") limit: Int = 100
    ): Response<ApiResponse<List<Message>>>
    
    @Multipart
    @POST("api/messages")
    suspend fun sendMessage(
        @Part("receiver_id") receiverId: RequestBody,
        @Part("message") message: RequestBody?,
        @Part("vanish_mode") vanishMode: RequestBody?,
        @Part media: MultipartBody.Part?
    ): Response<ApiResponse<Message>>
    
    @PUT("api/messages/{id}")
    suspend fun editMessage(
        @Path("id") messageId: Int,
        @Body message: Map<String, String>
    ): Response<ApiResponse<Message>>
    
    @DELETE("api/messages/{id}")
    suspend fun deleteMessage(@Path("id") messageId: Int): Response<ApiResponse<Any>>
    
    @POST("api/messages/{id}/read")
    suspend fun markMessageRead(
        @Path("id") messageId: Int,
        @Body body: Map<String, Boolean>
    ): Response<ApiResponse<Message>>
    
    // ==================== FOLLOWS ====================
    @POST("api/follows/request")
    suspend fun sendFollowRequest(
        @Body request: Map<String, Int>
    ): Response<ApiResponse<Any>>
    
    @POST("api/follows/{id}/accept")
    suspend fun acceptFollowRequest(@Path("id") requestId: Int): Response<ApiResponse<Any>>
    
    @POST("api/follows/{id}/reject")
    suspend fun rejectFollowRequest(@Path("id") requestId: Int): Response<ApiResponse<Any>>
    
    @DELETE("api/follows/{id}")
    suspend fun unfollow(@Path("id") followId: Int): Response<ApiResponse<Any>>
    
    @GET("api/follows/pending")
    suspend fun getPendingRequests(): Response<ApiResponse<Any>>
    
    @GET("api/users/{id}/followers")
    suspend fun getFollowers(@Path("id") userId: Int): Response<ApiResponse<List<User>>>
    
    @GET("api/users/{id}/following")
    suspend fun getFollowing(@Path("id") userId: Int): Response<ApiResponse<List<User>>>
    
    // ==================== PROFILE ====================
    @GET("api/users/{id}")
    suspend fun getUserProfile(@Path("id") userId: Int): Response<ApiResponse<User>>
    
    @Multipart
    @POST("api/profile/image")
    suspend fun updateProfileImage(
        @Part image: MultipartBody.Part
    ): Response<ApiResponse<User>>
    
    @Multipart
    @POST("api/profile/cover")
    suspend fun updateCoverImage(
        @Part image: MultipartBody.Part
    ): Response<ApiResponse<User>>
    
    // ==================== SEARCH ====================
    @GET("api/search/users")
    suspend fun searchUsers(
        @Query("q") query: String
    ): Response<ApiResponse<List<User>>>
    
    // ==================== PRESENCE ====================
    @POST("api/presence/ping")
    suspend fun updatePresence(): Response<ApiResponse<Any>>
    
    @POST("api/presence/offline")
    suspend fun setOffline(): Response<ApiResponse<Any>>
    
    @GET("api/presence/{userId}")
    suspend fun getUserPresence(@Path("userId") userId: Int): Response<ApiResponse<Any>>
    
    // ==================== FCM ====================
    @POST("api/fcm/token")
    suspend fun registerFcmToken(
        @Body token: Map<String, String>
    ): Response<ApiResponse<Any>>
    
    @DELETE("api/fcm/token")
    suspend fun deleteFcmToken(): Response<ApiResponse<Any>>
}
