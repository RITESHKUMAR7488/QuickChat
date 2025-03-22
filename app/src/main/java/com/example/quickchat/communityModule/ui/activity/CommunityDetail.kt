package com.example.quickchat.communityModule.ui.activity

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
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.quickchat.R
import com.example.quickchat.communityModule.ui.activity.CreateCommunity.Companion
import com.example.quickchat.communityModule.ui.adapters.ViewPagerAdapter
import com.example.quickchat.communityModule.ui.fragments.Posts
import com.example.quickchat.communityModule.viemodels.CommunityViewModel
import com.example.quickchat.databinding.ActivityCommunityDetailBinding
import com.example.quickchat.mainModule.viewmodels.PostViewModel
import com.example.quickchat.utility.BaseActivity
import com.example.quickchat.utility.UiState
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

@AndroidEntryPoint
class CommunityDetail : BaseActivity() {
    lateinit var communityId: String
    private lateinit var binding: ActivityCommunityDetailBinding
    private val communityViewModel: CommunityViewModel by viewModels()
    private val mainViewModel: PostViewModel by viewModels()
    private var filePath: Uri? = null

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
        const val STORAGE_PERMISSION_CODE = 101
        const val PICK_IMAGE_REQUEST = 22
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= DataBindingUtil.setContentView(this,R.layout.activity_community_detail)
        communityId = intent.getStringExtra("communityId").toString()
        setUpViewPager()


    }

    private fun setupThreeDotMenu() {
        // Find the three-dot menu ImageView
        val threeDotMenu = binding.root.findViewById<ImageView>(R.id.three_dot_menu)

        // Set click listener for the three-dot menu
        threeDotMenu.setOnClickListener { view ->
            showPopupMenu(view)
        }
    }

    private fun showPopupMenu(view: View) {
        // Initialize PopupMenu
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.nav_menu, popupMenu.menu)

        // Handle menu item clicks
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_settings -> {
                    val intent= Intent(this,UpdateCommunity::class.java)
                    intent.putExtra("COMMUNITY_ID", communityId)
                    startActivity(intent)
                    true
                }
                R.id.action_poster -> {
                    // Handle Edit Poster click
                    val intent = Intent(this, UpdateCommunity::class.java)
                    intent.putExtra("COMMUNITY_ID", communityId)
                    intent.putExtra("EDIT_POSTER", true)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        // Show the PopupMenu
        popupMenu.show()
    }

    private fun setCommunityDetails(){
        communityViewModel.getCommunityDetails(communityId).observe(this){
            Log.d("taggggggggg", "setCommunityDetails: ${it.toString()}")
            when(it){
                is UiState.Loading->{}
                is UiState.Success->{

                    binding.communityDescription.text=it.data.communityDescription
                    binding.address.text=it.data.address
                    binding.profileName.text=it.data.communityName
                    binding.mobileNumber.text=it.data.mobileNumber
                    if(it.data.imageUrl!=null){
                        Glide.with(this).load(it.data.imageUrl).into(binding.profileImage)

                    }

                }
                is UiState.Failure->{}
            }
        }
    }



    private fun setUpViewPager() {
        Log.d("TAGefergerg", "setUpViewPager: $communityId")
        val adapter = ViewPagerAdapter(this@CommunityDetail)
        adapter.addFragment(Posts.newInstance(communityId), "Posts")
        adapter.addFragment(Posts.newInstance(communityId), "Reels")
        adapter.addFragment(Posts.newInstance(communityId), "Songs")
        binding.pager.adapter = adapter
        binding.pager.isUserInputEnabled = false

        // Bind the viewPager with the TabLayout using TabLayoutMediator
        TabLayoutMediator(binding.tabLayout, binding.pager) { tab, position ->
            tab.text = adapter.getPageTitle(position)
        }.attach()
    }

    override fun onResume() {
        super.onResume()
        setupThreeDotMenu()
        setCommunityDetails()

    }





}