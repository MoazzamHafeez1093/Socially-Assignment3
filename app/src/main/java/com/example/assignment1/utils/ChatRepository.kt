package com.example.assignment1.utils

import android.content.Context
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.Serializable

data class ChatMessage(
    val messageId: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val type: String = "text", // text | image | post
    val content: String = "",   // text or imageUrl or postId
    val timestamp: Long = System.currentTimeMillis(),
    val editableUntil: Long = timestamp + 5 * 60 * 1000
) : Serializable

class ChatRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference

    fun sendText(chatId: String, text: String, onComplete: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onComplete(false)
        val messageId = db.child("messages").child(chatId).push().key ?: return onComplete(false)
        val payload = ChatMessage(
            messageId = messageId,
            chatId = chatId,
            senderId = uid,
            type = "text",
            content = text
        )
        db.child("messages").child(chatId).child(messageId).setValue(payload)
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    fun sendImage(context: Context, chatId: String, imageUri: Uri, onComplete: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onComplete(false)
        
        // Convert image to Base64
        val base64Image = Base64Image.uriToBase64(context, imageUri)
        if (base64Image == null) {
            onComplete(false)
            return
        }
        
        val messageId = db.child("messages").child(chatId).push().key ?: return onComplete(false)
        val payload = ChatMessage(
            messageId = messageId,
            chatId = chatId,
            senderId = uid,
            type = "image",
            content = base64Image // Store Base64 string instead of URL
        )
        db.child("messages").child(chatId).child(messageId).setValue(payload)
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    fun sendPost(chatId: String, postId: String, onComplete: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onComplete(false)
        val messageId = db.child("messages").child(chatId).push().key ?: return onComplete(false)
        val payload = ChatMessage(
            messageId = messageId,
            chatId = chatId,
            senderId = uid,
            type = "post",
            content = postId
        )
        db.child("messages").child(chatId).child(messageId).setValue(payload)
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    fun editMessage(chatId: String, messageId: String, newText: String, onComplete: (Boolean) -> Unit) {
        db.child("messages").child(chatId).child(messageId).get().addOnSuccessListener { snap ->
            val msg = snap.getValue(ChatMessage::class.java) ?: return@addOnSuccessListener
            if (System.currentTimeMillis() <= msg.editableUntil && msg.type == "text") {
                db.child("messages").child(chatId).child(messageId).child("content").setValue(newText)
                    .addOnCompleteListener { onComplete(it.isSuccessful) }
            } else {
                onComplete(false)
            }
        }.addOnFailureListener { onComplete(false) }
    }

    fun deleteMessage(chatId: String, messageId: String, onComplete: (Boolean) -> Unit) {
        db.child("messages").child(chatId).child(messageId).get().addOnSuccessListener { snap ->
            val msg = snap.getValue(ChatMessage::class.java) ?: return@addOnSuccessListener
            if (System.currentTimeMillis() <= msg.editableUntil) {
                db.child("messages").child(chatId).child(messageId).removeValue()
                    .addOnCompleteListener { onComplete(it.isSuccessful) }
            } else {
                onComplete(false)
            }
        }.addOnFailureListener { onComplete(false) }
    }
}


