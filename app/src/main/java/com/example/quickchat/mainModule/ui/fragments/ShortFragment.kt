package com.example.quickchat.mainModule.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.quickchat.R
import com.example.quickchat.databinding.FragmentShortBinding
import com.example.quickchat.mainModule.models.VideoData
import com.example.quickchat.mainModule.models.VideoFile
import com.example.quickchat.mainModule.ui.adapters.ShortsAdapter
import com.example.quickchat.mainModule.viewmodels.PostViewModel
import com.example.quickchat.utility.BaseFragment
import com.example.quickchat.utility.UiState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShortFragment : BaseFragment() {
    private lateinit var binding: FragmentShortBinding
    private lateinit var adapter: ShortsAdapter
    private val viewModel: PostViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_short, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getShorts()

    }


    private fun setUpRecyclerView(list: List<VideoFile>) {
        Log.d("dattaa", list.toString())

        adapter = ShortsAdapter(list, requireActivity())
        binding.rvShorts.adapter = adapter

        // ✅ Ensures videos scroll one at a time (Reels-like experience)
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.rvShorts)

        // ✅ Enable vertical scrolling
        binding.rvShorts.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
    }

    private fun getShorts() {
        viewModel.getAllVideo().observe(viewLifecycleOwner) {
            Log.d("riteshDataaa", it.toString())
            when (it) {

                is UiState.Loading -> {}
                is UiState.Success -> {
                    val list=ArrayList<VideoFile>()

                    for (i in it.data.videos){
                        list.addAll(i.videoFiles)
                    }

                    setUpRecyclerView(list)
                }

                is UiState.Failure -> {}

            }
        }


    }
}