package com.example.quickchat.mainModule.ui.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.quickchat.communityModule.ui.activity.CommunityDetail
import com.example.quickchat.databinding.RvCommunityChildBinding
import com.example.quickchat.databinding.RvHomeProfileChildBinding
import com.example.quickchat.mainModule.models.AllCommunityModel

class PostCommunityAdapter(
    private val list: List<AllCommunityModel>,
    private val context: Context
):RecyclerView.Adapter<PostCommunityAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding=RvHomeProfileChildBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem=list[position]
        holder.binding.tvUsername.text=currentItem.communityName
        holder.binding.item.setOnClickListener {
            val intent= Intent(context, CommunityDetail::class.java)
            intent.putExtra("communityId",currentItem.communityId)
            context.startActivity(intent)

        }

    }

    override fun getItemCount(): Int {
        return list.size
    }
    class ViewHolder(val binding: RvHomeProfileChildBinding):RecyclerView.ViewHolder(binding.root)

}