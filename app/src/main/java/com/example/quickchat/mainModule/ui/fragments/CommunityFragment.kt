
package com.example.quickchat.mainModule.ui.fragments
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quickchat.communityModule.ui.activity.CreateCommunity
import com.example.quickchat.databinding.FragmentComunityBinding
import com.example.quickchat.mainModule.ui.adapters.GetAllCommunityAdapter
import com.example.quickchat.mainModule.viewmodels.PostViewModel
import com.example.quickchat.utility.BaseFragment
import com.example.quickchat.utility.UiState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CommunityFragment : BaseFragment() {

    private var _binding: FragmentComunityBinding? = null
    private val binding get() = _binding!!

    private val postViewModel: PostViewModel by viewModels()
    private lateinit var adapter: GetAllCommunityAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentComunityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView
        setupRecyclerView()

        // Observe data from ViewModel
        observeCommunities()

        // Floating Action Button click listener
        binding.btnCreate.setOnClickListener {
            startActivity(Intent(requireActivity(), CreateCommunity::class.java))
        }
    }

    private fun setupRecyclerView() {
        adapter = GetAllCommunityAdapter(emptyList(), requireContext())
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@CommunityFragment.adapter
        }
    }

    private fun observeCommunities() {
        postViewModel.getAllCommunities(preferenceManager.userId.toString()) // Replace "userId" with the actual user ID
            .observe(viewLifecycleOwner) { state ->
                when (state) {
                    is UiState.Loading -> {
                        // Show loading indicator if needed
                    }
                    is UiState.Success -> {
                        Log.d("Communities", state.data.toString())
                        // Update adapter with new data
                        adapter = GetAllCommunityAdapter(state.data, requireContext())
                        binding.recyclerView.adapter = adapter
                    }


                    is UiState.Failure -> {
                        // Handle failure state
                        Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
