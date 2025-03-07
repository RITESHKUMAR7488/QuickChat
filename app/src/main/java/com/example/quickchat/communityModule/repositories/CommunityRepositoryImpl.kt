package com.example.quickchat.communityModule.repositories

import android.util.Log
import com.example.quickchat.communityModule.models.CommunityModels
import com.example.quickchat.constants.Constant
import com.example.quickchat.mainModule.models.PostModel
import com.google.firebase.firestore.FirebaseFirestore
import com.example.quickchat.utility.UiState
import com.google.firebase.firestore.SetOptions

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

    override fun getCommunityDetails(
        communityId: String,
        result: (UiState<CommunityModels>) -> Unit
    ) {
        Log.d("CommunityRepositoryImpl", "Fetching details for communityId: $communityId")
        database.collection(Constant.COMMUNITIES)
            .document(communityId)
            .get()
            .addOnSuccessListener { document ->
                Log.d("CommunityRepositoryImpl", "Document: $document")
                if (document.exists()) {
                    val community = document.toObject(CommunityModels::class.java)
                    Log.d("CommunityRepositoryImpl", "Parsed community: $community")
                    if (community != null) {
                        community.communityId = communityId
                        result.invoke(UiState.Success(community))
                    } else {
                        result.invoke(UiState.Failure("Failed to parse community details"))
                    }
                } else {
                    result.invoke(UiState.Failure("Community not found"))
                }
            }
            .addOnFailureListener { exception ->
                Log.e("CommunityRepositoryImpl", "Error fetching community details", exception)
                result.invoke(UiState.Failure(exception.localizedMessage ?: "An error occurred"))
            }
    }

    override fun getCommunityPosts(
        communityId: String,
        result: (UiState<List<PostModel>>) -> Unit
    ) {
        database.collection(Constant.COMMUNITIES).document(communityId).collection(Constant.MY_POST).get().addOnSuccessListener {
            val postsList = arrayListOf<PostModel>()
            for (document in it) {
                val posts = document.toObject(PostModel::class.java)
                posts.postId = document.id
                postsList.add(posts)
            }
            result.invoke(UiState.Success(postsList))
        }.addOnFailureListener {
            result.invoke(UiState.Failure(it.localizedMessage ?: "An error occurred"))
        }
    }

    override fun updateCommunity(
        userId: String,
        communityId: String,
        updatedModel: CommunityModels,
        result: (UiState<CommunityModels>) -> Unit
    ) {
        updatedModel.userId=userId
        updatedModel.communityId=communityId
        database.collection(Constant.COMMUNITIES).document(communityId).set(updatedModel).addOnSuccessListener {
            database.collection(Constant.USERS).document(userId).collection(Constant.MY_COMMUNITIES).document(communityId).set(updatedModel).addOnSuccessListener{
                result.invoke(UiState.Success(updatedModel))
            }.addOnFailureListener{
                result.invoke(UiState.Failure(it.localizedMessage ?: "An error occurred"))


            }



        }.addOnFailureListener{
            result.invoke(UiState.Failure(it.localizedMessage ?: "An error occurred"))

        }

    }


}




