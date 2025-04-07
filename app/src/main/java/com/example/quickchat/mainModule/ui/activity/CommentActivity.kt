package com.example.quickchat.mainModule.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.quickchat.R
import com.example.quickchat.databinding.ActivityCommentBinding
import com.example.quickchat.mainModule.models.CommentModel
import com.example.quickchat.mainModule.ui.adapters.CommentAdapter
import com.example.quickchat.mainModule.viewmodels.PostViewModel
import com.example.quickchat.onboardingModule.models.UserModel
import com.example.quickchat.utility.PreferenceManager
import com.example.quickchat.utility.UiState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CommentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommentBinding
    private val postViewModel: PostViewModel by viewModels()
    private lateinit var commentAdapter: CommentAdapter
    private var postId: String? = null
    private var currentUser: UserModel? = null

    @Inject
    lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        postId = intent.getStringExtra("postId")
        if (postId == null) {
            Toast.makeText(this, "Error: Post ID not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        setupCommentsRecyclerView()
        getCurrentUserDetails()
        loadComments()
        setupCommentInput()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupCommentsRecyclerView() {
        commentAdapter = CommentAdapter(
            preferenceManager.userId ?: "",
            onLikeComment = { comment ->
                likeComment(comment)
            },
            onUnlikeComment = { comment ->
                unlikeComment(comment)
            }
        )
        binding.rvComments.adapter = commentAdapter
    }

    private fun getCurrentUserDetails() {
        val userId = preferenceManager.userId
        if (userId != null) {
            postViewModel.getDetails(userId).observe(this, Observer { state ->
                when (state) {
                    is UiState.Success -> {
                        currentUser = state.data
                        // Load current user profile image
                        Glide.with(this)
                            .load(currentUser?.imageUrl)
                            .placeholder(R.drawable.user)
                            .error(R.drawable.user)
                            .into(binding.ivCurrentUserProfile)
                    }
                    is UiState.Failure -> {
                        Toast.makeText(this, state.error, Toast.LENGTH_SHORT).show()
                    }
                    is UiState.Loading -> {
                        // Handle loading state if needed
                    }
                }
            })
        }
    }

    private fun loadComments() {
        postId?.let { id ->
            postViewModel.getComments(id).observe(this, Observer { state ->
                when (state) {
                    is UiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        commentAdapter.submitList(state.data)
                    }
                    is UiState.Failure -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this, state.error, Toast.LENGTH_SHORT).show()
                    }
                    is UiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                }
            })
        }
    }

    private fun setupCommentInput() {
        binding.btnPostComment.setOnClickListener {
            val commentText = binding.etCommentInput.text.toString().trim()
            if (commentText.isNotEmpty()) {
                addComment(commentText)
            }
        }
    }

    private fun addComment(commentText: String) {
        val userId = preferenceManager.userId
        if (userId == null || postId == null) {
            Toast.makeText(this, "Error: User ID or Post ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        val comment = CommentModel(
            text = commentText,
            postId = postId,
            userId = userId,
            role = currentUser?.firstName,
            imageUrl = currentUser?.imageUrl
        )

        postViewModel.addComment(postId!!, comment).observe(this, Observer { state ->
            when (state) {
                is UiState.Success -> {
                    binding.etCommentInput.text.clear()
                    // Refresh the comments list
                    loadComments()
                }
                is UiState.Failure -> {
                    Toast.makeText(this, state.error, Toast.LENGTH_SHORT).show()
                }
                is UiState.Loading -> {
                    // Handle loading state if needed
                }
            }
        })
    }

    private fun likeComment(comment: CommentModel) {
        val userId = preferenceManager.userId
        if (userId == null || postId == null || comment.commentId == null) {
            Toast.makeText(this, "Error: User ID, Post ID, or Comment ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        postViewModel.likeComment(postId!!, comment.commentId!!, userId).observe(this, Observer { state ->
            when (state) {
                is UiState.Success -> {
                    // Refresh the comments list
                    loadComments()
                }
                is UiState.Failure -> {
                    Toast.makeText(this, state.error, Toast.LENGTH_SHORT).show()
                }
                is UiState.Loading -> {
                    // Handle loading state if needed
                }
            }
        })
    }

    private fun unlikeComment(comment: CommentModel) {
        val userId = preferenceManager.userId
        if (userId == null || postId == null || comment.commentId == null) {
            Toast.makeText(this, "Error: User ID, Post ID, or Comment ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        postViewModel.unlikeComment(postId!!, comment.commentId!!, userId).observe(this, Observer { state ->
            when (state) {
                is UiState.Success -> {
                    // Refresh the comments list
                    loadComments()
                }
                is UiState.Failure -> {
                    Toast.makeText(this, state.error, Toast.LENGTH_SHORT).show()
                }
                is UiState.Loading -> {
                    // Handle loading state if needed
                }
            }
        })
    }
}