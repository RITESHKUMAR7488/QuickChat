package com.example.quickchat.communityModule.repositories

import com.example.quickchat.communityModule.models.CommunityModels
import com.example.quickchat.constants.Constant
import com.google.firebase.firestore.FirebaseFirestore
import com.example.quickchat.utility.UiState

class CommunityRepositoryImpl(private val database: FirebaseFirestore) : CommunityRepository {
    override fun addCommunity(
        userId: String,
        model: CommunityModels,
        role: String,
        result: (UiState<CommunityModels>) -> Unit
    ) {
        model.role = role
        database.collection(Constant.USERS).document(Constant.USERID)
            .collection(Constant.MY_COMMUNITIES).add(model)
            .addOnSuccessListener { documentReference ->
                val communityId = documentReference.id
                model.communityId = communityId
                database.collection(Constant.COMMUNITIES)
                    .document(communityId)
                    .set(model)
                    .addOnSuccessListener {
                        result.invoke(UiState.Success(model))
                    }
                    .addOnFailureListener { e ->
                        result.invoke(UiState.Failure(e.message ?: "An error occurred"))
                    }
            }.addOnFailureListener {
                result.invoke(
                    UiState.Failure(
                        it.localizedMessage as String
                    )
                )
            }


    }


}