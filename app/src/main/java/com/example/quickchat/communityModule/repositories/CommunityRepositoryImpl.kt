package com.example.quickchat.communityModule.repositories

import com.example.quickchat.communityModule.models.CommunityModels
import com.example.quickchat.constants.Constant
import com.google.firebase.firestore.FirebaseFirestore
import com.example.quickchat.utility.UiState

class CommunityRepositoryImpl(private val database: FirebaseFirestore) : CommunityRepository {

    // Adds a new community to both the user's list of communities and the global community list in Firestore.
    override fun addCommunity(
        userId: String, // ID of the user adding the community
        model: CommunityModels, // Community model object containing community details
        role: String, // Role of the user within this community
        result: (UiState<CommunityModels>) -> Unit // Callback to handle the result of the operation
    ) {
        // Set the role in the community model
        model.role = role

        // Add the community model under the user's "My Communities" collection in Firestore
        database.collection(Constant.USERS).document(userId)
            .collection(Constant.MY_COMMUNITIES).add(model)
            .addOnSuccessListener { documentReference ->

                // Get the newly created document ID as community ID
                val communityId = documentReference.id
                model.communityId = communityId // Set community ID in the model

                // Store the community in the main "Communities" collection
                database.collection(Constant.COMMUNITIES)
                    .document(communityId)
                    .set(model)
                    .addOnSuccessListener {
                        // If successful, invoke the success callback with the updated model
                        result.invoke(UiState.Success(model))
                    }
                    .addOnFailureListener { e ->
                        // If there is an error, invoke the failure callback with the error message
                        result.invoke(UiState.Failure(e.message ?: "An error occurred"))
                    }
            }
            .addOnFailureListener {
                // If the initial add operation fails, invoke the failure callback with the error message
                result.invoke(
                    UiState.Failure(
                        it.localizedMessage ?: "An error occurred"
                    )
                )
            }
    }

    override fun getCommunity(userId: String, result: (UiState<List<CommunityModels>>) -> Unit) {
        database.collection(Constant.USERS).document(userId).collection(Constant.MY_COMMUNITIES).get().addOnSuccessListener {
            val communityList = arrayListOf<CommunityModels>()
            for (document in it) {
                val community = document.toObject(CommunityModels::class.java)
                community.communityId = document.id
                communityList.add(community)
            }
            result.invoke(UiState.Success(communityList))
        }.addOnFailureListener {
            result.invoke(UiState.Failure(it.localizedMessage ?: "An error occurred"))
        }


    }
}



