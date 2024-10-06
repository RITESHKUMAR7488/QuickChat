package com.example.quickchat.uis

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.example.quickchat.R
import com.example.quickchat.databinding.ActivitySignInBinding


class SignIn : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= DataBindingUtil.setContentView(this,R.layout.activity_sign_in)
        with(binding){
            txSignUp.setOnClickListener {
                val intent= Intent(this@SignIn,SignUp::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}