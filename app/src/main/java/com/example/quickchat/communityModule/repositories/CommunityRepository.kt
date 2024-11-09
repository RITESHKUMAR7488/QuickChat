package com.example.quickchat.communityModule.repositories

import com.example.quickchat.communityModule.models.CommunityModels
import com.example.quickchat.utility.UiState



interface CommunityRepository {

    fun addCommunity(userId: String,model: CommunityModels, result: (UiState<CommunityModels>) -> Unit)

}