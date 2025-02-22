package com.example.quickchat.mainModule.ui.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.quickchat.communityModule.ui.activity.CommunityDetail
import com.example.quickchat.databinding.RvCommunityChildBinding
import com.example.quickchat.mainModule.models.AllCommunityModel

class GetAllCommunityAdapter(
    private val list: List<AllCommunityModel>,
    private val context: Context
) : RecyclerView.Adapter<GetAllCommunityAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GetAllCommunityAdapter.ViewHolder {
        val binding =
            RvCommunityChildBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)

    }

    override fun onBindViewHolder(holder: GetAllCommunityAdapter.ViewHolder, position: Int) {
        val currentItem = list[position]
        holder.binding.tvCommunityName.text = currentItem.communityName
        holder.binding.tvDescription.text = currentItem.communityDescription
        Glide.with(context).load(currentItem.imageUrl).into(holder.binding.profileImage)
        holder.binding.item.setOnClickListener {
            val intent = Intent(context, CommunityDetail::class.java)
            intent.putExtra("communityId", currentItem.communityId)
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(val binding: RvCommunityChildBinding) : RecyclerView.ViewHolder(binding.root)

}