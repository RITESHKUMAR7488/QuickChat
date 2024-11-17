package com.example.quickchat.mainModule.repositories

import android.util.Log
import com.example.quickchat.constants.Constant
import com.example.quickchat.mainModule.models.PostModel
import com.example.quickchat.onboardingModule.models.UserModel
import com.example.quickchat.utility.UiState
import com.google.firebase.firestore.FirebaseFirestore

class MainRepositoryImp(private val database: FirebaseFirestore) : RepositoryMain {
    override fun addPost(
        userId: String,
        communityId: String,
        model: PostModel,
        result: (UiState<PostModel>) -> Unit
    ) {
        database.collection(Constant.USERS).document(userId).collection(Constant.MY_COMMUNITIES)
            .document(communityId).collection(Constant.MY_POST).add(model)
            .addOnSuccessListener { documentReference ->

                val communityId = documentReference.id
                model.communityId = communityId

                database.collection(Constant.POSTS)
                    .document(communityId)
                    .set(model)
                    .addOnSuccessListener {
                        result.invoke(
                            UiState.Success(model)
                        )
                    }
                    .addOnFailureListener { e->
                        result.invoke(
                            UiState.Failure(e.message ?: "An error occurred")
                        )
                    }

            }
            .addOnFailureListener{
                result.invoke(
                    UiState.Failure(it.message ?: "An error occurred")
                )
            }

    }

    override fun getDetails(
        userId: String,
        result: (UiState<UserModel?>) -> Unit
    ) {
        val usersCollection = database.collection(Constant.USERS)
        usersCollection.document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userModel: UserModel? = document.toObject(UserModel::class.java)
                    Log.d("nkss",userModel.toString())
                    result.invoke(UiState.Success(userModel))
                } else {
                    Log.d("Firestore", "No such user found!")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching user details", e)
                result.invoke(UiState.Failure(e.message ?: "An error occurred"))

            }
    }



}