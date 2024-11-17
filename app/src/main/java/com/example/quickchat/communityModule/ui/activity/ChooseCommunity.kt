package com.example.quickchat.communityModule.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.example.quickchat.R
import com.example.quickchat.communityModule.models.CommunityModels
import com.example.quickchat.communityModule.ui.adapters.ChooseCommunityAdapter
import com.example.quickchat.communityModule.viemodels.CommunityViewModel
import com.example.quickchat.databinding.ActivityChooseCommunityBinding
import com.example.quickchat.mainModule.ui.activity.PostActivity
import com.example.quickchat.utility.BaseActivity
import com.example.quickchat.utility.UiState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChooseCommunity : BaseActivity() {

    // Binding object for the layout
    private lateinit var binding: ActivityChooseCommunityBinding

    // ViewModel for managing community data
    private val communityViewModel: CommunityViewModel by viewModels()

    // Adapter for the RecyclerView
    private lateinit var adapter: ChooseCommunityAdapter

    // List to store community data
    private lateinit var communityList: List<CommunityModels>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display for a modern UI look
        enableEdgeToEdge()

        // Initialize the binding object
        binding = DataBindingUtil.setContentView(this, R.layout.activity_choose_community)

        // Fetch and display the list of communities
        fetchAndDisplayCommunities()
    }

    /**
     * Fetches the list of communities from the ViewModel and observes the result.
     */
    private fun fetchAndDisplayCommunities() {
        // Initialize an empty list
        communityList = ArrayList()

        // Observe the ViewModel's community data
        communityViewModel.getCommunity(preferenceManager.userId.toString()).observe(this, Observer {
            when (it) {
                is UiState.Loading -> {
                    // Handle loading state if necessary (e.g., show a progress bar)
                }
                is UiState.Failure -> {
                    // Show error message
                    commonUtil.showToast(it.error)
                }
                is UiState.Success -> {
                    if (it.data.isEmpty()) {
                        // Show message if no communities are found
                        commonUtil.showToast("No community found")
                    } else {
                        // Update the community list and notify the adapter
                        communityList = it.data
                        commonUtil.showToast("Communities found")
                        setupRecyclerView()
                    }
                }
            }
        })
    }

    /**
     * Sets up the RecyclerView with the adapter and click listener.
     */
    private fun setupRecyclerView() {
        // Initialize the adapter and handle item clicks
        adapter = ChooseCommunityAdapter(communityList, this) { communityId ->
            // Navigate to the CommunityDetailsActivity when a community is clicked
            val intent = Intent(this, PostActivity::class.java)
            intent.putExtra("COMMUNITY_ID", communityId) // Pass communityId to the next activity
            startActivity(intent)
        }

        // Attach the adapter to the RecyclerView
        binding.rvChooseCommunity.adapter = adapter
    }
}
