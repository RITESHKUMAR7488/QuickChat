package com.example.quickchat.onboardingModule.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.quickchat.onboardingModule.models.UserModel
import com.example.quickchat.onboardingModule.repositories.OnBoardingRepository
import com.example.quickchat.utility.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnBoardingViewModel @Inject constructor(private val repository: OnBoardingRepository):ViewModel() {

    //=========for registration===================
    private val register= MutableLiveData<UiState<String>>()
    val reg: LiveData<UiState<String>>
        get()=register

    fun registerUser(context: Context, email: String, passWord:String, model: UserModel){
        register.value= UiState.Loading
        repository.register(context,email,passWord,model){
            register.value=it
        }
    }
    //=========for registration===================
}