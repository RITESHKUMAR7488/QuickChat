package com.example.quickchat.communityModule.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.quickchat.communityModule.models.CommunityModels
import com.example.quickchat.communityModule.repositories.CommunityRepository
import com.example.quickchat.utility.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(private val repository: CommunityRepository) :
    ViewModel() {

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
}