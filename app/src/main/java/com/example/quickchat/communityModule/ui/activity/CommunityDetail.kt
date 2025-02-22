package com.example.quickchat.communityModule.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.quickchat.R
import com.example.quickchat.communityModule.ui.adapters.ViewPagerAdapter
import com.example.quickchat.communityModule.ui.fragments.Posts
import com.example.quickchat.communityModule.viemodels.CommunityViewModel
import com.example.quickchat.databinding.ActivityCommunityDetailBinding
import com.example.quickchat.utility.BaseActivity
import com.example.quickchat.utility.UiState
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CommunityDetail : BaseActivity() {
    lateinit var communityId: String
    private lateinit var binding: ActivityCommunityDetailBinding
    private val communityViewModel: CommunityViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= DataBindingUtil.setContentView(this,R.layout.activity_community_detail)
        communityId = intent.getStringExtra("communityId").toString()

        setUpViewPager()
        setCommunityDetails()




    }

    private fun setCommunityDetails(){
        communityViewModel.getCommunityDetails(communityId).observe(this){
            when(it){
                is UiState.Loading->{}
                is UiState.Success->{
                    binding.communityDescription.text=it.data.communityDescription
                    binding.profileName.text=it.data.communityName
                    if(it.data.imageUrl!=null){
                        Glide.with(this).load(it.data.imageUrl).into(binding.profileImage)

                    }

                }
                is UiState.Failure->{}
            }
        }
    }



    private fun setUpViewPager() {
        Log.d("TAG", "setUpViewPager: $communityId")
        val adapter = ViewPagerAdapter(this@CommunityDetail)
        adapter.addFragment(Posts(communityId), "Posts")
        adapter.addFragment(Posts(communityId), "Reels")
        adapter.addFragment(Posts(communityId), "Songs")
        binding.pager.adapter = adapter
        binding.pager.isUserInputEnabled = false




        // bind the viewPager with the TabLayout using TabLayoutMediator
        TabLayoutMediator(binding.tabLayout, binding.pager) { tab, position ->
            tab.text = adapter.getPageTitle(position)
        }.attach()
    }
}