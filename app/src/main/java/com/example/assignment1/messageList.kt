package com.example.assignment1

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment1.adapters.UserAdapter
import com.example.assignment1.models.User

class messageList : AppCompatActivity() {
    
    private lateinit var usersRecyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private val users = mutableListOf<User>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_message_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val username = sharedPref.getString("USERNAME_KEY", "Guest")
        val imageUri = sharedPref.getString("IMAGE_URI_KEY", null)


        val usernameTextView = findViewById<TextView>(R.id.header_title) // Make sure this ID exists
        // Set the username to a TextView
        usernameTextView.text = username

        // Setup users list
        setupUsersRecyclerView()
        loadUsers()
    }
    
    private fun setupUsersRecyclerView() {
        usersRecyclerView = findViewById(R.id.usersRecyclerView)
        userAdapter = UserAdapter(users) { user ->
            // Handle user click - start chat
            val intentChat = Intent(this, chat::class.java)
            intentChat.putExtra("PersonName", user.username)
            intentChat.putExtra("userId", user.userId)
            startActivity(intentChat)
        }
        usersRecyclerView.layoutManager = LinearLayoutManager(this)
        usersRecyclerView.adapter = userAdapter
    }
    
    private fun loadUsers() {
        // Add some sample users for demonstration
        val sampleUsers = listOf(
            User("user1", "Alice", "alice@example.com", ""),
            User("user2", "Bob", "bob@example.com", ""),
            User("user3", "Charlie", "charlie@example.com", ""),
            User("user4", "Diana", "diana@example.com", "")
        )
        
        users.clear()
        users.addAll(sampleUsers)
        userAdapter.notifyDataSetChanged()
    }
}