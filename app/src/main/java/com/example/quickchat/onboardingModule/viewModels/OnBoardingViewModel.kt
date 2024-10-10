package com.example.quickchat.onboardingModule.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.quickchat.onboardingModule.models.UserModel
import com.example.quickchat.onboardingModule.repositories.OnBoardingRepository
import com.example.quickchat.utility.UiState
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnBoardingViewModel @Inject constructor(private val repository: OnBoardingRepository):ViewModel() {

    //=========for registration===================
    private val register= MutableLiveData<UiState<String>>()
    private val gMails=MutableLiveData<UiState<String>>()
    private val login=MutableLiveData<UiState<String>>()
    val reg: LiveData<UiState<String>>
        get()=register
    val gmail:LiveData<UiState<String>>
        get() = gMails

    fun registerUser(context: Context, email: String, passWord:String, model: UserModel){
        register.value= UiState.Loading
        repository.register(context,email,passWord,model){
            register.value=it
        }
    }
    fun loginUser(context: Context, email: String, passWord:String){
        register.value= UiState.Loading
        repository.login(context,email,passWord){
            register.value=it
        }
    }
    fun googleSignIn(context: Context, account: GoogleSignInAccount, model: UserModel) {
        gMails.value = UiState.Loading
        repository.googleSignIn(context, account, model) {
            gMails.value = it
        }
    }

}