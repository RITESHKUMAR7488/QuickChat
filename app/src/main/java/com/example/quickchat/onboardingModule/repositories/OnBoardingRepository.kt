package com.example.quickchat.onboardingModule.repositories

import android.content.Context
import com.example.quickchat.onboardingModule.models.UserModel
import com.example.quickchat.utility.UiState
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

interface OnBoardingRepository {
    fun register(context: Context, email:String, password:String, userModel: UserModel, result:(UiState<String>)-> Unit )
    fun login(context: Context, email:String, password:String, teacherModel: UserModel, result:(UiState<String>)-> Unit )
    fun sendUserData(userModel: UserModel, result:(UiState<String>)-> Unit )
    fun googleSignIn(context: Context, account: GoogleSignInAccount, teacherModel: UserModel, result:(UiState<String>)-> Unit )
    fun googleSignInSendUserData(teacherModel: UserModel, result:(UiState<String>)-> Unit )
    fun userLogin(teacherId:String,studentId:String,result: (UiState<Boolean>) -> Unit)
}