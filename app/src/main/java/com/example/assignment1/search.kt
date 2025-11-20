package com.example.assignment1

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment1.adapters.UserAdapter
import com.example.assignment1.models.User
import com.example.assignment1.utils.FirebaseAuthManager
import com.example.assignment1.utils.FollowManager
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

class search : AppCompatActivity() {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val authManager = FirebaseAuthManager()
    private lateinit var followManager: FollowManager
    private lateinit var searchResultsLayout: LinearLayout
    private lateinit var searchInput: EditText
    private lateinit var userAdapter: UserAdapter
    private val searchResults = mutableListOf<User>()
    private var currentFilter = "all" // "all", "followers", "following"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        try {
            followManager = FollowManager()
        } catch (e: Exception) {
            // If Firebase fails to initialize, continue without it
        }
        
        setupUI()
        setupBottomNavigation()
    }
    
    private fun setupUI() {
        searchInput = findViewById(R.id.search_input)
        searchResultsLayout = findViewById(R.id.search_results)
        
        // Setup search functionality
        searchInput.setOnEditorActionListener { _, _, _ ->
            val query = searchInput.text.toString().trim()
            if (query.isNotEmpty()) {
                searchUsers(query)
            }
            true
        }
    }
    
    private fun setupBottomNavigation() {
        try {
            // Home tab
            findViewById<ImageButton>(R.id.tab_1)?.setOnClickListener {
                finish()
            }
            
            // Search tab (current page)
            findViewById<ImageButton>(R.id.tab_2_search)?.setOnClickListener {
                // Already on search page
            }
            
            // Plus tab (create post)
            findViewById<ImageButton>(R.id.tab_3_plus)?.setOnClickListener {
                startActivity(Intent(this, CreatePostActivity::class.java))
            }
            
            // Notifications tab
            findViewById<ImageButton>(R.id.tab_4_notification)?.setOnClickListener {
                startActivity(Intent(this, notifications::class.java))
            }
            
            // Profile tab
            findViewById<ImageButton>(R.id.tab_5)?.setOnClickListener {
                startActivity(Intent(this, OwnProfile::class.java))
            }
        } catch (e: Exception) {
            // Continue without bottom navigation if views not found
        }
    }
    
    private fun searchUsers(query: String) {
        try {
            // Clear previous results from UI
            searchResultsLayout.removeAllViews()
            
            val currentUser = authManager.getCurrentUser()
            if (currentUser != null) {
                when (currentFilter) {
                    "followers" -> searchInFollowers(query, currentUser.userId)
                    "following" -> searchInFollowing(query, currentUser.userId)
                    else -> searchAllUsers(query)
                }
            } else {
                searchAllUsers(query)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Search error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun searchAllUsers(query: String) {
        try {
            // Fetch all users, then perform case-insensitive contains filtering locally
            database.reference.child("users")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val filtered = mutableListOf<User>()
                        for (userSnapshot in snapshot.children) {
                            val user = try { userSnapshot.getValue(User::class.java) } catch (e: Exception) { null }
                            if (user != null && user.username.contains(query, ignoreCase = true)) {
                                filtered.add(user)
                            }
                        }
                        runOnUiThread {
                            displaySearchResults(filtered)
                            Toast.makeText(this@search, "Found ${filtered.size} users", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        runOnUiThread { Toast.makeText(this@search, "Search failed", Toast.LENGTH_SHORT).show() }
                    }
                })
        } catch (e: Exception) {
            Toast.makeText(this, "Search error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun displaySearchResults(users: List<User>) {
        searchResultsLayout.removeAllViews()
        
        for (user in users) {
            val userView = layoutInflater.inflate(R.layout.search_user_row, searchResultsLayout, false)
            // Setup user view with data
            userView.setOnClickListener {
                // Open user profile
                val intent = Intent(this, UserProfile::class.java)
                intent.putExtra("USER_ID", user.userId)
                startActivity(intent)
            }
            searchResultsLayout.addView(userView)
        }
    }
    
    private fun searchInFollowers(query: String, userId: String) {
        try {
            followManager.getFollowers(userId) { followers ->
                val filteredFollowers = followers.filter { 
                    it.username.contains(query, ignoreCase = true) 
                }
                runOnUiThread {
                    displaySearchResults(filteredFollowers)
                    Toast.makeText(this@search, "Found ${filteredFollowers.size} followers", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error searching followers", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun searchInFollowing(query: String, userId: String) {
        try {
            followManager.getFollowing(userId) { following ->
                val filteredFollowing = following.filter { 
                    it.username.contains(query, ignoreCase = true) 
                }
                runOnUiThread {
                    displaySearchResults(filteredFollowing)
                    Toast.makeText(this@search, "Found ${filteredFollowing.size} following", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error searching following", Toast.LENGTH_SHORT).show()
        }
    }
}