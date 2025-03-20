package com.example.quickchat.mainModule.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider

import com.example.quickchat.R
import com.example.quickchat.databinding.FragmentHomeBinding
import com.example.quickchat.mainModule.models.MainPostModel
import com.example.quickchat.mainModule.ui.adapters.GetAllPostAdapter
import com.example.quickchat.mainModule.viewmodels.PostViewModel
import com.example.quickchat.utility.BaseFragment
import com.example.quickchat.utility.UiState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment() {
    private lateinit var adapter: GetAllPostAdapter
    private lateinit var postViewModel: PostViewModel
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        postViewModel = ViewModelProvider(this)[PostViewModel::class.java]
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getALlPostData()
    }

    private fun setupRecyclerView(list: List<MainPostModel>) {
        // Pass the onLikeClickListener callback to the adapter
        adapter = GetAllPostAdapter(list, requireActivity()) { post ->
            // Handle like action here
            val currentUserId = preferenceManager.userId // Replace with the actual current user's ID
            if (post.likes?.contains(currentUserId) == true) {
                // Unlike the post
                if (currentUserId != null) {
                    postViewModel.unlikePost(post.postId!!, currentUserId)
                }
            } else {
                // Like the post
                if (currentUserId != null) {
                    postViewModel.likePost(post.postId!!, currentUserId)
                }
            }
        }
        binding.rvHomeMixed.adapter = adapter
    }

    private fun getALlPostData() {
        postViewModel.getAllPost().observe(viewLifecycleOwner) {
            Log.d("datttttaaaholu", it.toString())

            when (it) {
                is UiState.Loading -> {
                    // Show loading state (e.g., show a progress bar)
                }
                is UiState.Success -> {
                    Log.d("datttttaaaholu", it.data.toString())
                    setupRecyclerView(it.data)
                }
                is UiState.Failure -> {
                    // Handle error (e.g., show a toast or error message)
                    Log.e("HomeFragment", "Error fetching posts: ${it.error}")
                }
            }
        }
    }
}