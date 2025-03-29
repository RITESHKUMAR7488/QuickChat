package com.example.quickchat.mainModule.ui.adapters


import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.quickchat.databinding.ItemUserBinding
import com.example.quickchat.onboardingModule.models.UserModel

class UserAdapter(
    private val list: List<UserModel>,
    private val context: Context,
    private val onClick: (UserModel) -> Unit
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentUser = list[position]

        // Set user name
        holder.binding.tvUsername.text = "${currentUser.firstName} ${currentUser.lastName}"

        // Load user profile image (fallback to default image)
        Glide.with(holder.binding.root.context)
            .load(currentUser.imageUrl ?: "https://bit.ly/2TIt8NR")
            .into(holder.binding.ivProfile)

        // Click listener for starting chat
        holder.binding.root.setOnClickListener {
            onClick(currentUser)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root)
}
