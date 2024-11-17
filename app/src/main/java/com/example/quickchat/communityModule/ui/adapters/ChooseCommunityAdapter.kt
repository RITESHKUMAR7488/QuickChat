package com.example.quickchat.communityModule.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.quickchat.communityModule.models.CommunityModels
import com.example.quickchat.databinding.RvCommunityChildBinding

class ChooseCommunityAdapter(
    private val list: List<CommunityModels>, // List of community data
    private val context: Context,           // Context for layout inflation
    private val onCommunityClick: (String) -> Unit // Click listener to handle community clicks
) : RecyclerView.Adapter<ChooseCommunityAdapter.ViewHolder>() {

    // Creates and inflates the ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RvCommunityChildBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    // Returns the size of the community list
    override fun getItemCount(): Int {
        return list.size
    }

    // Binds data to the ViewHolder and sets up the click listener
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = list[position]

        // Set community name and description
        holder.binding.tvCommunityName.text = currentItem.communityName
        holder.binding.tvDescription.text = currentItem.communityDescription

        // Set a click listener on the root view (CardView in this case)
        holder.binding.root.setOnClickListener {
            // Trigger the click listener with the community's ID
            currentItem.communityId?.let { it1 -> onCommunityClick(it1) }
        }
    }

    // ViewHolder to hold references to the views for each item
    class ViewHolder(val binding: RvCommunityChildBinding) : RecyclerView.ViewHolder(binding.root)
}
