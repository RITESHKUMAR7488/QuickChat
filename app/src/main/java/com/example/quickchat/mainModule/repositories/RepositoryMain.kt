package com.example.quickchat.mainModule.repositories

import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import com.example.quickchat.mainModule.models.AllCommunityModel
import com.example.quickchat.mainModule.models.CommentModel
import com.example.quickchat.mainModule.models.ImageUploadResponse
import com.example.quickchat.mainModule.models.MainPostModel
import com.example.quickchat.mainModule.models.PostModel
import com.example.quickchat.mainModule.models.VideoGetResponse
import com.example.quickchat.mainModule.models.VideoUploadResponse
import com.example.quickchat.onboardingModule.models.UserModel
import com.example.quickchat.utility.UiState
import java.io.File


interface RepositoryMain {

    // Existing methods
    fun addPost(communityId: String, model: PostModel, result: (UiState<PostModel>) -> Unit)
    fun getDetails(userId: String, result: (UiState<UserModel?>) -> Unit)
    fun getAllCommunities(userId: String, result: (UiState<List<AllCommunityModel>>) -> Unit)
    fun getAllPost(result: (UiState<List<MainPostModel>>) -> Unit)
    fun uploadImage(
        imageFile: File,
        apiKey: String,
        data: MutableLiveData<ImageUploadResponse>,
        error: MutableLiveData<Throwable>
    )
    fun uploadVideo(
        videoFile: File,
        apiKey: String,
        data: MutableLiveData<VideoUploadResponse>,
        error: MutableLiveData<Throwable>
    )
    fun getVideo(
        data: MutableLiveData<VideoGetResponse>,
        error: MutableLiveData<Throwable>
    )
    fun updateUser(userModel: UserModel, result: (UiState<UserModel>) -> Unit)

    // New methods for like functionality
    fun likePost(postId: String, userId: String, result: (UiState<PostModel>) -> Unit)
    fun unlikePost(postId: String, userId: String, result: (UiState<PostModel>) -> Unit)
    fun getAllUser(userId: String, result: (UiState<List<UserModel>>) -> Unit)
    fun addComment(postId: String, comment: CommentModel, result: (UiState<CommentModel>) -> Unit)
    fun getComments(postId: String, result: (UiState<List<CommentModel>>) -> Unit)
    fun likeComment(postId: String,commentId: String, userId: String, result: (UiState<CommentModel>) -> Unit)
    fun unlikeComment(postId: String, commentId: String, userId: String, result: (UiState<CommentModel>) -> Unit)

}

