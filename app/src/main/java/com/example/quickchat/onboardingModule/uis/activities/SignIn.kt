package com.example.quickchat.onboardingModule.uis.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
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
            .requestIdToken("1099197774188-kn7f12q93p57ul4uhklnmtj4ilsh4o9j.apps.googleusercontent.com")
            .requestId()
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        // Add this after creating mGoogleSignInClient in onCreate()
        val account = GoogleSignIn.getLastSignedInAccount(this)
        Log.d("GoogleSignIn", "Last signed in account at startup: ${account?.email ?: "None"}")



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
    // Modify your signInGoogle() function to include more logging
    private fun signInGoogle() {
        Log.d("GoogleSignIn", "Starting Google Sign-In process with client ID ending: ${
            "1099197774188-k6m1ddg2juua5ofdl8a8f43p7p4opn6l.apps.googleusercontent.com".takeLast(10)
        }")
        val signIntent = mGoogleSignInClient.signInIntent
        Log.d("GoogleSignIn", "Launching sign-in intent")
        launcher.launch(signIntent)
    }

    // Handle the result from Google Sign-In activity
    // Replace your existing launcher with this improved version
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Log.d("GoogleSignIn", "Activity result received with code: ${result.resultCode}")

        if (result.resultCode == RESULT_OK) {
            Log.d("GoogleSignIn", "Result OK, attempting to get account from intent data")
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                handleResult(task)
            } catch (e: Exception) {
                Log.e("GoogleSignIn", "Exception processing sign-in result", e)
                commonUtil.showToast("Google Sign-In Failed: ${e.message}")
            }
        } else {
            Log.d("GoogleSignIn", "User canceled the sign-in process or it failed with result code: ${result.resultCode}")
            if (result.data != null) {
                val error = result.data?.extras?.get("error")
                Log.d("GoogleSignIn", "Error data from intent: $error")
            }
            commonUtil.showToast("Google Sign-In was canceled or failed")
        }
    }


    private fun handleResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            Log.d("GoogleSignIn", "Successfully retrieved account: ${account?.email}")

            if (account != null) {
                updateUI(account)
            } else {
                Log.e("GoogleSignIn", "Account is null after successful API call")
                commonUtil.showToast("Google Sign-In failed: Account is null")
            }
        } catch (e: ApiException) {
            // Get the error code to diagnose the specific issue
            val statusCode = e.statusCode
            Log.e("GoogleSignIn", "Google Sign-In failed with ApiException code: $statusCode, message: ${e.message}", e)
            commonUtil.showToast("Google Sign-In failed with code $statusCode: ${e.localizedMessage}")
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
