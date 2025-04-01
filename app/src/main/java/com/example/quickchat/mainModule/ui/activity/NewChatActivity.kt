package com.example.quickchat.mainModule.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.quickchat.R
import com.example.quickchat.databinding.ActivityNewChatBinding
import com.example.quickchat.mainModule.ui.adapters.UserAdapter
import com.example.quickchat.mainModule.viewmodels.PostViewModel
import com.example.quickchat.onboardingModule.models.UserModel
import com.example.quickchat.utility.BaseActivity
import com.example.quickchat.utility.UiState
import dagger.hilt.android.AndroidEntryPoint
import io.getstream.chat.android.client.ChatClient

@AndroidEntryPoint
class NewChatActivity : BaseActivity() {
    private lateinit var binding: ActivityNewChatBinding
    private lateinit var adapter: UserAdapter
    private val viewModel: PostViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_new_chat)

        setUpRecyclerView()
        observeUsers()

        // Fetch users using the correct method
        // Since preferenceManager is injected in BaseActivity, we can use it directly
        val currentUserId = preferenceManager.userId ?: "6iGWQhR4FFWi8BifmzoLib1go7D2"
        viewModel.getAllUser(currentUserId)
    }

    private fun setUpRecyclerView() {
        adapter = UserAdapter(emptyList(), this) { user ->
            createChannelWithUser(user)
        }
        binding.recyclerView.adapter = adapter
    }

    private fun createChannelWithUser(otherUser: UserModel) {
        val currentUserId = preferenceManager.userId
        if (currentUserId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val otherUserId = otherUser.uid?.toString() ?: run {
            Toast.makeText(this, "Invalid user ID", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a sorted channel ID to ensure consistency
        val channelId = listOf(currentUserId, otherUserId)
            .sorted()
            .joinToString("-")

        val client = ChatClient.instance()

        // Channel data
        val channelData = mapOf(
            "name" to (otherUser.firstName ?: "Chat"),
            "image" to (otherUser.imageUrl ?: ""),
            "members" to listOf(currentUserId, otherUserId)
        )

        client.createChannel(
            channelType = "messaging",
            channelId = channelId,
            memberIds = listOf(currentUserId, otherUserId),
            extraData = channelData
        ).enqueue { result ->
            if (result.isSuccess) {
                // Correct way to get the channel from the result
                val channel = result.getOrNull()
                if (channel != null) {
                    println("Channel created successfully: ${channel.cid}")
                    startActivity(ChatActivity.newIntent(this, channel))
                    finish()
                } else {
                    val error = "Channel is null despite success"
                    println(error)
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                }
            } else {
                // Correct way to get the error
                val error = result.errorOrNull()?.message ?: "Unknown error"
                println("Failed to create channel: $error")
                Toast.makeText(this, "Failed: $error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeUsers() {
        viewModel.users.observe(this) { state ->
            when(state) {
                is UiState.Failure -> {
                    Toast.makeText(this, state.error, Toast.LENGTH_SHORT).show()
                }
                UiState.Loading -> {
                    // Show loading state if needed
                }
                is UiState.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val users = state.data as List<UserModel>
                    adapter.updateData(users)
                }
            }
        }
    }
}