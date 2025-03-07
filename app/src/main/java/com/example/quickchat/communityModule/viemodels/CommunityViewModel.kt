package com.example.quickchat.communityModule.viemodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.quickchat.communityModule.models.CommunityModels
import com.example.quickchat.communityModule.repositories.CommunityRepository
import com.example.quickchat.mainModule.models.PostModel
import com.example.quickchat.utility.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(private val repository: CommunityRepository) :
    ViewModel() {

    // Add a new community
    fun addCommunity(
        userId: String,
        model: CommunityModels,
        role: String
    ): LiveData<UiState<CommunityModels>> {
        val successData = MutableLiveData<UiState<CommunityModels>>()
        successData.value = UiState.Loading
        repository.addCommunity(userId, model, role) {
            successData.value = it
        }
        return successData
    }

    // Get all communities for a user
    fun getCommunity(userId: String): LiveData<UiState<List<CommunityModels>>> {
        val successData = MutableLiveData<UiState<List<CommunityModels>>>()
        successData.value = UiState.Loading
        repository.getCommunity(userId) {
            successData.value = it
        }
        return successData
    }

    // Get details of a specific community
    fun getCommunityDetails(communityId: String): LiveData<UiState<CommunityModels>> {
        val successData = MutableLiveData<UiState<CommunityModels>>()
        successData.value = UiState.Loading
        repository.getCommunityDetails(communityId) {
            successData.value = it
        }
        return successData
    }

    // Get posts for a specific community
    fun getCommunityPost(communityId: String): LiveData<UiState<List<PostModel>>> {
        val successData = MutableLiveData<UiState<List<PostModel>>>()
        successData.value = UiState.Loading
        repository.getCommunityPosts(communityId) {
            successData.value = it
        }
        return successData
    }

    // Update a community
    fun updateCommunity(
        userId: String,
        communityId: String,
        updatedModel: CommunityModels
    ): LiveData<UiState<CommunityModels>> {
        val successData = MutableLiveData<UiState<CommunityModels>>()
        successData.value = UiState.Loading
        repository.updateCommunity(userId,communityId, updatedModel) {
            successData.value = it
        }
        return successData
    }
}