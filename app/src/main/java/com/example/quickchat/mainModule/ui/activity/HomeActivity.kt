package com.example.quickchat.mainModule.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.quickchat.R
import com.example.quickchat.communityModule.ui.activity.ChooseCommunity
import com.example.quickchat.databinding.ActivityMain3Binding
import com.example.quickchat.mainModule.ui.fragments.ChatFragment
import com.example.quickchat.mainModule.ui.fragments.CommunityFragment
import com.example.quickchat.mainModule.ui.fragments.HomeFragment
import com.example.quickchat.mainModule.ui.fragments.ShortFragment
import com.example.quickchat.mainModule.viewmodels.PostViewModel
import com.example.quickchat.onboardingModule.uis.activities.SignIn
import com.example.quickchat.utility.BaseActivity
import com.example.quickchat.utility.UiState
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : BaseActivity() {
    private lateinit var binding: ActivityMain3Binding
    private val postViewModel: PostViewModel by viewModels()
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main3)
        getUSerDetails()

        replaceFragment(HomeFragment())

        with(binding) {
            auth = FirebaseAuth.getInstance()
            bottomNavigationView.setOnNavigationItemSelectedListener { item ->
                when (item.itemId) {

                    R.id.Add -> {
                        val intent = Intent(this@HomeActivity, ChooseCommunity::class.java)
                        startActivity(intent)
                        true
                    }
                    R.id.community -> {
                        replaceFragment(CommunityFragment())
                        true
                    }
                    R.id.home -> {
                        replaceFragment(HomeFragment())
                        true
                    }
                    R.id.shorts -> {
                        replaceFragment(ShortFragment())
                        true
                    }
                    R.id.chat -> {
                        replaceFragment(ChatFragment())
                        true
                    }
                    else -> {
                        replaceFragment(HomeFragment())
                        false
                    }
                }
            }
        }
        val drawerLayout = binding.drawerLayout
        binding.hamburgerIcon.setOnClickListener {
            if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.openDrawer(GravityCompat.START)
            } else {
                Log.d("Hamburger", "Drawer already open")
            }
        }





    }

    private fun getUSerDetails() {
        Log.d("UserIDss",preferenceManager.userId.toString())
        preferenceManager.userId?.let {
            postViewModel.getDetails(it).observe(this) {
                when (it) {
                    is UiState.Loading -> {}
                    is UiState.Success -> {
                        Log.d("datttttaaa", it.data.toString())
                        preferenceManager.userModel = it.data
                        val hview: View = binding.navView.getHeaderView(0)

                        val profileImageUrl = preferenceManager.userModel?.imageUrl
                        val profileImage = hview.findViewById<View>(R.id.circleImageView)
                        fun loadProfilePicture(context: Context, imageUrl: String, imageView: ImageView) {
                            Glide.with(context)
                                .load(imageUrl)
                                .into(imageView)
                        }
                        profileImageUrl?.let { url ->
                            loadProfilePicture(this, url, profileImage as ImageView)
                        }

                        val userName = hview.findViewById<TextView>(R.id.tv_Header_Username)
                        val userEmail = hview.findViewById<TextView>(R.id.tv_email)

                        userName.text = preferenceManager.userModel?.firstName
                        userEmail.text = preferenceManager.userModel?.email


                        val logout = hview.findViewById<View>(R.id.btn_signOut)
                        profileImage.setOnClickListener {
                            val intent = Intent(this, UserDetailActivity::class.java)
                            startActivity(intent)
                        }
                        logout.setOnClickListener {
                            logout()
                        }
                    }
                    is UiState.Failure -> {}
                }
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()

    }
    private fun logout() {
        // Sign out from Firebase
        auth.signOut()

        // Configure Google Sign-In options matching your SignIn activity
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1099197774188-kn7f12q93p57ul4uhklnmtj4ilsh4o9j.apps.googleusercontent.com")
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Sign out from Google
        googleSignInClient.signOut().addOnCompleteListener {
            // Clear preferences and redirect
            val sharedPref = getSharedPreferences("motonew", MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putBoolean("loggedIn", false)
            editor.apply()

            preferenceManager.isLoggedIn = false

            val intent = Intent(this, SignIn::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
