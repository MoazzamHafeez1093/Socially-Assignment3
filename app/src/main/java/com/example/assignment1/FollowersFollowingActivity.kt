package com.example.assignment1

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment1.adapters.UserAdapter
import com.example.assignment1.models.User
import com.example.assignment1.utils.FirebaseAuthManager
import com.example.assignment1.utils.FollowManager

class FollowersFollowingActivity : AppCompatActivity() {
    private lateinit var followManager: FollowManager
    private lateinit var authManager: FirebaseAuthManager
    private lateinit var usersRecyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private val users = mutableListOf<User>()
    private var currentMode = "followers" // "followers" or "following"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Get mode from intent
        currentMode = intent.getStringExtra("mode") ?: "followers"
        
        // Create simple UI programmatically
        createSimpleFollowersFollowingScreen()
        
        try {
            followManager = FollowManager()
            authManager = FirebaseAuthManager()
        } catch (e: Exception) {
            // If Firebase fails to initialize, continue without it
        }
        
        setupUsersRecyclerView()
        loadUsers()
    }
    
    private fun createSimpleFollowersFollowingScreen() {
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setBackgroundColor(android.graphics.Color.WHITE)
            setPadding(20, 20, 20, 20)
        }
        
        val titleText = android.widget.TextView(this).apply {
            text = if (currentMode == "followers") "Followers" else "Following"
            textSize = 24f
            setTextColor(android.graphics.Color.parseColor("#784A34"))
            gravity = android.view.Gravity.CENTER
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 30
            }
        }
        
        val backButton = android.widget.Button(this).apply {
            text = "← Back"
            setBackgroundColor(android.graphics.Color.parseColor("#784A34"))
            setTextColor(android.graphics.Color.WHITE)
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 20
            }
            setOnClickListener {
                finish()
            }
        }
        
        val switchButton = android.widget.Button(this).apply {
            text = if (currentMode == "followers") "Show Following" else "Show Followers"
            setBackgroundColor(android.graphics.Color.parseColor("#784A34"))
            setTextColor(android.graphics.Color.WHITE)
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 20
            }
            setOnClickListener {
                currentMode = if (currentMode == "followers") "following" else "followers"
                titleText.text = if (currentMode == "followers") "Followers" else "Following"
                text = if (currentMode == "followers") "Show Following" else "Show Followers"
                loadUsers()
            }
        }
        
        usersRecyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@FollowersFollowingActivity)
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }
        
        layout.addView(titleText)
        layout.addView(backButton)
        layout.addView(switchButton)
        layout.addView(usersRecyclerView)
        setContentView(layout)
    }
    
    private fun setupUsersRecyclerView() {
        userAdapter = UserAdapter(users) { user ->
            // Handle user click - could open user profile
            Toast.makeText(this, "Clicked on ${user.username}", Toast.LENGTH_SHORT).show()
        }
        usersRecyclerView.adapter = userAdapter
    }
    
    private fun loadUsers() {
        try {
            val currentUser = authManager.getCurrentUser()
            if (currentUser != null) {
                if (currentMode == "followers") {
                    followManager.getFollowers(currentUser.userId) { followers ->
                        runOnUiThread {
                            users.clear()
                            users.addAll(followers)
                            userAdapter.notifyDataSetChanged()
                        }
                    }
                } else {
                    followManager.getFollowing(currentUser.userId) { following ->
                        runOnUiThread {
                            users.clear()
                            users.addAll(following)
                            userAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Demo mode - No ${currentMode}", Toast.LENGTH_SHORT).show()
        }
    }
}
