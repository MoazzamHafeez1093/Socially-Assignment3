package com.example.assignment1.data.local.dao

import androidx.room.*
import com.example.assignment1.data.local.entities.MessageEntity

@Dao
interface MessageDao {
    @Query("""
        SELECT * FROM messages 
        WHERE (senderId = :userId1 AND receiverId = :userId2) 
           OR (senderId = :userId2 AND receiverId = :userId1)
        ORDER BY createdAt ASC
    """)
    suspend fun getConversation(userId1: String, userId2: String): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE messageId = :messageId")
    suspend fun getMessageById(messageId: String): MessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE messageId = :messageId")
    suspend fun deleteMessage(messageId: String)

    @Query("DELETE FROM messages WHERE vanishAt <= :currentTime AND isVanishMode = 1")
    suspend fun deleteVanishedMessages(currentTime: Long = System.currentTimeMillis())

    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()

    @Query("UPDATE messages SET isRead = 1, readAt = :readAt WHERE messageId = :messageId")
    suspend fun markMessageAsRead(messageId: String, readAt: Long = System.currentTimeMillis())

    @Query("""
        SELECT * FROM messages 
        WHERE receiverId = :userId AND isRead = 0
        ORDER BY createdAt DESC
    """)
    suspend fun getUnreadMessages(userId: String): List<MessageEntity>
}
