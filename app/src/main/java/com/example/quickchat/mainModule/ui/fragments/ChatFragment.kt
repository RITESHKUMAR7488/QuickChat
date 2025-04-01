package com.example.quickchat.mainModule.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
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
    private lateinit var localPreferenceManager: PreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        localPreferenceManager = PreferenceManager(requireContext())

        binding.btnCreate.setOnClickListener {
            startActivity(Intent(requireContext(), NewChatActivity::class.java))
        }

        val offlinePluginFactory = StreamOfflinePluginFactory(requireContext())
        val statePluginFactory = StreamStatePluginFactory(
            config = StatePluginConfig(
                backgroundSyncEnabled = true,
                userPresence = true,
            ),
            appContext = requireContext(),
        )

        val client = ChatClient.Builder("rmfj6d9hddee", requireContext())
            .withPlugins(offlinePluginFactory, statePluginFactory)
            .logLevel(ChatLogLevel.ALL)
            .build()

        val user = User(
            id = localPreferenceManager.userId.toString(),
            name = localPreferenceManager.userModel?.firstName.toString(),
            image = localPreferenceManager.userModel?.imageUrl.toString()
        )

        val token = generateJWT(localPreferenceManager.userId.toString(), "2tdtqr2gj6f49j2e5jdrjfkr6v2w4wynza6396xrm8guum5cp44bdf7naur94hr6")

        Log.d("ChatFragmentsss12", "Token: $token")

        client.connectUser(user, token).enqueue { result ->
            if (result.isSuccess) {
                Log.d("ChatFragment", "User connected successfully!")

                val filter = Filters.and(
                    Filters.eq("type", "messaging"),
                    Filters.contains("members", user.id)
                )

                val viewModelFactory = ChannelListViewModelFactory(filter, ChannelListViewModel.DEFAULT_SORT)
                val viewModel: ChannelListViewModel by viewModels { viewModelFactory }

                viewModel.bindView(binding.channelListView, viewLifecycleOwner)
                binding.channelListView.setChannelItemClickListener { channel ->
                    startActivity(ChatActivity.newIntent(requireContext(), channel))
                }

                fetchUsers(client)
            } else {
                Log.e("ChatFragment", "User connection failed: ${result}")
                Toast.makeText(requireContext(), "Something went wrong!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // Use the localPreferenceManager instead of the base class's preferenceManager
        val filter = Filters.and(
            Filters.eq("type", "messaging"),
            Filters.contains("members", localPreferenceManager.userId.toString())
        )

        val viewModelFactory = ChannelListViewModelFactory(filter, ChannelListViewModel.DEFAULT_SORT)
        val viewModel: ChannelListViewModel by viewModels { viewModelFactory }
        viewModel.bindView(binding.channelListView, viewLifecycleOwner)
    }

    private fun fetchUsers(client: ChatClient) {
        client.queryUsers(
            QueryUsersRequest(
                Filters.ne("id", ""),
                offset = 0,
                limit = 50
            )
        ).enqueue { result ->
            if (result.isSuccess) {
                val users = result.getOrNull()
                Log.d("ChatFragment", "Users found: ${users?.size}")

                users?.forEach { user ->
                    Log.d("ChatFragment", "User ID: ${user.id}, Name: ${user.name}, Image: ${user.image}")
                }
            } else {
                Log.e("ChatFragment", "Error fetching users: ${result}")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun generateJWT(userId: String, apiSecret: String, expirationInMinutes: Int? = null): String {
        val algorithm = Algorithm.HMAC256(apiSecret)
        val jwtBuilder = JWT.create()
            .withIssuer("YourAppName")
            .withSubject(userId)
            .withClaim("user_id", userId)
            .withIssuedAt(Date())

        expirationInMinutes?.let {
            val expirationDate = Date(System.currentTimeMillis() + it * 60 * 1000)
            jwtBuilder.withExpiresAt(expirationDate)
        }

        return jwtBuilder.sign(algorithm)
    }
}