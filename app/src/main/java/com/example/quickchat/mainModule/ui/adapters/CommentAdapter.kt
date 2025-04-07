package com.example.quickchat.mainModule.ui.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.quickchat.R
import com.example.quickchat.databinding.ItemCommentBinding
import com.example.quickchat.mainModule.models.CommentModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class CommentAdapter(
    private val currentUserId: String,
    private val onLikeComment: (CommentModel) -> Unit,
    private val onUnlikeComment: (CommentModel) -> Unit
) : ListAdapter<CommentModel, CommentAdapter.CommentViewHolder>(CommentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = getItem(position)
        holder.bind(comment)
    }

    inner class CommentViewHolder(private val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: CommentModel) {
            binding.comment = comment
            binding.tvCommentUsername.text = comment.role ?: "User"
            binding.tvCommentText.text = comment.text

            // Format timestamp to relative time
            comment.timestamp?.let {
                binding.tvCommentTime.text = getTimeAgo(it)
            }

            // Set likes count
            val likesCount = comment.likes ?: 0
            binding.tvCommentLikes.text = if (likesCount > 0) likesCount.toString() else ""

            // Load user profile image
            Glide.with(binding.root.context)
                .load(comment.imageUrl)
                .placeholder(R.drawable.user)
                .error(R.drawable.user)
                .into(binding.ivCommentUserProfile)

            // Handle like button click
            binding.btnLikeComment.setOnClickListener {
                if ((comment.likes ?: 0) > 0) {
                    onUnlikeComment(comment)
                } else {
                    onLikeComment(comment)
                }
            }

            // Set like button state
            val likeDrawable = if ((comment.likes ?: 0) > 0) {
                R.drawable.liked
            } else {
                R.drawable.like
            }
            binding.btnLikeComment.setImageResource(likeDrawable)

            binding.executePendingBindings()
        }

        private fun getTimeAgo(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp

            return when {
                diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
                diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)}m"
                diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)}h"
                diff < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(diff)}d"
                else -> {
                    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                    dateFormat.format(Date(timestamp))
                }
            }
        }
    }

    class CommentDiffCallback : DiffUtil.ItemCallback<CommentModel>() {
        override fun areItemsTheSame(oldItem: CommentModel, newItem: CommentModel): Boolean {
            return oldItem.commentId == newItem.commentId
        }

        override fun areContentsTheSame(oldItem: CommentModel, newItem: CommentModel): Boolean {
            return oldItem == newItem
        }
    }
}