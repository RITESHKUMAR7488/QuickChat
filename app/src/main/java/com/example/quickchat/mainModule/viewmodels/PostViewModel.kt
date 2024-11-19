package com.example.quickchat.mainModule.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.quickchat.mainModule.models.AllCommunityModel
import com.example.quickchat.mainModule.models.PostModel
import com.example.quickchat.mainModule.repositories.RepositoryMain
import com.example.quickchat.onboardingModule.models.UserModel
import com.example.quickchat.utility.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(private val repository: RepositoryMain) : ViewModel() {
    fun addPost(
        userId: String, communityId: String, model: PostModel
    ): LiveData<UiState<PostModel>> {
        val successData = MutableLiveData<UiState<PostModel>>()
        successData.value = UiState.Loading
        repository.addPost(userId, communityId, model) {
            successData.value = it
        }
        return successData
    }

    fun getDetails(userId: String): LiveData<UiState<UserModel?>> {
        val successData = MutableLiveData<UiState<UserModel?>>()
        successData.value = UiState.Loading
        repository.getDetails(userId) {
            successData.value = it
        }
        return successData
    }
    fun getAllCommunities(userId: String): LiveData<UiState<List<AllCommunityModel>>> {
        val successData = MutableLiveData<UiState<List<AllCommunityModel>>>()
        successData.value = UiState.Loading
        repository.getAllCommunities(userId) {
            successData.value = it
        }
        return successData

    }
}