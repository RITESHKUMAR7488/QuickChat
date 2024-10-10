package com.example.quickchat.onboardingModule.repositories

import android.content.Context
import android.util.Log
import com.example.quickchat.constants.Constant
import com.example.quickchat.onboardingModule.models.UserModel
import com.example.quickchat.utility.PreferenceManager
import com.example.quickchat.utility.UiState
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class OnBoardingRepositoryImpl(
    private val database: FirebaseFirestore,
    private val auth: FirebaseAuth
) : OnBoardingRepository {
    private lateinit var userId: String
    @Inject
    lateinit var preferenceManager:PreferenceManager



    override fun register(
        context: Context,
        email: String,
        password: String,
        userModel: UserModel,
        result: (UiState<String>) -> Unit
    ) {
        Log.d("statess", email+password)

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            Log.d("statess", auth.currentUser?.uid ?: "")
            userId = auth.currentUser?.uid ?: ""
            Log.d("statess", it.toString())

            if (it.isSuccessful) {
                sendUserData(userModel) { state ->
                    when (state) {
                        is UiState.Success -> {
                            result.invoke(UiState.Success("user register successfully"))
                        }
                        is UiState.Failure -> {
                            result.invoke(UiState.Failure(state.error))
                        }
                        is UiState.Loading -> {
                            result.invoke(UiState.Loading)
                        }
                    }
                }


            } else {
                try {
                    throw it.exception ?: java.lang.Exception("invalid authentication")
                } catch (e: FirebaseAuthWeakPasswordException) {
                    result.invoke(UiState.Failure("Authentication failed, password must be at least 6 chracter"))
                } catch (e: FirebaseAuthInvalidCredentialsException) {
                    result.invoke(UiState.Failure("Authentication failed,Invalid email"))
                } catch (e: FirebaseAuthUserCollisionException) {
                    result.invoke(UiState.Failure("Authentication failed,Email already registered"))
                } catch (e: Exception) {
                    e.message?.let { it1 -> UiState.Failure(it1) }
                        ?.let { it2 -> result.invoke(it2) }
                }
            }
        }.addOnFailureListener {
            it.localizedMessage?.let { it1 -> UiState.Failure(it1) }
                ?.let { it2 -> result.invoke(it2) }
        }

    }

     override fun login(
         context: Context,
         email: String,
         password: String,
         result: (UiState<String>) -> Unit
     ) {
         auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
             if(it.isSuccessful){
                 Log.d("userId",auth.currentUser!!.uid)
                 userId= auth.currentUser!!.uid
                 result.invoke(UiState.Success("Login successfully"))



             }else {
                 try {
                     throw it.exception ?: java.lang.Exception("invalid authentication")
                 } catch (e: FirebaseAuthWeakPasswordException) {
                     result.invoke(UiState.Failure("Authentication failed, password must be at least 6 chracter"))
                 } catch (e: FirebaseAuthInvalidCredentialsException) {
                     result.invoke(UiState.Failure("Authentication failed,Invalid email or password"))
                 } catch (e: FirebaseAuthUserCollisionException) {
                     result.invoke(UiState.Failure("Authentication failed,Email already registered"))
                 } catch (e: Exception) {
                     e.message?.let { it1 -> UiState.Failure(it1) }
                         ?.let { it2 -> result.invoke(it2) }
                 }

             }

         }

     }



    override fun sendUserData(userModel: UserModel, result: (UiState<String>) -> Unit) {
        Log.d("statess", "EnterHere")
        val document = database.collection(Constant.USERS).document(userId)

        userModel.uid=userId
        document.set(userModel).addOnSuccessListener {
            Log.d("succes", "succes2")
            result.invoke(
                UiState.Success("User register successfully")
            )
        }.addOnFailureListener {
            UiState.Failure(it.localizedMessage)
            Log.d("successs", it.localizedMessage.toString())
        }
    }

    override fun googleSignIn(
        context: Context,
        account: GoogleSignInAccount,
        userModel: UserModel,
        result: (UiState<String>) -> Unit
    ) {
        // Notify that the process is loading
        result(UiState.Loading)

        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        preferenceManager = PreferenceManager(context)

        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser?.uid
                Log.d("authss", "Sign-in successful, userId: $userId")

                if (userId != null) {
                    userModel.firstName = account.givenName ?: "N/A"
                    userModel.lastName = account.familyName ?: "N/A"
                    userModel.email = account.email ?: "N/A"
                    userModel.uid = userId

                    preferenceManager.email = account.email
                    googleSignInSendUserData(userModel) { userDataResult ->
                        result(userDataResult)
                    }
                } else {
                    result(UiState.Failure("User ID is null"))
                }
            } else {
                Log.e("auth", "Sign-in failed", task.exception)
                result(UiState.Failure(task.exception?.message ?: "Unknown error occurred"))
            }
        }
    }

    override fun googleSignInSendUserData(
        userModel: UserModel,
        result: (UiState<String>) -> Unit
    ) {
        result(UiState.Loading)

        val userId = auth.currentUser?.uid
        if (userId == null) {
            result(UiState.Failure("User ID is null"))
            return
        }

        userModel.uid = userId

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