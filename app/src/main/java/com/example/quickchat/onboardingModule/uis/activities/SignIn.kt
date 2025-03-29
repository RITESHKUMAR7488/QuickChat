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
import android.app.AlertDialog
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText

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
            binding.btnGoogle.setOnClickListener { signInGoogle() }
            forgotPasswordText.setOnClickListener {
                showForgotPasswordDialog()
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
        Log.d("GoogleSignIn", "Starting Google Sign-In process")
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
        Log.d("GoogleSignIn", "UpdateUI called with account: ${account.email}")

        userModel = UserModel().apply {
            firstName = account.displayName ?: "N/A"
            lastName = account.familyName ?: "N/A"
            email = account.email ?: "N/A"
            // Ensure other required fields are set if needed
        }

        // Set a log to check if this is called
        Log.d("GoogleSignIn", "About to call onBoardingModel.googleSignIn")
        onBoardingModel.googleSignIn(this, account, userModel)

        // Add a log to verify observation setup
        Log.d("GoogleSignIn", "Setting up observer for gmail LiveData")
        onBoardingModel.gmail.observe(this) { state ->
            Log.d("GoogleSignIn", "Observed state: $state")
            when (state) {
                is UiState.Loading -> {
                    Log.d("GoogleSignIn", "Google Sign-In in progress...")
                    // Consider showing a loading indicator here
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

    // Add this method to your SignIn.kt class
    private fun showForgotPasswordDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_forgot_password, null)
        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)

        val alertDialog = builder.create()
        alertDialog.show()

        val emailEditText = dialogView.findViewById<EditText>(R.id.et_reset_email)
        val cancelButton = dialogView.findViewById<Button>(R.id.btn_cancel)
        val resetButton = dialogView.findViewById<Button>(R.id.btn_reset)

        cancelButton.setOnClickListener {
            alertDialog.dismiss()
        }

        resetButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isEmpty()) {
                emailEditText.error = "Please enter your email"
                return@setOnClickListener
            }

            // Call ViewModel to reset password
            onBoardingModel.resetUserPassword(email)


            // Observe the result
            onBoardingModel.resetPassword.observe(this) { state ->
                when (state) {
                    is UiState.Loading -> {
                        // Already showing loading
                    }
                    is UiState.Success -> {

                        alertDialog.dismiss()
                        commonUtil.showToast(state.data)
                    }
                    is UiState.Failure -> {
                        
                        commonUtil.showToast(state.error)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

    }



}
