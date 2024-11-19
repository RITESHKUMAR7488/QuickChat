package com.example.quickchat.mainModule.repositories

import com.example.quickchat.mainModule.models.AllCommunityModel
import com.example.quickchat.mainModule.models.PostModel
import com.example.quickchat.onboardingModule.models.UserModel
import com.example.quickchat.utility.UiState


interface RepositoryMain {

    fun addPost(userId:String,communityId:String,model: PostModel, result: (UiState<PostModel>) -> Unit)
    fun getDetails(userId: String,result: (UiState<UserModel?>) -> Unit)
    fun getAllCommunities(userId:String,result: (UiState<List<AllCommunityModel>>) -> Unit)
}