package com.example.quickchat.communityModule.ui.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.quickchat.databinding.RvHomeChildBinding
import com.example.quickchat.mainModule.models.PostModel

class CommunityPostAdapter(val list: List<PostModel>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
       return PostViewHolder(
            RvHomeChildBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }


    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("CheckResult")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = list[position]
        val postViewHolder = holder as PostViewHolder
        postViewHolder.binding.tvUsername.text = currentItem.detailModel?.firstname
        postViewHolder.binding.tvDescription.text = currentItem.description
        postViewHolder.binding.tvTitle.text = currentItem.title
        Log.d("imagessssss", "onBindViewHolder: ${currentItem.imageUrl}")
        Glide.with(postViewHolder.binding.root.context).load(currentItem.imageUrl.toString()).into(holder.binding.postImage)
    }

    class PostViewHolder(val binding: RvHomeChildBinding) : RecyclerView.ViewHolder(binding.root)
}