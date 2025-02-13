package com.example.quickchat.communityModule.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.quickchat.R
import com.example.quickchat.communityModule.ui.adapters.CommunityPostAdapter
import com.example.quickchat.communityModule.viemodels.CommunityViewModel
import com.example.quickchat.databinding.FragmentPostsBinding
import com.example.quickchat.mainModule.models.PostModel
import com.example.quickchat.mainModule.viewmodels.PostViewModel
import com.example.quickchat.utility.BaseFragment
import com.example.quickchat.utility.PreferenceManager
import com.example.quickchat.utility.UiState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Posts (val communityId: String): BaseFragment() {
    private lateinit var list: List<PostModel>
    private lateinit var adapter: CommunityPostAdapter
    private val communityViewModel: CommunityViewModel by viewModels()



    lateinit var binding: FragmentPostsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_posts, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        getPosts(communityId)

    }

    private fun getPosts(communityId: String) {
        // Fetch posts using the communityId
        communityViewModel.getCommunityPost(preferenceManager.userId.toString(), communityId)
            .observe(viewLifecycleOwner) { state ->
                when (state) {
                    is UiState.Loading -> {

                    }
                    is UiState.Success -> {
                        setupRecyclerView(state.data)
                        Log.d("TAG", "getPosts: ${state.data}")
                    }
                    is UiState.Failure -> {

                    }
                }

            }

    }

    private fun setupRecyclerView(list: List<PostModel>) {
        adapter = CommunityPostAdapter(list)
        binding.rvPost.adapter = adapter
    }
}