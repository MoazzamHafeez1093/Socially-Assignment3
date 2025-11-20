package com.example.assignment1.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment1.R
import com.example.assignment1.models.Story
import com.example.assignment1.utils.Base64Image

class StoryAdapter(
    private val stories: List<Story>,
    private val onStoryClick: (Story) -> Unit
) : RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {

    class StoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val storyImage: ImageView = view.findViewById(R.id.storyImageView)
        val username: TextView = view.findViewById(R.id.storyUsername)
        val profileImage: ImageView = view.findViewById(R.id.storyProfileImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.story_item, parent, false)
        return StoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = stories[position]
        
        // Set username
        holder.username.text = story.username
        
        // Set story image from Base64
        if (story.imageUrl.isNotEmpty()) {
            try {
                val bitmap = Base64Image.base64ToBitmap(story.imageUrl)
                holder.storyImage.setImageBitmap(bitmap)
            } catch (e: Exception) {
                holder.storyImage.setImageResource(R.drawable.ic_default_profile)
            }
        } else {
            holder.storyImage.setImageResource(R.drawable.ic_default_profile)
        }
        
        // Set profile image from Base64
        if (story.userProfileImage.isNotEmpty()) {
            try {
                val bitmap = Base64Image.base64ToBitmap(story.userProfileImage)
                holder.profileImage.setImageBitmap(bitmap)
            } catch (e: Exception) {
                holder.profileImage.setImageResource(R.drawable.ic_default_profile)
            }
        } else {
            holder.profileImage.setImageResource(R.drawable.ic_default_profile)
        }
        
        // Click listener
        holder.itemView.setOnClickListener {
            onStoryClick(story)
        }
    }

    override fun getItemCount(): Int = stories.size
}

