package com.example.quickchat.mainModule.ui.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.example.quickchat.R
import com.example.quickchat.databinding.ActivityNewChatBinding
import com.example.quickchat.mainModule.ui.adapters.UserAdapter
import com.example.quickchat.onboardingModule.models.UserModel

class NewChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewChatBinding
    private lateinit var adapter: UserAdapter
    private var userList= listOf<UserModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= DataBindingUtil.setContentView(this,R.layout.activity_new_chat)
        with(binding){

        }

    }
    private fun setUpRecycyclerView(){
        adapter=UserAdapter(userList,this){ user->


        }
        binding.recyclerView.adapter=adapter


    }
}