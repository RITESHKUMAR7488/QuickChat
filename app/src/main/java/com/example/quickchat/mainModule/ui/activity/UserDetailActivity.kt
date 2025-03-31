package com.example.quickchat.mainModule.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.quickchat.R
import com.example.quickchat.databinding.ActivityUserDetailBinding
import com.example.quickchat.mainModule.viewmodels.PostViewModel
import com.example.quickchat.onboardingModule.models.UserModel
import com.example.quickchat.utility.BaseActivity
import com.example.quickchat.utility.UiState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserDetailActivity : BaseActivity() {
    private lateinit var binding: ActivityUserDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_detail)
        with(binding) {
            fetchUserData()
            EditProfileButton.setOnClickListener {
                val intent = Intent(this@UserDetailActivity, EditUserDetailActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun fetchUserData() {
        val name = preferenceManager.userModel?.firstName
        val email = preferenceManager.userModel?.email
        val address = preferenceManager.userModel?.address
        val mobile = preferenceManager.userModel?.mobileNumber
        val profileImageUrl = preferenceManager.userModel?.imageUrl // Fetch profile picture URL

        binding.nameInput.text = name
        binding.emailInput.text = email
        binding.addressInput.text = address
        binding.mobileInput.text = mobile

        // Load the profile picture if the URL is available
        profileImageUrl?.let { url ->
            loadProfilePicture(url)
        }
    }

    private fun loadProfilePicture(imageUrl: String) {
        Glide.with(this)
            .load(imageUrl)
            .into(binding.profileImage)
    }
}