package com.example.quickchat.communityModule.repositories

import com.example.quickchat.communityModule.models.CommunityModels
import com.google.firebase.firestore.FirebaseFirestore
import com.example.quickchat.utility.UiState

class CommunityRepositoryImpl(private val database: FirebaseFirestore): CommunityRepository {
    override fun addCommunity(
        userId: String,
        model: CommunityModels,
        result: (UiState<CommunityModels>) -> Unit
    ) {

    }


}