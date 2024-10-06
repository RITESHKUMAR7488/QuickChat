package com.example.quickchat.onboardingModule.uis.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.quickchat.R
import com.example.quickchat.databinding.ActivitySignUpBinding
import com.example.quickchat.onboardingModule.models.UserModel
import com.example.quickchat.onboardingModule.viewModels.OnBoardingViewModel
import com.example.quickchat.utility.BaseActivity
import com.example.quickchat.utility.UiState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUp : BaseActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private val onBoardingModel: OnBoardingViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_sign_up)
        with(binding){
            txSignIn.setOnClickListener{
                val intent= Intent(this@SignUp, SignIn::class.java)
                startActivity(intent)
                finish()
            }
            button.setOnClickListener{
                validation()
            }
        }
    }

    private fun validation() {


        val email=binding.etMail.text.toString().trim()
        val password=binding.etPassword.text.toString().trim()
        val firstName=binding.etFirstName.text.toString().trim()
        val lastName=binding.etLastName.text.toString().trim()
        val rePassword=binding.etConfirmPassword.text.toString().trim()


        if (firstName.isBlank()){
            binding.etFirstName.error="Please enter name"
        }
       else if (lastName.isBlank()){
            binding.etLastName.error="Please enter name"
        }
        else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.etMail.error="Invalid email"
        }
        else if (password.length<8){
            binding.etPassword.error="Password must be at least 8 character"
        }
        else if (rePassword!=password){
            binding.etConfirmPassword.error="Password does not match"
        }

        else{
            val model= UserModel()
            model.email=email
            model.password=password
            model.firstName=firstName
            model.lastName=lastName

            onBoardingModel.registerUser(this,email=email,passWord=password, model = model)
            onBoardingModel.reg.observe(this){
                Log.d("registers",it.toString())
                when(it){
                    is UiState.Loading ->{
                        Log.d("statess","Loading")

                    }
                    is UiState.Success ->{
                        Log.d("states",it.data.toString())

                    }
                    is UiState.Failure ->{
                        Log.d("states",it.error.toString())

                    }
                }
            }
        }


    }
}