package com.example.assignment1.utils

import android.content.Context
import android.widget.Toast
import com.example.assignment1.models.User
import com.example.assignment1.data.network.ApiClient
import com.example.assignment1.data.prefs.SessionManager
import com.example.assignment1.data.local.AppDatabase
import com.example.assignment1.data.local.entities.PendingActionEntity
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FollowManager(private val context: Context) {
    private val sessionManager = SessionManager(context)
    private val database = AppDatabase.getInstance(context)
    
    // Send follow request (direct follow in REST API)
    fun sendFollowRequest(fromUserId: String, toUserId: String, context: Context, onComplete: (Boolean, String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = ApiClient.getApiService(context)
                val response = apiService.followUser(toUserId.toInt())
                
                if (response.isSuccessful && response.body()?.status == "success") {
                    withContext(Dispatchers.Main) {
                        onComplete(true, "Follow request sent successfully")
                        Toast.makeText(context, "Followed user!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Queue for offline
                    queueFollowAction(toUserId)
                    withContext(Dispatchers.Main) {
                        onComplete(true, "Follow request queued")
                        Toast.makeText(context, "Follow request queued", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                // Queue for offline
                queueFollowAction(toUserId)
                withContext(Dispatchers.Main) {
                    onComplete(true, "Follow request queued")
                    Toast.makeText(context, "Follow request queued", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun queueFollowAction(followeeId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val followData = mapOf(
                "followeeId" to followeeId.toInt(),
                "timestamp" to System.currentTimeMillis()
            )
            
            val pendingAction = PendingActionEntity(
                actionType = "follow_user",
                jsonData = Gson().toJson(followData),
                retryCount = 0,
                status = "pending"
            )
            
            database.pendingActionDao().insertAction(pendingAction)
        }
    }
    
    // Accept follow request (not needed in direct follow system)
    fun acceptFollowRequest(fromUserId: String, toUserId: String, context: Context, onComplete: (Boolean, String?) -> Unit) {
        onComplete(true, "Direct follow system - no requests to accept")
    }
    
    // Reject follow request (not needed in direct follow system)
    fun rejectFollowRequest(fromUserId: String, toUserId: String, context: Context, onComplete: (Boolean, String?) -> Unit) {
        onComplete(true, "Direct follow system - no requests to reject")
    }
    
    // Get followers list
    fun getFollowers(userId: String, onComplete: (List<User>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = ApiClient.getApiService(context)
                val response = apiService.getFollowers(userId.toInt())
                
                if (response.isSuccessful && response.body()?.status == "success") {
                    val followers = response.body()?.data ?: emptyList()
                    withContext(Dispatchers.Main) {
                        onComplete(followers.map { it.toUser() })
                    }
                } else {
                    withContext(Dispatchers.Main) { onComplete(emptyList()) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onComplete(emptyList()) }
            }
        }
    }
    
    // Get following list
    fun getFollowing(userId: String, onComplete: (List<User>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = ApiClient.getApiService(context)
                val response = apiService.getFollowing(userId.toInt())
                
                if (response.isSuccessful && response.body()?.status == "success") {
                    val following = response.body()?.data ?: emptyList()
                    withContext(Dispatchers.Main) {
                        onComplete(following.map { it.toUser() })
                    }
                } else {
                    withContext(Dispatchers.Main) { onComplete(emptyList()) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onComplete(emptyList()) }
            }
        }
    }
    
    // Get pending follow requests (not used in direct follow system)
    fun getPendingFollowRequests(userId: String, onComplete: (List<User>) -> Unit) {
        onComplete(emptyList())
    }
    
    // Check if user is following another user
    fun isFollowing(currentUserId: String, targetUserId: String, onComplete: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = ApiClient.getApiService(context)
                val response = apiService.checkFollowing(targetUserId.toInt())
                
                if (response.isSuccessful && response.body()?.status == "success") {
                    val isFollowing = response.body()?.data?.get("isFollowing") as? Boolean ?: false
                    withContext(Dispatchers.Main) { onComplete(isFollowing) }
                } else {
                    withContext(Dispatchers.Main) { onComplete(false) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onComplete(false) }
            }
        }
    }
    
    // Unfollow user
    fun unfollowUser(currentUserId: String, targetUserId: String, context: Context, onComplete: (Boolean, String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = ApiClient.getApiService(context)
                val response = apiService.unfollowUser(targetUserId.toInt())
                
                if (response.isSuccessful && response.body()?.status == "success") {
                    withContext(Dispatchers.Main) {
                        onComplete(true, "Unfollowed successfully")
                        Toast.makeText(context, "Unfollowed user", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onComplete(false, "Failed to unfollow")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onComplete(false, "Error unfollowing user: ${e.message}")
                }
            }
        }
    }
}
    
    // Send follow request
    fun sendFollowRequest(fromUserId: String, toUserId: String, context: Context, onComplete: (Boolean, String?) -> Unit) {
        try {
            val followRequestData = mapOf(
                "fromUserId" to fromUserId,
                "toUserId" to toUserId,
                "timestamp" to System.currentTimeMillis(),
                "status" to "pending"
            )
            
            database.reference.child("followRequests").child(toUserId).child(fromUserId)
                .setValue(followRequestData)
                .addOnSuccessListener {
                    onComplete(true, "Follow request sent successfully")
                    Toast.makeText(context, "Follow request sent!", Toast.LENGTH_SHORT).show()
                    
                    // Send notification to the user
                    try {
                        com.example.assignment1.services.MyFirebaseMessagingService.sendNotificationToUser(
                            toUserId,
                            "New Follow Request",
                            "You have a new follow request",
                            "follow_request",
                            fromUserId,
                            "User"
                        )
                    } catch (e: Exception) {
                        // Notification sending failed, but follow request was sent
                    }
                }
                .addOnFailureListener { e ->
                    onComplete(false, e.message)
                }
        } catch (e: Exception) {
            onComplete(false, "Error sending follow request: ${e.message}")
        }
    }
    
    // Accept follow request
    fun acceptFollowRequest(fromUserId: String, toUserId: String, context: Context, onComplete: (Boolean, String?) -> Unit) {
        try {
            // Add to followers/following lists
            val followerData = mapOf(
                "userId" to fromUserId,
                "timestamp" to System.currentTimeMillis()
            )
            val followingData = mapOf(
                "userId" to toUserId,
                "timestamp" to System.currentTimeMillis()
            )
            
            database.reference.child("followers").child(toUserId).child(fromUserId).setValue(followerData)
            database.reference.child("following").child(fromUserId).child(toUserId).setValue(followingData)
            
            // Remove follow request
            database.reference.child("followRequests").child(toUserId).child(fromUserId).removeValue()
                .addOnSuccessListener {
                    onComplete(true, "Follow request accepted")
                    Toast.makeText(context, "Follow request accepted!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    onComplete(false, e.message)
                }
        } catch (e: Exception) {
            onComplete(false, "Error accepting follow request: ${e.message}")
        }
    }
    
    // Reject follow request
    fun rejectFollowRequest(fromUserId: String, toUserId: String, context: Context, onComplete: (Boolean, String?) -> Unit) {
        try {
            database.reference.child("followRequests").child(toUserId).child(fromUserId).removeValue()
                .addOnSuccessListener {
                    onComplete(true, "Follow request rejected")
                    Toast.makeText(context, "Follow request rejected", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    onComplete(false, e.message)
                }
        } catch (e: Exception) {
            onComplete(false, "Error rejecting follow request: ${e.message}")
        }
    }
    
    // Get followers list
    fun getFollowers(userId: String, onComplete: (List<User>) -> Unit) {
        try {
            database.reference.child("followers").child(userId)
                .addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
                    override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                        val followers = mutableListOf<User>()
                        for (followerSnapshot in snapshot.children) {
                            val followerId = followerSnapshot.key
                            if (followerId != null) {
                                // Get user details
                                database.reference.child("users").child(followerId)
                                    .addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
                                        override fun onDataChange(userSnapshot: com.google.firebase.database.DataSnapshot) {
                                            val user = userSnapshot.getValue(User::class.java)
                                            if (user != null) {
                                                followers.add(user)
                                                if (followers.size == snapshot.children.count().toInt()) {
                                                    onComplete(followers)
                                                }
                                            }
                                        }
                                        override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                                            onComplete(emptyList())
                                        }
                                    })
                            }
                        }
                        if (snapshot.children.count().toInt() == 0) {
                            onComplete(emptyList())
                        }
                    }
                    override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                        onComplete(emptyList())
                    }
                })
        } catch (e: Exception) {
            onComplete(emptyList())
        }
    }
    
    // Get following list
    fun getFollowing(userId: String, onComplete: (List<User>) -> Unit) {
        try {
            database.reference.child("following").child(userId)
                .addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
                    override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                        val following = mutableListOf<User>()
                        for (followingSnapshot in snapshot.children) {
                            val followingId = followingSnapshot.key
                            if (followingId != null) {
                                // Get user details
                                database.reference.child("users").child(followingId)
                                    .addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
                                        override fun onDataChange(userSnapshot: com.google.firebase.database.DataSnapshot) {
                                            val user = userSnapshot.getValue(User::class.java)
                                            if (user != null) {
                                                following.add(user)
                                                if (following.size == snapshot.children.count().toInt()) {
                                                    onComplete(following)
                                                }
                                            }
                                        }
                                        override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                                            onComplete(emptyList())
                                        }
                                    })
                            }
                        }
                        if (snapshot.children.count().toInt() == 0) {
                            onComplete(emptyList())
                        }
                    }
                    override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                        onComplete(emptyList())
                    }
                })
        } catch (e: Exception) {
            onComplete(emptyList())
        }
    }
    
    // Get pending follow requests
    fun getPendingFollowRequests(userId: String, onComplete: (List<User>) -> Unit) {
        try {
            database.reference.child("followRequests").child(userId)
                .addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
                    override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                        val requests = mutableListOf<User>()
                        for (requestSnapshot in snapshot.children) {
                            val requestUserId = requestSnapshot.key
                            if (requestUserId != null) {
                                // Get user details
                                database.reference.child("users").child(requestUserId)
                                    .addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
                                        override fun onDataChange(userSnapshot: com.google.firebase.database.DataSnapshot) {
                                            val user = userSnapshot.getValue(User::class.java)
                                            if (user != null) {
                                                requests.add(user)
                                                if (requests.size == snapshot.children.count().toInt()) {
                                                    onComplete(requests)
                                                }
                                            }
                                        }
                                        override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                                            onComplete(emptyList())
                                        }
                                    })
                            }
                        }
                        if (snapshot.children.count().toInt() == 0) {
                            onComplete(emptyList())
                        }
                    }
                    override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                        onComplete(emptyList())
                    }
                })
        } catch (e: Exception) {
            onComplete(emptyList())
        }
    }
    
    // Check if user is following another user
    fun isFollowing(currentUserId: String, targetUserId: String, onComplete: (Boolean) -> Unit) {
        try {
            database.reference.child("following").child(currentUserId).child(targetUserId)
                .addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
                    override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                        onComplete(snapshot.exists())
                    }
                    override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                        onComplete(false)
                    }
                })
        } catch (e: Exception) {
            onComplete(false)
        }
    }
    
    // Unfollow user
    fun unfollowUser(currentUserId: String, targetUserId: String, context: Context, onComplete: (Boolean, String?) -> Unit) {
        try {
            database.reference.child("following").child(currentUserId).child(targetUserId).removeValue()
            database.reference.child("followers").child(targetUserId).child(currentUserId).removeValue()
                .addOnSuccessListener {
                    onComplete(true, "Unfollowed successfully")
                    Toast.makeText(context, "Unfollowed user", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    onComplete(false, e.message)
                }
        } catch (e: Exception) {
            onComplete(false, "Error unfollowing user: ${e.message}")
        }
    }
}