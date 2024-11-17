package com.example.quickchat.communityModule.repositories

import com.example.quickchat.communityModule.models.CommunityModels
import com.example.quickchat.utility.UiState



interface CommunityRepository {

    fun addCommunity(userId: String,model: CommunityModels, role:String,result: (UiState<CommunityModels>) -> Unit)
    fun getCommunity(userId: String,result: (UiState<List<CommunityModels>>) -> Unit)

}