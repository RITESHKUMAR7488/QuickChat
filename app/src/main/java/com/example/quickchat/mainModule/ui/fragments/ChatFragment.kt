package com.example.quickchat.mainModule.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.quickchat.databinding.FragmentChatBinding
import com.example.quickchat.mainModule.ui.activity.ChatActivity
import com.example.quickchat.mainModule.ui.activity.NewChatActivity
import com.example.quickchat.utility.BaseFragment
import com.example.quickchat.utility.PreferenceManager
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.User
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory
import io.getstream.chat.android.state.plugin.config.StatePluginConfig
import io.getstream.chat.android.state.plugin.factory.StreamStatePluginFactory
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModel
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModelFactory
import io.getstream.chat.android.ui.viewmodel.channels.bindView
import java.util.Date

class ChatFragment : BaseFragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var client: ChatClient


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferenceManager = PreferenceManager(requireContext())
        initializeStreamClient()
        setupClickListeners()
    }

    private fun initializeStreamClient() {
        try {
            val offlinePluginFactory = StreamOfflinePluginFactory(appContext = requireContext())
            val statePluginFactory = StreamStatePluginFactory(
                config = StatePluginConfig(
                    backgroundSyncEnabled = true,
                    userPresence = true
                ),
                appContext = requireContext()
            )

            client = ChatClient.Builder("rmfj6d9hddee", requireContext().applicationContext)
                .withPlugins(offlinePluginFactory, statePluginFactory)
                .logLevel(ChatLogLevel.ALL)
                .build()

            connectUser()
        } catch (e: Exception) {
            Log.e("ChatFragment", "Stream initialization failed", e)
            showToast("Chat initialization failed")
        }
    }

    private fun connectUser() {
        val userId = preferenceManager.userId?.toString() ?: run {
            showToast("User not logged in")
            return
        }

        val user = User(
            id = userId,
            name = preferenceManager.userModel?.firstName ?: "User",
            image = preferenceManager.userModel?.imageUrl ?: ""
        )

        val token = generateJWT(
            userId = userId,
            apiSecret = "2tdtqr2gj6f49j2e5jdrjfkr6v2w4wynza6396xrm8guum5cp44bdf7naur94hr6"
        )

        client.connectUser(user, token).enqueue { result ->
            if (result.isSuccess) {
                Log.d("ChatFragment", "User connected successfully")
                setupChannelList()
                fetchUsers()
            } else {
                Log.e("ChatFragment", "Connection failed: ${result.errorOrNull()}")
                showToast("Failed to connect to chat")
            }
        }
    }

    private fun setupChannelList() {
        val userId = preferenceManager.userId?.toString() ?: return

        val filter = Filters.and(
            Filters.eq("type", "messaging"),
            Filters.`in`("members", listOf(userId)),
            Filters.eq("is_direct_message", true), // Match your channel property
            Filters.eq("member_count", 2) // Keep this if you only want 1:1 chats
        )

        val viewModelFactory = ChannelListViewModelFactory(
            filter = filter,
            sort = ChannelListViewModel.DEFAULT_SORT,
            limit = 30
        )

        val viewModel: ChannelListViewModel by viewModels { viewModelFactory }
        viewModel.bindView(binding.channelListView, viewLifecycleOwner)

        binding.channelListView.setChannelItemClickListener { channel ->
            navigateToChat(channel)
        }
    }

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
                Log.d("ChatFragment", "Fetched ${users.size} users")

                // If you're using UiState in your ViewModel/UI:
                // viewModel.setUsers(UiState.Success(users))
            } else {
                val error = result.errorOrNull()?.message ?: "Unknown error"
                Log.e("ChatFragment", "User fetch failed: $error")
                showToast("Failed to load users")

                // If using UiState:
                // viewModel.setUsers(UiState.Failure(error))
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnCreate.setOnClickListener {
            startActivity(Intent(requireContext(), NewChatActivity::class.java))
        }
    }

    private fun navigateToChat(channel: io.getstream.chat.android.models.Channel) {
        startActivity(ChatActivity.newIntent(requireContext(), channel))
    }

    private fun generateJWT(userId: String, apiSecret: String): String {
        return JWT.create()
            .withIssuer("QuickChat")
            .withSubject(userId)
            .withClaim("user_id", userId)
            .withIssuedAt(Date())
            .withExpiresAt(Date(System.currentTimeMillis() + 3600 * 1000))
            .sign(Algorithm.HMAC256(apiSecret))
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}