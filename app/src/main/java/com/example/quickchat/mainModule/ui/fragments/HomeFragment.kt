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
    private lateinit var  postViewModel: PostViewModel
    lateinit var binding: FragmentHomeBinding




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        postViewModel= ViewModelProvider(this)[PostViewModel::class.java]
        binding=DataBindingUtil.inflate(inflater,R.layout.fragment_home, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getALlPostData()
    }

    private fun setupRecyclerView(list: List<MainPostModel>) {
        adapter = GetAllPostAdapter(list,requireActivity())
        binding.rvHomeMixed.adapter = adapter


    }

    private fun getALlPostData(){

        postViewModel.getAllPost().observe(viewLifecycleOwner){
            Log.d("datttttaaaholu",it.toString())

            when(it){
                is UiState.Loading->{}
                is UiState.Success->{
                    Log.d("datttttaaaholu",it.data.toString())
                    setupRecyclerView(it.data)
                }
                is UiState.Failure->{}
            }
        }
    }

}