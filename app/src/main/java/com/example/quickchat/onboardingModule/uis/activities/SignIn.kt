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
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private val Req_Code: Int = 123

    // Initialize ViewModel for handling user onboarding processes
    private val onBoardingModel: OnBoardingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Bind layout using DataBindingUtil
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_in)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()

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

                    // Call ViewModel to perform login
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

    // Process the Google Sign-In result
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

    // Update UI and handle Google Sign-In logic
    private fun UpdateUI(account: GoogleSignInAccount) {
        // Initialize the user model with Google account data
        userModel = UserModel().apply {
            firstName = account.displayName
            email = account.email
        }

        // Call ViewModel to handle Google Sign-In
        onBoardingModel.googleSignIn(this, account, userModel)
        onBoardingModel.gmail.observe(this) {
            when (it) {
                is UiState.Loading -> {
                    // Handle loading state if necessary
                }

                is UiState.Success -> {
                    // If the Google Sign-In is successful, set preferences for Gmail login
                    preferenceManager.isGmailLoggedIn = true
                    preferenceManager.isLoggedIn = true // Optionally set general login flag

                    // Call the function to send user data to Firestore
                    googleSignInSendUserData(userModel) { result ->
                        when (result) {
                            is UiState.Success -> {
                                // Log success and navigate to MainActivity
                                Log.d("Firestore", "User data stored: ${result.data}")
                                startActivity(Intent(this@SignIn, MainActivity::class.java))
                                finish()
                            }

                            is UiState.Failure -> {
                                // Show error message if something went wrong
                                commonUtil.showToast(result.error)
                            }

                            else -> {}
                        }
                    }
                }

                is UiState.Failure -> {
                    // Handle failure if Google Sign-In fails
                    commonUtil.showToast(it.error)
                }
            }
        }
    }


    // Function to store user data in Firestore after Google Sign-In
    private fun googleSignInSendUserData(userModel: UserModel, result: (UiState<String>) -> Unit) {
        result(UiState.Loading) // Indicate that the process has started

        // Get the current user's ID from Firebase Authentication
        val userId = auth.currentUser?.uid
        if (userId == null) {
            result(UiState.Failure("User ID is null")) // Handle missing user ID
            return
        }

        // Assign the user ID to the user model
        userModel.uid = userId

        // Create Firestore document reference and store user data
        val document = database.collection(Constant.USERS).document(userId)
        document.set(userModel)
            .addOnSuccessListener {
                Log.d("Firestore", "Google user data stored successfully")
                result(UiState.Success("Google user data successfully stored in Firestore"))
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error storing Google user data", e)
                result(UiState.Failure(e.message ?: "Unknown error occurred"))
            }
    }
}
