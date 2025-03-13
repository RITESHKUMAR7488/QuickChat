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
import com.example.quickchat.constants.Constant
import com.example.quickchat.databinding.ActivitySignInBinding
import com.example.quickchat.mainModule.ui.activity.HomeActivity
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

        }
    }

    // Handle user login using email and password
    private fun login() {
        with(binding) {
            Log.d("Logincccccc", "Login function called")

            when {
                etMail.text.toString().isBlank() -> {
                    Log.d("Login", "Email field is blank")
                    etMail.error = "Please enter email"
                }
                etPassword.text.toString().isBlank() -> {
                    Log.d("Login", "Password field is blank")
                    etPassword.error = "Please enter password"
                }
                else -> {
                    val email = etMail.text.toString().trim()
                    val password = etPassword.text.toString().trim()

                    Log.d("Logingseghaeh", "Attempting to log in with email: $email")

                    // Call ViewModel to handle login
                    Log.d("Logincuoi", "Calling onBoardingModel.loginUser")
                    onBoardingModel.loginUser(this@SignIn, email, password)

                    // Observe the result of the login process
                    onBoardingModel.reg.observe(this@SignIn) { state ->
                        Log.d("jshdfjhse", state.toString())
                        when (state) {
                            is UiState.Loading -> {
                                Log.d("Login", "Login in progress...")
                            }
                            is UiState.Success -> {
                                Log.d("Logingcvdtsdvb", "Login successful: ${state.data}")
                                Log.d("Login", "Navigating to HomeActivity")

                                preferenceManager.isLoggedIn = true
                                startActivity(Intent(this@SignIn, HomeActivity::class.java))
                                finish()
                            }
                            is UiState.Failure -> {
                                Log.e("Login", "Login failed: ${state.error}")
                            }
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
        Log.d("GoogleSignInhcfdjhf", "handleResult called")

        try {
            Log.d("GoogleSignIn", "Attempting to get GoogleSignInAccount from task")
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)

            if (account != null) {
                Log.d("GoogleSignIn", "GoogleSignInAccount successfully retrieved: ${account.email}")
                updateUI(account)
            } else {
                Log.e("GoogleSignIn", "GoogleSignInAccount is null")
                commonUtil.showToast("Google Sign-In failed: Account is null")
            }
        } catch (e: ApiException) {
            Log.e("GoogleSignIn", "Google Sign-In failed with ApiException: ${e.message}", e)
            commonUtil.showToast("Google Sign-In failed: ${e.message}")
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "Google Sign-In failed with unexpected exception: ${e.message}", e)
            commonUtil.showToast("Google Sign-In failed: ${e.message}")
        }
    }


    private fun updateUI(account: GoogleSignInAccount) {
        Log.d("GoogleSignIndb", "UpdateUI called with account: ${account.email}")

        userModel = UserModel().apply {
            firstName = account.displayName
            email = account.email
        }

        // Call ViewModel to handle Google Sign-In
        Log.d("GoogleSignIn", "Calling onBoardingModel.googleSignIn")
        onBoardingModel.googleSignIn(this, account, userModel)

        onBoardingModel.gmail.observe(this) { state ->
            Log.d("GoogleSignIn", "Observed state: $state")
            when (state) {
                is UiState.Loading -> {
                    Log.d("GoogleSignIn", "Google Sign-In in progress...")
                }
                is UiState.Success -> {
                    Log.d("GoogleSignIn", "Google Sign-In successful: ${state.data}")

                    preferenceManager.isGmailLoggedIn = true
                    preferenceManager.isLoggedIn = true

                    Log.d("GoogleSignIn", "Navigating to HomeActivity")
                    startActivity(Intent(this@SignIn, HomeActivity::class.java))
                    finish()
                }
                is UiState.Failure -> {
                    Log.e("GoogleSignIn", "Google Sign-In failed: ${state.error}")
                    commonUtil.showToast(state.error)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.btnGoogle.setOnClickListener { signInGoogle() }
    }



}
