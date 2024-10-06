package com.example.quickchat.onboardingModule.repositories

import android.content.Context
import android.util.Log
import com.example.quickchat.constants.Constant
import com.example.quickchat.onboardingModule.models.UserModel
import com.example.quickchat.utility.UiState
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore

 class OnBoardingRepositoryImpl(
    private val database: FirebaseFirestore,
    private val auth: FirebaseAuth
) : OnBoardingRepository {
    private lateinit var userId: String



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
        teacherModel: UserModel,
        result: (UiState<String>) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun sendUserData(userModel: UserModel, result: (UiState<String>) -> Unit) {
        Log.d("statess", "EnterHare")
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
        teacherModel: UserModel,
        result: (UiState<String>) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun googleSignInSendUserData(
        teacherModel: UserModel,
        result: (UiState<String>) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun userLogin(
        teacherId: String,
        studentId: String,
        result: (UiState<Boolean>) -> Unit
    ) {
        TODO("Not yet implemented")
    }
}