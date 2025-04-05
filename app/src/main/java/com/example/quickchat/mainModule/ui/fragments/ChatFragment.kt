package com.example.quickchat.mainModule.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
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
    // Use this safe getter that returns nullable binding
    private val binding get() = _binding

    private lateinit var client: ChatClient
    private val viewModel: ChannelListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return requireNotNull(_binding).root
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
            // Use viewLifecycleOwner to ensure we're in a valid state
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                if (binding == null) return@launchWhenStarted // Fragment detached

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
    }

    private fun setupChannelList() {
        if (binding == null) return // Early exit if fragment is detached

        val userId = preferenceManager.userId?.toString() ?: return

        val filter = Filters.and(
            Filters.eq("type", "messaging"),
            Filters.`in`("members", listOf(userId)),
            Filters.eq("is_direct_message", true),
            Filters.eq("member_count", 2)
        )

        val viewModelFactory = ChannelListViewModelFactory(
            filter = filter,
            sort = ChannelListViewModel.DEFAULT_SORT,
            limit = 30
        )

        // Use the viewModel property we declared at class level
        viewModel.bindView(requireNotNull(binding).channelListView, viewLifecycleOwner)

        binding?.channelListView?.setChannelItemClickListener { channel ->
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
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                if (binding == null) return@launchWhenStarted

                if (result.isSuccess) {
                    val users = result.getOrNull() ?: emptyList()
                    Log.d("ChatFragment", "Fetched ${users.size} users")
                } else {
                    val error = result.errorOrNull()?.message ?: "Unknown error"
                    Log.e("ChatFragment", "User fetch failed: $error")
                    showToast("Failed to load users")
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding?.btnCreate?.setOnClickListener {
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
        // Clear references to avoid memory leaks
        binding?.channelListView?.setChannelItemClickListener(null)
        _binding = null
    }
}