package com.example.quickchat.mainModule.ui.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.quickchat.R
import com.example.quickchat.communityModule.models.CommunityModels
import com.example.quickchat.communityModule.ui.activity.CommunityDetail
import com.example.quickchat.constants.Constant
import com.example.quickchat.databinding.ItemCommunityRecylerviewBinding
import com.example.quickchat.databinding.RvHomeChildBinding
import com.example.quickchat.databinding.RvHomeProfileChildBinding
import com.example.quickchat.mainModule.models.AllCommunityModel
import com.example.quickchat.mainModule.models.MainPostModel
import com.example.quickchat.mainModule.models.PostModel
import com.example.quickchat.mainModule.ui.activity.CommentActivity
import com.example.quickchat.onboardingModule.models.UserModel

class GetAllPostAdapter(
    private val items: List<MainPostModel>,
    private val context: Context,
    private val onLikeClickListener: (PostModel) -> Unit // Add a callback for like clicks
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_ONE = 1  // Single Post
        private const val VIEW_TYPE_TWO = 2  // Single Community
        private const val VIEW_TYPE_COMMUNITY_CHUNK = 3 // Community Chunk (Horizontal RecyclerView)
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is MainPostModel.TypeOneItem -> VIEW_TYPE_ONE
            is MainPostModel.TypeTwoItem -> VIEW_TYPE_TWO
            is MainPostModel.CommunityChunk -> VIEW_TYPE_COMMUNITY_CHUNK
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_ONE -> {
                val binding = RvHomeChildBinding.inflate(inflater, parent, false)
                TypeOneViewHolder(binding)
            }

            VIEW_TYPE_TWO -> {
                val binding = RvHomeProfileChildBinding.inflate(inflater, parent, false)
                TypeTwoViewHolder(binding)
            }

            VIEW_TYPE_COMMUNITY_CHUNK -> {
                val binding = ItemCommunityRecylerviewBinding.inflate(inflater, parent, false)
                CommunityChunkViewHolder(binding, context)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is MainPostModel.TypeOneItem -> (holder as TypeOneViewHolder).bind(item.data)
            is MainPostModel.TypeTwoItem -> (holder as TypeTwoViewHolder).bind(item.data)
            is MainPostModel.CommunityChunk -> (holder as CommunityChunkViewHolder).bind(item.data)
        }
    }

    override fun getItemCount(): Int = items.size

    // ViewHolder for Posts
    inner class TypeOneViewHolder(private val binding: RvHomeChildBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PostModel) {
            binding.tvUsername.text = item.detailModel?.firstname.toString()
            binding.tvDescription.text = item.description
            binding.tvTitle.text = item.title
            Glide.with(binding.root.context).load(item.userModels?.firstOrNull()?.imageUrl.toString()).into(binding.imageView2)

            // Set initial like state
            val isLiked = item.likes?.isNotEmpty() ?: false
            updateLikeUI(isLiked, item.likes?.size ?: 0)

            // Handle like button click
            binding.like.setOnClickListener {
                // Toggle the like state
                val updatedLikes = item.likes?.toMutableList() ?: mutableListOf()
                if (isLiked) {
                    updatedLikes.removeAll { true } // Remove all likes (simplified for example)
                } else {
                    updatedLikes.add("user_id") // Add the current user's ID (replace with actual ID)
                }

                // Update the item's likes
                item.likes = updatedLikes

                // Notify the adapter of the change
                notifyItemChanged(adapterPosition)

                // Trigger the like click listener
                onLikeClickListener(item)
            }

            // Add comment button click listener
            binding.comment.setOnClickListener {
                val intent = Intent(context, CommentActivity::class.java)
                intent.putExtra("postId", item.postId)
                context.startActivity(intent)
            }

            // Load post image (if available)
            if (item.imageUrl == null) {
                binding.postImage.visibility = View.GONE
            } else {
                binding.postImage.visibility = View.VISIBLE
                Glide.with(binding.root.context).load(item.imageUrl.toString())
                    .into(binding.postImage)
            }
        }

        private fun updateLikeUI(isLiked: Boolean, likeCount: Int) {
            // Update like icon
            val likeIcon = if (isLiked) R.drawable.liked else R.drawable.like
            binding.like.setImageResource(likeIcon)

            // Update like count text
            binding.NumberOfLikes.text = if (likeCount > 0) likeCount.toString() else "Like"
        }
    }

    // ViewHolder for Single Community Item (Unchanged)
    class TypeTwoViewHolder(private val binding: RvHomeProfileChildBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CommunityModels) {
            binding.tvUsername.text = item.communityName
            Log.d("communityimagesssssss", "onBindViewHolder: ${item.communityName}")
            Glide.with(binding.root.context).load(item.imageUrl.toString()).into(binding.ivProfile)
        }
    }

    // ViewHolder for Community Chunks (Unchanged)
    class CommunityChunkViewHolder(
        private val binding: ItemCommunityRecylerviewBinding,
        private val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(communities: List<AllCommunityModel>) {
            val adapter = PostCommunityAdapter(communities, context)
            binding.communityRecyclerView.adapter = adapter
        }
    }
}