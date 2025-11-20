package com.example.assignment1.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.assignment1.data.models.Message
import com.example.assignment1.utils.ChatMessage

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val messageId: String,
    val senderId: String,
    val receiverId: String,
    val content: String?,
    val imageUrl: String?,
    val messageType: String = "text",
    val isVanishMode: Boolean = false,
    val vanishAt: Long? = null,
    val isRead: Boolean = false,
    val readAt: Long? = null,
    val createdAt: Long
) {
    fun toMessage() = Message(
        messageId = messageId,
        senderId = senderId,
        receiverId = receiverId,
        content = content,
        imageUrl = imageUrl,
        messageType = messageType,
        isVanishMode = isVanishMode,
        vanishAt = vanishAt,
        isRead = isRead,
        readAt = readAt,
        createdAt = createdAt
    )

    fun toChatMessage() = ChatMessage(
        messageId = messageId,
        chatId = "${if (senderId < receiverId) senderId else receiverId}_${if (senderId < receiverId) receiverId else senderId}",
        senderId = senderId,
        type = messageType,
        content = content ?: imageUrl ?: "",
        timestamp = createdAt,
        isVanishMode = isVanishMode
    )

    companion object {
        fun fromMessage(message: Message) = MessageEntity(
            messageId = message.messageId,
            senderId = message.senderId,
            receiverId = message.receiverId,
            content = message.content,
            imageUrl = message.imageUrl,
            messageType = message.messageType ?: "text",
            isVanishMode = message.isVanishMode ?: false,
            vanishAt = message.vanishAt,
            isRead = message.isRead ?: false,
            readAt = message.readAt,
            createdAt = message.createdAt
        )
    }
}
