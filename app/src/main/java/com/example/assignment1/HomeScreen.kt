package com.example.assignment1

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment1.adapters.PostAdapter
import com.example.assignment1.models.Post
import com.example.assignment1.utils.Base64Image
import com.example.assignment1.utils.PostRepository
import com.example.assignment1.utils.PresenceManager
import com.example.assignment1.data.network.ApiClient
import com.example.assignment1.data.prefs.SessionManager
import com.example.assignment1.data.local.AppDatabase
import com.example.assignment1.data.local.entities.StoryEntity
import com.example.assignment1.data.local.entities.PostEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeScreen : AppCompatActivity() {
    private lateinit var sessionManager: SessionManager
    private lateinit var database: AppDatabase
    private lateinit var postRepository: PostRepository
    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private val posts = mutableListOf<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize session and database
        sessionManager = SessionManager(this)
        database = AppDatabase.getInstance(this)

        try {
        setContentView(R.layout.activity_home_screen)
        } catch (e: Exception) {
            // If layout fails, create a simple home screen programmatically
            createSimpleHomeScreen()
            return
        }
        
        try {
        PresenceManager.setOnline(this)
        } catch (e: Exception) {
            // Continue without presence
        }
        
        // Initialize post repository and adapter
        try {
        postRepository = PostRepository()
        setupPostsRecyclerView()
        } catch (e: Exception) {
            // If Firebase is not initialized, continue without posts
        }

        // Set padding for the main layout based on system bars (status bar, navigation bar)
        try {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
            }
        } catch (e: Exception) {
            // If findViewById fails, continue without window insets
        }

        // Retrieve the profile image URI from the intent
        try {
        val imageUriString = intent.getStringExtra("PROFILE_IMAGE_URI")
        val profileImageView = findViewById<ImageView>(R.id.profileImageView)
        val profileImageInFeed = findViewById<ImageButton>(R.id.tab_5)
        if (imageUriString != null) {
            val imageUri = Uri.parse(imageUriString)
            profileImageView.setImageURI(imageUri)
            profileImageInFeed.setImageURI(imageUri)
        } else {
            // 👇 show fallback placeholder
            profileImageView.setImageResource(R.drawable.ic_default_profile)
            profileImageInFeed.setImageResource(R.drawable.ic_default_profile)
        }

        // Retrieve and set the username to the TextView
        val username = intent.getStringExtra("USERNAME_KEY")
        val usernameTextView = findViewById<TextView>(R.id.usernameTextView)
        usernameTextView.text = username

        // Load and display stories from API with offline caching
            try {
        loadStoriesFromAPI()
            } catch (e: Exception) {
                // If API fails, load from cache
                loadStoriesFromCache()
            }

        // Set up the Search button to open the search screen
        val searchBtn = findViewById<ImageButton>(R.id.tab_2_search)
        searchBtn.setOnClickListener {
            val intentSearch = Intent(this, search::class.java)
            imageUriString?.let {
                val imageUri = Uri.parse(it)
                intentSearch.putExtra("PROFILE_IMAGE_URI", imageUri.toString()) // Pass URI as String
            }
            startActivity(intentSearch)
            overridePendingTransition(0, 0)
        }

        // Set up the Share button to open the message list screen
        val shareBtn = findViewById<ImageButton>(R.id.shareButton)
        shareBtn.setOnClickListener {
            val intentShare = Intent(this, messageList::class.java)
            startActivity(intentShare)
        }

        val storyOwnBtn = findViewById<ImageView>(R.id.profileImageView)
        val addStoryBtn = findViewById<ImageButton>(R.id.addStoryButton)

        // Simplified story logic: always allow upload from + button, decide view/upload for big circle after reload
        addStoryBtn.setOnClickListener {
            val intentStoryUpload = Intent(this, story_Upload::class.java)
            imageUriString?.let {
                val imageUri = Uri.parse(it)
                intentStoryUpload.putExtra("PROFILE_IMAGE_URI", imageUri.toString())
            }
            // Start for result so we can refresh stories when returning
            startActivityForResult(intentStoryUpload, 200)
            overridePendingTransition(0, 0)
        }

        // Initially (before stories load) tapping big circle uploads a story
        storyOwnBtn.setOnClickListener {
            val intentStoryUpload = Intent(this, story_Upload::class.java)
            imageUriString?.let {
                val imageUri = Uri.parse(it)
                intentStoryUpload.putExtra("PROFILE_IMAGE_URI", imageUri.toString())
            }
            startActivityForResult(intentStoryUpload, 200)
            overridePendingTransition(0, 0)
        }

        val UserStoryBtn = findViewById<ImageView>(R.id.UserStoryView)
        UserStoryBtn.setOnClickListener {
            val intentUserStory = Intent(this, UserStoryView::class.java)
            startActivity(intentUserStory)
            overridePendingTransition(0, 0)
        }

        val notificationBtn = findViewById<ImageButton>(R.id.tab_4_notification)
        notificationBtn.setOnClickListener {
            val intentnotification = Intent(this, notifications::class.java)
            startActivity(intentnotification)
            overridePendingTransition(0, 0)
        }

        val MyProfileBtn = findViewById<ImageButton>(R.id.tab_5)
        MyProfileBtn.setOnClickListener {
            val intentMyProfile = Intent(this, OwnProfile::class.java)
            imageUriString?.let {
                val imageUri = Uri.parse(it)
                intentMyProfile.putExtra("PROFILE_IMAGE_URI", imageUri.toString()) // Pass URI as String
            }
            startActivity(intentMyProfile)
            overridePendingTransition(0, 0)
        }

        // Set up the Plus button to open create post screen
        val plusBtn = findViewById<ImageButton>(R.id.tab_3_plus)
        plusBtn.setOnClickListener {
            val intentCreatePost = Intent(this, CreatePostActivity::class.java)
            startActivityForResult(intentCreatePost, 100)
        }

        // Load posts from API with offline caching
            try {
        loadPostsFromAPI()
            } catch (e: Exception) {
                // If API fails, load from cache
                loadPostsFromCache()
            }
        } catch (e: Exception) {
            // If any findViewById fails, continue without those features
        }
    }

    private fun setupPostsRecyclerView() {
        try {
        postsRecyclerView = findViewById(R.id.postsRecyclerView)
        postAdapter = PostAdapter(posts) { post ->
            // Handle comment click
            val intentComments = Intent(this, CommentsActivity::class.java)
            intentComments.putExtra("post", post)
            startActivity(intentComments)
        }
        postsRecyclerView.layoutManager = LinearLayoutManager(this)
        postsRecyclerView.adapter = postAdapter
        // Improve scroll smoothness when inside ScrollView
        postsRecyclerView.isNestedScrollingEnabled = false
        postsRecyclerView.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        postsRecyclerView.itemAnimator = null
        postsRecyclerView.setItemViewCacheSize(20)
        } catch (e: Exception) {
            // If RecyclerView setup fails, continue without posts
        }
    }

    private fun loadPostsFromAPI() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = ApiClient.getApiService(this@HomeScreen)
                val response = apiService.getFeedPosts(page = 1, limit = 20)
                
                if (response.status == "success" && response.data != null) {
                    // Cache posts in Room database
                    val postEntities = response.data.map { PostEntity.fromPost(it) }
                    database.postDao().deleteAllPosts() // Clear old posts
                    database.postDao().insertPosts(postEntities)
                    
                    // Convert API posts to legacy format for UI
                    val legacyPosts = response.data.map { it.toLegacyPost() }
                    
                    // Update UI
                    withContext(Dispatchers.Main) {
                        posts.clear()
                        posts.addAll(legacyPosts)
                        postAdapter.notifyDataSetChanged()
                    }
                } else {
                    // Load from cache if API fails
                    loadPostsFromCache()
                }
            } catch (e: Exception) {
                // Load from cache on error
                withContext(Dispatchers.Main) {
                    loadPostsFromCache()
                }
            }
        }
    }
    
    private fun loadPostsFromCache() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val postEntities = database.postDao().getPosts(limit = 20, offset = 0)
                val legacyPosts = postEntities.map { it.toPost().toLegacyPost() }
                
                withContext(Dispatchers.Main) {
                    if (legacyPosts.isEmpty()) {
                        Toast.makeText(this@HomeScreen, "No posts available offline", Toast.LENGTH_SHORT).show()
                    } else {
                        posts.clear()
                        posts.addAll(legacyPosts)
                        postAdapter.notifyDataSetChanged()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HomeScreen, "Failed to load posts", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            loadPostsFromAPI()
        }
        if (requestCode == 200 && resultCode == RESULT_OK) {
            // Story uploaded, reload stories
            loadStoriesFromAPI()
        }
    }

    override fun onStart() {
        super.onStart()
        try {
        PresenceManager.setOnline(this)
        } catch (e: Exception) {
            // Continue without presence
        }
    }

    override fun onStop() {
        super.onStop()
        try {
        PresenceManager.setOffline(this)
        } catch (e: Exception) {
            // Continue without presence
        }
    }

    private fun loadStoriesFromAPI() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = ApiClient.getApiService(this@HomeScreen)
                val response = apiService.getStories()
                
                if (response.status == "success" && response.data != null) {
                    // Cache stories in Room database
                    val storyEntities = response.data.map { StoryEntity.fromStory(it) }
                    database.storyDao().deleteAllStories() // Clear old stories
                    database.storyDao().insertStories(storyEntities)
                    
                    // Convert API stories to legacy format for UI
                    val legacyStories = response.data.map { it.toLegacyStory() }
                    
                    // Update UI
                    withContext(Dispatchers.Main) {
                        updateStoriesUI(legacyStories)
                    }
                } else {
                    // Load from cache if API fails
                    loadStoriesFromCache()
                }
            } catch (e: Exception) {
                // Load from cache on error
                withContext(Dispatchers.Main) {
                    loadStoriesFromCache()
                }
            }
        }
    }
    
    private fun loadStoriesFromCache() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val storyEntities = database.storyDao().getActiveStories()
                val stories = storyEntities.map { it.toStory().toLegacyStory() }
                
                withContext(Dispatchers.Main) {
                    if (stories.isEmpty()) {
                        Toast.makeText(this@HomeScreen, "No stories available offline", Toast.LENGTH_SHORT).show()
                    } else {
                        updateStoriesUI(stories)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HomeScreen, "Failed to load stories", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun loadAllStories(currentTime: Long) {
        // Deprecated - now using API
        loadStoriesFromAPI()
    }
    
    private fun updateStoriesUI(stories: List<com.example.assignment1.models.Story>) {
        try {
            if (stories.isEmpty()) {
                return
            }
            
            // Populate the first story bubble with current user's story if available
            val currentUserId = sessionManager.getUserId()
            val myStory = stories.firstOrNull { it.userId == currentUserId }
            
            try {
                val profileImageView = findViewById<ImageView>(R.id.profileImageView)
                val usernameTextView = findViewById<TextView>(R.id.usernameTextView)
                if (myStory != null && !myStory.userProfileImage.isNullOrEmpty()) {
                    val bitmap = Base64Image.base64ToBitmap(myStory.userProfileImage)
                    profileImageView.setImageBitmap(bitmap)
                    usernameTextView.text = "Your story"
                    profileImageView.setOnClickListener {
                        val intent = Intent(this, storyViewOwn::class.java)
                        startActivity(intent)
                        overridePendingTransition(0, 0)
                    }
                } else {
                    // No active story yet – big circle allows upload
                    usernameTextView.text = "Add story"
                    profileImageView.setOnClickListener {
                        val intentStoryUpload = Intent(this, story_Upload::class.java)
                        startActivityForResult(intentStoryUpload, 200)
                        overridePendingTransition(0, 0)
                    }
                }
            } catch (e: Exception) {
                // Continue if view not found
            }
            
            // Populate additional story bubbles (story2-story6) with other users' stories
            val otherStories = stories.filter { it.userId != currentUserId }.take(5)
            val storyViews = listOf(
                Triple(R.id.story2, "Ayesha", R.drawable.ic_default_profile),
                Triple(R.id.story3, "Danny", R.drawable.danny_rob),
                Triple(R.id.story4, "Sofia", R.drawable.sofia_martinez),
                Triple(R.id.story5, "Hannah", R.drawable.hannah_lee),
                Triple(R.id.story6, "Omar", R.drawable.omar_sheikh)
            )
            
            otherStories.forEachIndexed { index, story ->
                if (index < storyViews.size) {
                    try {
                        val (storyLayoutId, _, _) = storyViews[index]
                        val storyLayout = findViewById<LinearLayout>(storyLayoutId)
                        
                        // Find the ImageView inside the story layout
                        val storyImageView = storyLayout.findViewById<ImageView>(
                            resources.getIdentifier("UserStoryView", "id", packageName)
                        )
                        val storyUsername = storyLayout.findViewById<TextView>(
                            resources.getIdentifier("usernameTextView", "id", packageName)
                        )
                        
                        // Set story image from Firebase
                        if (storyImageView != null && !story.userProfileImage.isNullOrEmpty()) {
                            val bitmap = Base64Image.base64ToBitmap(story.userProfileImage)
                            storyImageView.setImageBitmap(bitmap)
                        }
                        
                        // Set username
                        if (storyUsername != null) {
                            storyUsername.text = story.username
                        }
                        
                        // Set click listener to open story viewer
                        storyLayout.setOnClickListener {
                            val intent = Intent(this, UserStoryView::class.java)
                            intent.putExtra("STORY_USER_ID", story.userId)
                            intent.putExtra("STORY_USERNAME", story.username)
                            startActivity(intent)
                        }
                    } catch (e: Exception) {
                        // Continue if view not found
                    }
                }
            }
            
            if (stories.isNotEmpty()) {
                Toast.makeText(this, "Loaded ${stories.size} active stories", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            // If story UI update fails, continue without stories
            android.util.Log.e("HomeScreen", "Error displaying stories: ${e.message}", e)
        }
    }
    
    private fun checkUserStoryStatus(callback: (Boolean) -> Unit) {
        try {
            val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            if (currentUserId == null) {
                callback(false)
                return
            }
            
            val currentTime = System.currentTimeMillis()
            database.reference.child("stories")
                .orderByChild("userId")
                .equalTo(currentUserId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var hasActiveStory = false
                        for (storySnapshot in snapshot.children) {
                            val expiresAt = storySnapshot.child("expiresAt").getValue(Long::class.java) ?: 0
                            if (expiresAt > currentTime) {
                                hasActiveStory = true
                                break
                            }
                        }
                        callback(hasActiveStory)
                    }
                    
                    override fun onCancelled(error: DatabaseError) {
                        callback(false)
                    }
                })
        } catch (e: Exception) {
            callback(false)
        }
    }
    
    private fun cleanupExpiredStories(currentTime: Long) {
        try {
            // Remove expired stories from Firebase
            database.reference.child("stories")
                .orderByChild("expiresAt")
                .endAt(currentTime.toDouble())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val expiredStoryIds = mutableListOf<String>()
                        for (storySnapshot in snapshot.children) {
                            expiredStoryIds.add(storySnapshot.key ?: "")
                        }
                        
                        // Remove expired stories
                        expiredStoryIds.forEach { storyId ->
                            database.reference.child("stories").child(storyId).removeValue()
                        }
                    }
                    
                    override fun onCancelled(error: DatabaseError) {
                        // Handle error silently
                    }
                })
        } catch (e: Exception) {
            // If Firebase is not initialized, continue without cleanup
        }
    }
    
    private fun createSimpleHomeScreen() {
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setBackgroundColor(android.graphics.Color.WHITE)
            setPadding(50, 50, 50, 50)
        }
        
        val welcomeText = android.widget.TextView(this).apply {
            text = "Welcome to Socially!"
            textSize = 32f
            setTextColor(android.graphics.Color.parseColor("#784A34"))
            gravity = android.view.Gravity.CENTER
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 50
            }
        }
        
        val statusText = android.widget.TextView(this).apply {
            text = "Home Screen - Demo Mode"
            textSize = 18f
            setTextColor(android.graphics.Color.GRAY)
            gravity = android.view.Gravity.CENTER
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 30
            }
        }
        
        val logoutButton = android.widget.Button(this).apply {
            text = "Logout"
            setBackgroundColor(android.graphics.Color.parseColor("#784A34"))
            setTextColor(android.graphics.Color.WHITE)
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 20
            }
            setOnClickListener {
                val intent = Intent(this@HomeScreen, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        
        layout.addView(welcomeText)
        layout.addView(statusText)
        layout.addView(logoutButton)
        setContentView(layout)
    }
}