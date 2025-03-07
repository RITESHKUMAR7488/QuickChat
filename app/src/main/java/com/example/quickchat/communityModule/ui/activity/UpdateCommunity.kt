package com.example.quickchat.communityModule.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.example.quickchat.R
import com.example.quickchat.communityModule.models.CommunityModels
import com.example.quickchat.communityModule.viemodels.CommunityViewModel
import com.example.quickchat.databinding.ActivityUpdateCommunityBinding
import com.example.quickchat.mainModule.ui.activity.UserDetailActivity
import com.example.quickchat.utility.BaseActivity
import com.example.quickchat.utility.UiState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UpdateCommunity : BaseActivity() {
    private lateinit var binding: ActivityUpdateCommunityBinding
    private val communityViewModel: CommunityViewModel by viewModels()
    lateinit var communityId: String
    lateinit var userId: String
    lateinit var model: CommunityModels

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=DataBindingUtil.setContentView(this,R.layout.activity_update_community)
        with(binding){
            communityId=intent.getStringExtra("COMMUNITY_ID").toString()
            userId=preferenceManager.userModel?.uid.toString()
            fetchCommunityDetails()
            UpdateCommunityButton.setOnClickListener {
                updateCommunityData()
            }


        }

    }

    private fun fetchCommunityDetails() {
        communityViewModel.getCommunityDetails(communityId).observe(this) {
            when (it) {
                is UiState.Failure -> {}
                UiState.Loading -> {}
                is UiState.Success -> {
                    model=it.data
                    binding.nameInput.setText(it.data.communityName)
                    binding.descriptionInput.setText(it.data.communityDescription)
                    binding.emailInput.setText(it.data.email)
                    binding.mobileInput.setText(it.data.mobileNumber)
                    binding.addressInput.setText(it.data.address)
                }
            }

        }
    }

    private fun updateCommunityData(){
        val name=binding.nameInput.text.toString()
        val description=binding.descriptionInput.text.toString()
        val email=binding.emailInput.text.toString()
        val mobile=binding.mobileInput.text.toString()
        val address=binding.addressInput.text.toString()

        if(name.isBlank()){
            binding.nameInput.error="Please enter name"
        }else if(description.isBlank()){
            binding.descriptionInput.error="Please enter description"

        }
        else if(email.isBlank()){
            binding.emailInput.error="Please enter email"
        }
        else if(mobile.isBlank()) {
            binding.mobileInput.error = "Please enter mobile number"
        }
        else if(address.isBlank()){
            binding.addressInput.error="Please enter address"

        }else{
              model.apply {
                communityName = name
                communityDescription = description
                this.email = email
                mobileNumber = mobile
                this.address = address
            }

            Log.d("statesdddddd",model.toString())

            communityViewModel.updateCommunity(userId,communityId,model).observe(this){
                when(it){
                    is UiState.Failure -> {}
                    UiState.Loading -> {}
                    is UiState.Success -> {

                        onBackPressedDispatcher.onBackPressed()
                        finish()

                    }
                }
            }
        }



    }



}