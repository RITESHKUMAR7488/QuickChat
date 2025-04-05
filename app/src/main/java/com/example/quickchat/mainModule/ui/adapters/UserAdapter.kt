package com.example.quickchat.mainModule.ui.adapters


import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.quickchat.databinding.ItemUserBinding
import com.example.quickchat.mainModule.models.ChatModel
import com.example.quickchat.onboardingModule.models.UserModel

class UserAdapter(private var users: List<ChatModel>, private val context: Context, private val onUserClick: (ChatModel) -> Unit) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    fun updateData(newUsers: List<ChatModel>) {
        users = newUsers
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int = users.size

    inner class UserViewHolder(private val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: ChatModel) {
            binding.tvUsername.text = user.name
            Glide.with(binding.ivProfile.context).load(user.image).into(binding.ivProfile)

            binding.root.setOnClickListener {
                onUserClick(user)
            }
        }
    }
}
