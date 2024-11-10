package com.example.quickchat.communityModule.ui.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.quickchat.R
import com.example.quickchat.communityModule.models.CommunityModels
import com.example.quickchat.communityModule.viewModels.CommunityViewModel
import com.example.quickchat.constants.Constant
import com.example.quickchat.databinding.ActivityCreateCommunityBinding
import com.example.quickchat.utility.BaseActivity
import com.example.quickchat.utility.PreferenceManager
import com.example.quickchat.utility.UiState
import com.google.firebase.firestore.FirebaseFirestore

class CreateCommunity : BaseActivity() {

    private lateinit var binding: ActivityCreateCommunityBinding
    private val communityViewModel: CommunityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_community)
        preferenceManager = PreferenceManager(this)

        with(binding) {
            createProfileButton.setOnClickListener {
                validateCommunity()
            }

        }


    }

    private fun validateCommunity() {
        with(binding) {
            val communityName = nameInput.text.toString().trim()
            val communityDescription = descriptionInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val mobile = mobileInput.text.toString().trim()
            val address = addressInput.text.toString().trim()

            if (communityName.isNotEmpty()) {
                nameInput.error = "please enter community name"
                commonUtil.showToast("please enter community name")
                return
            }

            if (communityDescription.isNotEmpty()) {
                descriptionInput.error = "please enter community description"
                commonUtil.showToast("please enter community description")
                return
            }

            if (email.isNotEmpty()) {
                emailInput.error = "please enter  email"
                commonUtil.showToast("please enter email")
                return
            }

            if (mobile.isEmpty() || mobile.length != 10) {
                mobileInput.error = "please enter  mobile number"
                commonUtil.showToast("please enter  mobile number")
                return
            }
            if (address.isEmpty()) {
                addressInput.error = "please enter  address"
                commonUtil.showToast("please enter  address")
                return
            }

            val community = CommunityModels(
                communityName = communityName,
                communityDescription = communityDescription,
                email = email,
                mobileNumber = mobile,
                address = address,
                userId = preferenceManager.userId
            )

            preferenceManager.userId?.let {
                communityViewModel.addCommunity(it, community, Constant.ADMIN)
                    .observe(this@CreateCommunity) { it2 ->
                        when (it2) {
                            is UiState.Loading -> {
                                Log.d("TAG", "validateCommunity: loading")
                            }

                            is UiState.Success -> {
                                commonUtil.showToast("community created successfully")
                            }

                            is UiState.Failure -> {

                                commonUtil.showToast(it2.error)
                            }

                            else -> {
                                commonUtil.showToast("Something went wrong")
                            }
                        }
                    }


            }
        }
    }
}
