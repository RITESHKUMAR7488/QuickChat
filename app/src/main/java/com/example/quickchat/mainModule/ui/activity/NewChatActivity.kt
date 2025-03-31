package com.example.quickchat.mainModule.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.quickchat.R
import com.example.quickchat.databinding.ActivityNewChatBinding
import com.example.quickchat.mainModule.ui.adapters.UserAdapter
import com.example.quickchat.mainModule.viewmodels.PostViewModel
import com.example.quickchat.onboardingModule.models.UserModel
import com.example.quickchat.utility.BaseActivity
import com.example.quickchat.utility.UiState
import dagger.hilt.android.AndroidEntryPoint

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
        viewModel.getAllUser("6iGWQhR4FFWi8BifmzoLib1go7D2") // Replace with the actual current user ID
    }

    private fun setUpRecyclerView() {
        adapter = UserAdapter(emptyList(), this) { user ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("USER", user)
            startActivity(intent)
        }
        binding.recyclerView.adapter = adapter
    }

    private fun observeUsers() {
        viewModel.users.observe(this) { state ->
            when(state){
                is UiState.Failure -> {}
                UiState.Loading -> {}
                is UiState.Success<*> -> {
                    adapter.updateData(state.data as List<UserModel>)
                }
            }
        }
    }
}
