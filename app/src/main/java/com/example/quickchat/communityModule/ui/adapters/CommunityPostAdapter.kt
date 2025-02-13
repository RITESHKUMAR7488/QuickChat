package com.example.quickchat.communityModule.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = list[position]
        val postViewHolder = holder as PostViewHolder
        postViewHolder.binding.tvUsername.text = currentItem.detailModel?.firstname
        postViewHolder.binding.tvDescription.text = currentItem.description
    }

    class PostViewHolder(val binding: RvHomeChildBinding) : RecyclerView.ViewHolder(binding.root)
}