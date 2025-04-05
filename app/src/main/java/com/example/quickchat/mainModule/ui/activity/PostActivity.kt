package com.example.quickchat.mainModule.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.quickchat.R
import com.example.quickchat.communityModule.ui.activity.CreateCommunity
import com.example.quickchat.communityModule.ui.activity.CreateCommunity.Companion
import com.example.quickchat.communityModule.ui.adapters.ViewPagerAdapter
import com.example.quickchat.communityModule.ui.fragments.Posts
import com.example.quickchat.mainModule.models.PostModel
import com.example.quickchat.mainModule.viewmodels.PostViewModel
import com.example.quickchat.utility.BaseActivity
import com.example.quickchat.utility.PreferenceManager
import com.example.quickchat.utility.UiState
import com.example.quickchat.databinding.ActivityPostBinding
import com.example.quickchat.mainModule.models.DetailModel
import com.example.quickchat.mainModule.ui.fragments.PhotoUpload
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

@AndroidEntryPoint
class PostActivity : BaseActivity() {

    private lateinit var binding: ActivityPostBinding
    private val postViewModel: PostViewModel by viewModels()
    private var communityId: String? = null





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_post)
        preferenceManager = PreferenceManager(this)
        communityId = intent.getStringExtra("COMMUNITY_ID")
        setUpViewPager()



    }
    private fun setUpViewPager() {
        Log.d("setUpViewPager", "Initializing ViewPager and TabLayout")

        // Initialize the adapter
        val adapter = ViewPagerAdapter(this@PostActivity)
        Log.d("setUpViewPager", "ViewPagerAdapter created")

        // Add fragments to the adapter
        adapter.addFragment(PhotoUpload.newInstance(communityId ?: ""), "Photo") // Pass communityId
        Log.d("setUpViewPager", "Added PhotoUpload fragment with title 'Photo'")

        adapter.addFragment(PhotoUpload.newInstance(communityId ?: ""), "Videos") // Add Posts fragment
        Log.d("setUpViewPager", "Added Posts fragment with title 'Posts'")

        // Set the adapter to the ViewPager
        binding.pager.adapter = adapter
        Log.d("setUpViewPager", "ViewPager adapter set")

        // Disable user input for the ViewPager
        binding.pager.isUserInputEnabled = false
        Log.d("setUpViewPager", "ViewPager user input disabled")

        // Bind the ViewPager with the TabLayout using TabLayoutMediator
        TabLayoutMediator(binding.tabLayout, binding.pager) { tab, position ->
            tab.text = adapter.getPageTitle(position)
            Log.d("setUpViewPager", "Tab created at position $position with title: ${adapter.getPageTitle(position)}")
        }.attach()

        Log.d("setUpViewPager", "TabLayoutMediator attached to ViewPager and TabLayout")
    }















}