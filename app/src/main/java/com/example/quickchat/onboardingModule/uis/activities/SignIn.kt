package com.example.quickchat.onboardingModule.uis.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.quickchat.R
import com.example.quickchat.constants.Constant
import com.example.quickchat.databinding.ActivitySignInBinding
import com.example.quickchat.mainModule.ui.activity.MainActivity
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignIn : BaseActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var userModel: UserModel
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val Req_Code: Int = 123

    // Initialize ViewModel for handling user onboarding processes
    private val onBoardingModel: OnBoardingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Bind layout using DataBindingUtil
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_in)



        // Configure Google Sign-In options
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1099197774188-k6m1ddg2juua5ofdl8a8f43p7p4opn6l.apps.googleusercontent.com")
            .requestId()
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        // Set up button click listeners
        with(binding) {
            btnSignin.setOnClickListener { login() }
            txSignUp.setOnClickListener {
                startActivity(Intent(this@SignIn, SignUp::class.java))
            }
            btnGoogle.setOnClickListener { signInGoogle() }
        }
    }

    // Handle user login using email and password
    private fun login() {
        with(binding) {
            when {
                etMail.text.toString().isBlank() -> etMail.error = "Please enter email"
                etPassword.text.toString().isBlank() -> etPassword.error = "Please enter password"
                else -> {
                    val email = etMail.text.toString().trim()
                    val password = etPassword.text.toString().trim()

                    onBoardingModel.loginUser(this@SignIn, email, password)
                    onBoardingModel.reg.observe(this@SignIn) {
                        when (it) {
                            is UiState.Loading -> Log.d("statess", "Loading")
                            is UiState.Success -> {
                                Log.d("states", it.data.toString())
                                preferenceManager.isLoggedIn = true
                                startActivity(Intent(this@SignIn, MainActivity::class.java))
                                finish()
                            }
                            is UiState.Failure -> Log.d("states", it.error.toString())
                        }
                    }
                }
            }
        }
    }

    // Initiate Google Sign-In process
    private fun signInGoogle() {
        val signIntent = mGoogleSignInClient.signInIntent
        launcher.launch(signIntent)
    }

    // Handle the result from Google Sign-In activity
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                handleResult(task)
            } catch (e: Exception) {
                commonUtil.showToast("Google Sign-In Failed: ${e.message}")
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

        userModel = UserModel().apply {
            firstName = account.displayName
            email = account.email
        }

        // Call ViewModel to handle Google Sign-In
        onBoardingModel.googleSignIn(this, account, userModel)
        onBoardingModel.gmail.observe(this) {
            when (it) {
                is UiState.Loading -> {

                }

                is UiState.Success -> {

                    preferenceManager.isGmailLoggedIn = true
                    preferenceManager.isLoggedIn = true
                    startActivity(Intent(this@SignIn, MainActivity::class.java))
                    finish()

                }

                is UiState.Failure -> {
                    commonUtil.showToast(it.error)
                }
            }
        }
    }



}
