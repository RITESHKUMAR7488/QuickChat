package com.example.quickchat.communityModule.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.example.quickchat.R
import com.example.quickchat.communityModule.models.CommunityModels
import com.example.quickchat.communityModule.viemodels.CommunityViewModel
import com.example.quickchat.databinding.ActivityUpdateCommunityBinding
import com.example.quickchat.mainModule.ui.activity.UserDetailActivity
import com.example.quickchat.utility.BaseActivity
import com.example.quickchat.utility.UiState
import dagger.hilt.android.AndroidEntryPoint

// Annotation to enable Hilt dependency injection for this activity
@AndroidEntryPoint
class UpdateCommunity : BaseActivity() {
    // Binding object for the activity's layout
    private lateinit var binding: ActivityUpdateCommunityBinding

    // ViewModel for community-related operations
    private val communityViewModel: CommunityViewModel by viewModels()

    // Variables to store community ID, user ID, and community model
    lateinit var communityId: String
    lateinit var userId: String
    lateinit var model: CommunityModels

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge display for the activity
        enableEdgeToEdge()

        // Initialize the binding object and set the content view
        binding = DataBindingUtil.setContentView(this, R.layout.activity_update_community)

        // Use the binding object to access views and set up the activity
        with(binding) {
            // Retrieve the community ID from the intent extras
            communityId = intent.getStringExtra("COMMUNITY_ID").toString()

            // Retrieve the user ID from the preference manager
            userId = preferenceManager.userModel?.uid.toString()

            // Fetch the community details using the community ID
            fetchCommunityDetails()

            // Set a click listener for the update button
            UpdateCommunityButton.setOnClickListener {
                updateCommunityData()
            }
        }
    }

    // Function to fetch community details from the ViewModel
    private fun fetchCommunityDetails() {
        // Observe the community details LiveData from the ViewModel
        communityViewModel.getCommunityDetails(communityId).observe(this) {
            when (it) {
                // Handle failure state
                is UiState.Failure -> {}
                // Handle loading state
                UiState.Loading -> {}
                // Handle success state
                is UiState.Success -> {
                    // Store the retrieved community model
                    model = it.data

                    // Populate the input fields with the retrieved data
                    binding.nameInput.setText(it.data.communityName)
                    binding.descriptionInput.setText(it.data.communityDescription)
                    binding.emailInput.setText(it.data.email)
                    binding.mobileInput.setText(it.data.mobileNumber)
                    binding.addressInput.setText(it.data.address)
                }
            }
        }
    }

    // Function to update the community data
    private fun updateCommunityData() {
        // Retrieve the input values from the UI
        val name = binding.nameInput.text.toString()
        val description = binding.descriptionInput.text.toString()
        val email = binding.emailInput.text.toString()
        val mobile = binding.mobileInput.text.toString()
        val address = binding.addressInput.text.toString()

        // Validate the input fields
        if (name.isBlank()) {
            binding.nameInput.error = "Please enter name"
        } else if (description.isBlank()) {
            binding.descriptionInput.error = "Please enter description"
        } else if (email.isBlank()) {
            binding.emailInput.error = "Please enter email"
        } else if (mobile.isBlank()) {
            binding.mobileInput.error = "Please enter mobile number"
        } else if (address.isBlank()) {
            binding.addressInput.error = "Please enter address"
        } else {
            // Update the community model with the new values
            model.apply {
                communityName = name
                communityDescription = description
                this.email = email
                mobileNumber = mobile
                this.address = address
            }

            // Log the updated model for debugging purposes
            Log.d("statesdddddd", model.toString())

            // Call the ViewModel to update the community data
            communityViewModel.updateCommunity(userId, communityId, model).observe(this) {
                when (it) {
                    // Handle failure state
                    is UiState.Failure -> {}
                    // Handle loading state
                    UiState.Loading -> {}
                    // Handle success state
                    is UiState.Success -> {
                        // Navigate back to the previous screen
                        onBackPressedDispatcher.onBackPressed()
                        finish()
                    }
                }
            }
        }
    }
}