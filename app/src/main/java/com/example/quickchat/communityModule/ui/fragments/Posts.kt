package com.example.quickchat.communityModule.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.quickchat.R
import com.example.quickchat.communityModule.ui.adapters.CommunityPostAdapter
import com.example.quickchat.communityModule.viemodels.CommunityViewModel
import com.example.quickchat.databinding.FragmentPostsBinding
import com.example.quickchat.mainModule.models.PostModel
import com.example.quickchat.utility.BaseFragment
import com.example.quickchat.utility.UiState
import dagger.hilt.android.AndroidEntryPoint
@AndroidEntryPoint
class Posts : BaseFragment() {

    private lateinit var list: List<PostModel>
    private lateinit var adapter: CommunityPostAdapter
    private lateinit var communityViewModel: CommunityViewModel
    private lateinit var binding: FragmentPostsBinding

    // Use a companion object to create a new instance of the fragment with arguments
    companion object {
        private const val ARG_COMMUNITY_ID = "communityId"

        fun newInstance(communityId: String): Posts {
            val fragment = Posts()
            val args = Bundle()
            args.putString(ARG_COMMUNITY_ID, communityId)
            fragment.arguments = args
            return fragment
        }
    }

    private var communityId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retrieve the communityId from arguments
        arguments?.let {
            communityId = it.getString(ARG_COMMUNITY_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        communityViewModel = ViewModelProvider(this)[CommunityViewModel::class.java]
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_posts, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fetch posts using the communityId
        Log.d("TAG", "getPosts: $communityId  ${preferenceManager.userId}")
        communityId?.let { id ->
            getPosts(id)
        }
    }

    private fun getPosts(communityId: String) {

        Log.d("TAGddddfddd", "getPosts: $communityId  ${preferenceManager.userId}")
        communityViewModel.getCommunityPost(communityId)
            .observe(viewLifecycleOwner) { state ->
                when (state) {
                    is UiState.Loading -> {
                        // Handle loading state
                    }
                    is UiState.Success -> {
                        setupRecyclerView(state.data)
                        Log.d("TAG", "getPostsddd: ${state.data}")
                    }
                    is UiState.Failure -> {
                        // Handle failure state
                    }
                }
            }
    }

    private fun setupRecyclerView(list: List<PostModel>) {
        Log.d("hfedhojioweg", "setupRecyclerView: $list")
        adapter = CommunityPostAdapter(list)
        binding.rvPost.adapter = adapter
    }
}