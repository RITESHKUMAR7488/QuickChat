package com.example.quickchat.mainModule.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.quickchat.R
import com.example.quickchat.mainModule.models.PostModel
import com.example.quickchat.mainModule.viewmodels.PostViewModel
import com.example.quickchat.utility.BaseActivity
import com.example.quickchat.utility.PreferenceManager
import com.example.quickchat.utility.UiState
import com.example.quickchat.databinding.ActivityPostBinding
import com.example.quickchat.mainModule.models.DetailModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PostActivity : BaseActivity() {

    // View binding for the activity layout
    private lateinit var binding: ActivityPostBinding

    // ViewModel for managing posts
    private val postViewModel: PostViewModel by viewModels()

    // Community ID passed from ChooseCommunity activity
    private var communityId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        enableEdgeToEdge()

        // Initialize the binding object
        binding = DataBindingUtil.setContentView(this, R.layout.activity_post)

        // Initialize PreferenceManager
        preferenceManager = PreferenceManager(this)

        // Retrieve the communityId passed via Intent
        communityId = intent.getStringExtra("COMMUNITY_ID")

        // Log the received communityId for debugging
        Log.d("PostActivity", "Received Community ID: $communityId")

        // Check if communityId is null or invalid
        if (communityId.isNullOrEmpty()) {
            commonUtil.showToast("Invalid community selected.")
            finish() // Close the activity if no valid communityId is found
            return
        }

        // Set up UI and click listeners
        setupListeners()
    }

    /**
     * Sets up button click listeners and UI interactions.
     */
    private fun setupListeners() {
        with(binding) {
            // Handle the post creation button click
            btnPost.setOnClickListener {
                validateAndCreatePost()
            }
        }
    }

    /**
     * Validates the input fields and creates a new post.
     */
    private fun validateAndCreatePost() {
        with(binding) {
            // Get input values
            val title = etTitle.text.toString().trim()
            val description = etDescription.text.toString().trim()

            // Validate title
            if (title.isEmpty()) {
                etTitle.error = "Please enter a post title"
                commonUtil.showToast("Please enter a post title")
                return
            }

            // Validate description
            if (description.isEmpty()) {
                etDescription.error = "Please enter a post description"
                commonUtil.showToast("Please enter a post description")
                return
            }
            val detailModel=DetailModel(
                firstname = preferenceManager.userModel?.firstName,
                lastName = preferenceManager.userModel?.lastName,
                email = preferenceManager.userModel?.email
            )

            // Create a PostModel object
            val post = PostModel(
                title = title,
                description = description,
                userId = preferenceManager.userId,// Set the userId
                detailModel = detailModel

            )

            // Log userId for debugging
            Log.d("PostActivity", "User ID: ${preferenceManager.userId}")

            // Ensure the userId and communityId are valid before proceeding
            val userId = preferenceManager.userId
            if (userId != null && communityId != null) {
                // Call the ViewModel to add the post
                postViewModel.addPost(userId, communityId!!, post).observe(this@PostActivity) { state ->
                    when (state) {
                        is UiState.Loading -> {
                            Log.d("PostActivity", "Creating post: Loading...")
                        }

                        is UiState.Success -> {
                            commonUtil.showToast("Post created successfully!")
                        }

                        is UiState.Failure -> {
                            commonUtil.showToast("Error: ${state.error}")
                        }

                        else -> {
                            commonUtil.showToast("Something went wrong")
                        }
                    }
                }
            } else {
                commonUtil.showToast("Failed to create post. Invalid user or community ID.")
            }
        }
    }
}
