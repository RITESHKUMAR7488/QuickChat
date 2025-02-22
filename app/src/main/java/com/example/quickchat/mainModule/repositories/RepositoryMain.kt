package com.example.quickchat.mainModule.repositories

import androidx.lifecycle.MutableLiveData
import com.example.quickchat.mainModule.models.AllCommunityModel
import com.example.quickchat.mainModule.models.ImageUploadResponse
import com.example.quickchat.mainModule.models.MainPostModel
import com.example.quickchat.mainModule.models.PostModel
import com.example.quickchat.onboardingModule.models.UserModel
import com.example.quickchat.utility.UiState
import java.io.File


interface RepositoryMain {

    fun addPost(communityId:String,model: PostModel, result: (UiState<PostModel>) -> Unit)
    fun getDetails(userId: String,result: (UiState<UserModel?>) -> Unit)
    fun getAllCommunities(userId:String,result: (UiState<List<AllCommunityModel>>) -> Unit)
    fun getAllPost(result: (UiState<List<MainPostModel>>) -> Unit)

    fun uploadImage(
        imageFile: File,
        apiKey: String,
        data: MutableLiveData<ImageUploadResponse>,
        error: MutableLiveData<Throwable>
    )
}