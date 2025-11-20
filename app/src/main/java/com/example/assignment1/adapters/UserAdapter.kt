package com.example.assignment1.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment1.R
import com.example.assignment1.models.User

class UserAdapter(
    private val users: List<User>,
    private val onUserClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameText: TextView = itemView.findViewById(R.id.usernameText)
        val userStatusText: TextView = itemView.findViewById(R.id.userStatusText) // acts as last message preview
        val messageTimeText: TextView? = itemView.findViewById(R.id.messageTimeText)
        val profileImageView: ImageView = itemView.findViewById(R.id.profileImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        
    holder.usernameText.text = user.username
    // Placeholder last-message preview and time until chat data is available
    holder.userStatusText.text = "Have a nice day, bro!" // TODO: bind real last message
    holder.messageTimeText?.text = "· now"
        
        // Set default profile image
        holder.profileImageView.setImageResource(R.drawable.placeholder_image)
        
        holder.itemView.setOnClickListener {
            onUserClick(user)
        }
    }

    override fun getItemCount(): Int = users.size
}
