package com.example.quickchat.mainModule.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.quickchat.R
import com.example.quickchat.databinding.FragmentHomeBinding
import com.example.quickchat.mainModule.models.MainPostModel
import com.example.quickchat.mainModule.models.PostModel
import com.example.quickchat.mainModule.ui.adapters.GetAllPostAdapter
import com.example.quickchat.mainModule.viewmodels.PostViewModel
import com.example.quickchat.utility.BaseFragment
import com.example.quickchat.utility.ShareUtils
import com.example.quickchat.utility.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
    }

    private fun setupRecyclerView(list: List<MainPostModel>) {
        adapter = GetAllPostAdapter(
            list,
            requireActivity(),
            // Like callback
            onLikeClickListener = { post ->
                // Handle like action
                val currentUserId = preferenceManager.userId
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
            },
            // Share callback
            onShareClickListener = { post ->
                handleSharePost(post)
            }
        )
        binding.rvHomeMixed.adapter = adapter
    }

    private fun handleSharePost(post: PostModel) {
        // Launch in a coroutine because sharing involves image processing
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                ShareUtils.sharePost(requireContext(), post)
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error sharing post: ${e.message}")
                commonUtil.showToast("Failed to share post")
            }
        }
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

    override fun onResume() {
        super.onResume()
        getALlPostData()
    }
}