package com.example.quickchat

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.example.quickchat.communityModule.ui.activity.ChooseCommunity
import com.example.quickchat.communityModule.ui.activity.CreateCommunity
import com.example.quickchat.databinding.ActivityMainBinding
import com.example.quickchat.mainModule.models.PostModel
import com.example.quickchat.mainModule.ui.activity.PostActivity
import com.example.quickchat.utility.BaseActivity
import com.example.quickchat.utility.CommonUtil
import javax.inject.Inject

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= DataBindingUtil.setContentView(this,R.layout.activity_main)
        with(binding){
            btnPost.setOnClickListener {
                startActivity(Intent(this@MainActivity,CreateCommunity::class.java))
            }
            textClick.setOnClickListener {
                startActivity(Intent(this@MainActivity,ChooseCommunity::class.java))
            }



        }




    }
}