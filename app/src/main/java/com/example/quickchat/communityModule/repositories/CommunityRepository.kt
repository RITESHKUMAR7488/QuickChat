package com.example.quickchat.communityModule.repositories

import com.example.quickchat.communityModule.models.CommunityModels
import com.example.quickchat.utility.UiState



interface CommunityRepository {

    fun addCommunity(communityId: String, communityName: String, communityDescription: String, result: (UiState<CommunityModels>) -> Unit)

}