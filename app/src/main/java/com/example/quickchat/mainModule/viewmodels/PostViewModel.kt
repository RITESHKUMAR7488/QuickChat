package com.example.quickchat.mainModule.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.quickchat.mainModule.models.AllCommunityModel
import com.example.quickchat.mainModule.models.ImageUploadResponse
import com.example.quickchat.mainModule.models.MainPostModel
import com.example.quickchat.mainModule.models.PostModel
import com.example.quickchat.mainModule.models.VideoGetResponse
import com.example.quickchat.mainModule.repositories.RepositoryMain
import com.example.quickchat.onboardingModule.models.UserModel
import com.example.quickchat.onboardingModule.repositories.OnBoardingRepository
import com.example.quickchat.utility.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(private val repository: RepositoryMain,
    private val onBoardingRepository: OnBoardingRepository

) : ViewModel() {

    // Existing methods for other actions
    private val data = MutableLiveData<ImageUploadResponse>()
    private val error = MutableLiveData<Throwable>()

    fun addPost(
        communityId: String, model: PostModel
    ): LiveData<UiState<PostModel>> {
        val successData = MutableLiveData<UiState<PostModel>>()
        successData.value = UiState.Loading
        repository.addPost( communityId, model) {
            successData.value = it
        }
        return successData
    }

    fun getDetails(userId: String): LiveData<UiState<UserModel?>> {
        val successData = MutableLiveData<UiState<UserModel?>>()
        successData.value = UiState.Loading
        repository.getDetails(userId) {
            successData.value = it
        }
        return successData
    }

    fun getAllCommunities(userId: String): LiveData<UiState<List<AllCommunityModel>>> {
        val successData = MutableLiveData<UiState<List<AllCommunityModel>>>()
        successData.value = UiState.Loading
        repository.getAllCommunities(userId) {
            successData.value = it
        }
        return successData
    }

    fun getAllPost(): LiveData<UiState<List<MainPostModel>>> {
        val successData = MutableLiveData<UiState<List<MainPostModel>>>()
        successData.value = UiState.Loading
        repository.getAllPost {
            successData.value = it
        }
        return successData
    }

    fun uploadImage(
        imageFile: File,
        apiKey: String
    ): LiveData<UiState<ImageUploadResponse>> {
        val successData = MutableLiveData<UiState<ImageUploadResponse>>()
        successData.value = UiState.Loading

        val data = MutableLiveData<ImageUploadResponse>()
        val error = MutableLiveData<Throwable>()

        repository.uploadImage(imageFile, apiKey, data, error)

        data.observeForever { response ->
            response?.let {
                successData.value = UiState.Success(it)  // ✅ Update UiState with success response
            }
        }

        error.observeForever { throwable ->
            throwable?.let {
                successData.value = UiState.Failure(it.message ?: "Unknown error")  // ✅ Handle errors
            }
        }

        return successData
    }

    fun getAllVideo(): LiveData<UiState<VideoGetResponse>> {
        val successData = MutableLiveData<UiState<VideoGetResponse>>()
        successData.value = UiState.Loading

        val data = MutableLiveData<VideoGetResponse>() // ✅ Change to List
        val error = MutableLiveData<Throwable>()

        repository.getVideo(data, error) // Ensure repository returns a List

        data.observeForever { response ->
            response?.let {
                successData.value = UiState.Success(it)  // ✅ Store list in UiState
            }
        }

        error.observeForever { throwable ->
            throwable?.let {
                successData.value = UiState.Failure(it.message ?: "Unknown error")
            }
        }

        return successData
    }
    fun updateUser(userModel: UserModel): LiveData<UiState<UserModel>> {
        val successData = MutableLiveData<UiState<UserModel>>()
        successData.value = UiState.Loading
        repository.updateUser(userModel) {
            successData.value = it
        }
        return successData
    }




}
