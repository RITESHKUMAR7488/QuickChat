package com.example.quickchat.mainModule.ui.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.quickchat.R
import com.example.quickchat.databinding.ActivityNewChatBinding
import com.example.quickchat.mainModule.models.ChatModel
import com.example.quickchat.mainModule.ui.adapters.UserAdapter
import com.example.quickchat.mainModule.viewmodels.PostViewModel
import com.example.quickchat.onboardingModule.models.UserModel
import com.example.quickchat.utility.BaseActivity
import com.example.quickchat.utility.UiState
import dagger.hilt.android.AndroidEntryPoint
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.models.Filters

@AndroidEntryPoint
class NewChatActivity : BaseActivity() {
    private lateinit var binding: ActivityNewChatBinding
    private lateinit var adapter: UserAdapter
    private val viewModel: PostViewModel by viewModels()

    private lateinit var client: ChatClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_new_chat)
        client = ChatClient.instance()

        setUpRecyclerView()
        //observeUsers()
        fetchUsers()


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


    private fun createChannelWithUser(otherUser: ChatModel) {
        val currentUserId = preferenceManager.userId
        if (currentUserId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val otherUserId = otherUser.id ?: run {
            Toast.makeText(this, "Invalid user ID", Toast.LENGTH_SHORT).show()
            return
        }

        val channelId = listOf(currentUserId, otherUserId)
            .sorted()
            .joinToString("_dm_")

        val channelData = mapOf(
            "members" to listOf(currentUserId, otherUserId),
            "is_direct_message" to true
        )

        client.createChannel(
            channelType = "messaging",
            channelId = channelId,
            memberIds = listOf(currentUserId, otherUserId),
            extraData = channelData
        ).enqueue { result ->
            if (result.isSuccess) {
                val channel = result.getOrNull()
                if (channel != null) {
                    startActivity(ChatActivity.newIntent(this, channel))
                    finish()
                } else {
                    Toast.makeText(this, "Failed to create conversation", Toast.LENGTH_SHORT).show()
                }
            } else {
                val error = result.errorOrNull()?.message ?: "Unknown error"
                Toast.makeText(this, "Failed: $error", Toast.LENGTH_SHORT).show()
                Log.d("ChatFragment", "User fetch failed: $error")
            }
        }
    }


//    private fun observeUsers() {
//        viewModel.users.observe(this) { state ->
//            when (state) {
//                is UiState.Failure -> {
//                    Toast.makeText(this, state.error, Toast.LENGTH_SHORT).show()
//                }
//
//                UiState.Loading -> {
//                    // Show loading state if needed
//                }
//
//                is UiState.Success<*> -> {
//                    @Suppress("UNCHECKED_CAST")
//
//                }
//            }
//        }
//    }

    private fun fetchUsers() {
        val userId = preferenceManager.userId?.toString() ?: return

        client.queryUsers(
            QueryUsersRequest(
                filter = Filters.ne("id", userId),
                offset = 0,
                limit = 50
            )
        ).enqueue { result ->
            if (result.isSuccess) {
                // CORRECT WAY to access Stream SDK response data
                val users = result.getOrNull() ?: emptyList() // Directly get the list
                Log.d("ChatFragment", "Fetched ${users.toString()} users")

                val chatUser= ArrayList<ChatModel>()

                for (i in users){
                    chatUser.add(ChatModel(name = i.name, image = i.image, id = i.id))
                }





                adapter.updateData(chatUser)

                // If you're using UiState in your ViewModel/UI:
                // viewModel.setUsers(UiState.Success(users))
            } else {
                val error = result.errorOrNull()?.message ?: "Unknown error"
                Log.e("ChatFragment", "User fetch failed: $error")


                // If using UiState:
                // viewModel.setUsers(UiState.Failure(error))
            }
        }
    }
}