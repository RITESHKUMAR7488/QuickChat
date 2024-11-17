package com.example.quickchat.onboardingModule.repositories

import android.content.Context
import com.example.quickchat.onboardingModule.models.UserModel
import com.example.quickchat.utility.UiState
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

interface OnBoardingRepository {
    fun register(context: Context, email:String, password:String, userModel: UserModel, result:(UiState<String>)-> Unit )
    fun login(context: Context, email:String, password:String, result:(UiState<String>)-> Unit )
    fun sendUserData( context: Context,userModel: UserModel, result:(UiState<String>)-> Unit )
    fun googleSignIn(context: Context, account: GoogleSignInAccount, userModel: UserModel, result:(UiState<String>)-> Unit )
    fun googleSignInSendUserData(context: Context,teacherModel: UserModel, result:(UiState<String>)-> Unit )

}