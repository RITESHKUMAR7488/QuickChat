package com.example.quickchat.onboardingModule.uis.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.quickchat.R
import com.example.quickchat.databinding.ActivitySplashScreenBinding
import com.example.quickchat.mainModule.ui.activity.MainActivity
import com.example.quickchat.utility.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashScreen : BaseActivity() {
    private lateinit var binding: ActivitySplashScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= DataBindingUtil.setContentView(this,R.layout.activity_splash_screen)
        with(binding){
            if (preferenceManager.isLoggedIn){
                Handler(Looper.getMainLooper()).postDelayed({

                    startActivity(Intent(this@SplashScreen, MainActivity::class.java))
                    finish()

                },3000)
            }
            else{
                Handler(Looper.getMainLooper()).postDelayed({

                    startActivity(Intent(this@SplashScreen, SignUp::class.java))
                    finish()

                },3000)
            }


        }

    }
}