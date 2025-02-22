package com.example.quickchat.mainModule.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment

import com.example.quickchat.R
import com.example.quickchat.communityModule.ui.activity.ChooseCommunity
import com.example.quickchat.databinding.ActivityMain3Binding
import com.example.quickchat.mainModule.ui.fragments.CommunityFragment
import com.example.quickchat.mainModule.ui.fragments.HomeFragment
import com.example.quickchat.mainModule.viewmodels.PostViewModel
import com.example.quickchat.utility.BaseActivity
import com.example.quickchat.utility.UiState

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : BaseActivity() {
    private lateinit var binding: ActivityMain3Binding
    private val postViewModel: PostViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= DataBindingUtil.setContentView(this,R.layout.activity_main3)
        getUSerDetails()

        replaceFragment(HomeFragment())

        with(binding){bottomNavigationView.setOnNavigationItemSelectedListener { item ->

            when (item.itemId) {
                R.id.Add -> {
                    val intent = Intent(this@HomeActivity, ChooseCommunity::class.java) // Replace AddActivity with your target activity
                    startActivity(intent)
                    true // Return true to indicate the selection was handled
                }

                R.id.community->{
                    replaceFragment(CommunityFragment())
                    true
                }

                R.id.home->{
                    replaceFragment(HomeFragment())
                    true
                }
                else -> {
                    replaceFragment(HomeFragment())
                    false
                }
            }

        }


        }

    }
    fun getUSerDetails(){
        preferenceManager.userId?.let {
            postViewModel.getDetails(it).observe(this){
                when(it){
                    is UiState.Loading->{}
                    is UiState.Success->{
                        Log.d("datttttaaa",it.data.toString())
                        preferenceManager.userModel=it.data
                    }

                    is UiState.Failure->{}
                }
            }
        }

    }

    fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, fragment)
        fragmentTransaction.addToBackStack(null) // Optional, if you want to keep the fragment in the back stack
        fragmentTransaction.commit()
    }
}