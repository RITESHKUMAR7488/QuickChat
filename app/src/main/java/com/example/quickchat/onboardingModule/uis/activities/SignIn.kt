package com.example.quickchat.onboardingModule.uis.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.quickchat.MainActivity
import com.example.quickchat.R
import com.example.quickchat.databinding.ActivitySignInBinding
import com.example.quickchat.onboardingModule.models.UserModel
import com.example.quickchat.onboardingModule.viewModels.OnBoardingViewModel
import com.example.quickchat.utility.BaseActivity
import com.example.quickchat.utility.UiState
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignIn : BaseActivity() {
    private lateinit var binding: ActivitySignInBinding
    private lateinit var userModel: UserModel
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val Req_Code: Int = 123

    private val onBoardingModel: OnBoardingViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_in)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1099197774188-k6m1ddg2juua5ofdl8a8f43p7p4opn6l.apps.googleusercontent.com")
            .requestId()
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        with(binding) {
            btnSignin.setOnClickListener {
                login()
            }
            txSignUp.setOnClickListener {
                val intent = Intent(this@SignIn, SignUp::class.java)
                startActivity(intent)
            }
            btnGoogle.setOnClickListener {
                signInGoogle()

            }

        }
    }

    fun login() {
        with(binding) {
            if (etMail.text.toString().isBlank()) {
                etMail.error = "Please enter email"

            } else if (etPassword.text.toString().isBlank()) {
                etPassword.error = "Please enter password"
            } else {
                val email = etMail.text.toString().trim()
                val password = etPassword.text.toString().trim()
                onBoardingModel.loginUser(this@SignIn, email, password)
                onBoardingModel.reg.observe(this@SignIn) {
                    when (it) {
                        is UiState.Loading -> {
                            Log.d("statess", "Loading")
                        }

                        is UiState.Success -> {
                            Log.d("states", it.data.toString())
                            preferenceManager.isLoggedIn = true
                            val intent = Intent(this@SignIn, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }

                        is UiState.Failure -> {
                            Log.d("states", it.error.toString())
                        }

                    }


                }
            }
        }
    }

    private fun signInGoogle() {
        val signIntent = mGoogleSignInClient.signInIntent
        launcher.launch(signIntent)
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    handleResult(task)

                } catch (e: Exception) {
                    commonUtil.showToast("\"Google Sign-In Failed: ${e.message}\"")
                }
            }


        }

    private fun handleResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                UpdateUI(account)
            }
        } catch (e: ApiException) {
            commonUtil.showToast(e.toString())
        }
    }

    private fun UpdateUI(account: GoogleSignInAccount) {
        userModel=UserModel()
        onBoardingModel.googleSignIn(this, account, userModel)
        onBoardingModel.gmail.observe(this){

            Log.d("observerData",it.toString())


            when (it) {
                is UiState.Loading -> {

                }

                is UiState.Success-> {
                    userModel.firstName = account.displayName
                    userModel.email = account.email
                    val intent = Intent(this@SignIn, MainActivity::class.java)
                    preferenceManager.isGmailLoggedIn = true
                    Log.d("google", account.email.toString())
                    startActivity(intent)
                    finish()
                }

                is UiState.Failure -> {

                    commonUtil.showToast(it.error)
                }
            }

        }


    }
}





