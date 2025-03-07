package com.example.quickchat.mainModule.repositories

import androidx.lifecycle.MutableLiveData
import com.example.quickchat.mainModule.models.AllCommunityModel
import com.example.quickchat.mainModule.models.ImageUploadResponse
import com.example.quickchat.mainModule.models.MainPostModel
import com.example.quickchat.mainModule.models.PostModel
import com.example.quickchat.mainModule.models.VideoGetResponse
import com.example.quickchat.onboardingModule.models.UserModel
import com.example.quickchat.utility.UiState
import java.io.File


interface RepositoryMain {

    fun addPost(communityId:String,model: PostModel, result: (UiState<PostModel>) -> Unit)
    fun getDetails(userId: String,result: (UiState<UserModel?>) -> Unit)
    fun getAllCommunities(userId:String,result: (UiState<List<AllCommunityModel>>) -> Unit)
    fun getAllPost(result: (UiState<List<MainPostModel>>) -> Unit)

    // Function to upload an image file to a server using an API key.
// It takes the following parameters:
// - imageFile: The image file to be uploaded.
// - apiKey: The API key for authentication.
// - data: A MutableLiveData object to hold the response of the image upload.
// - error: A MutableLiveData object to hold any errors that occur during the upload process.
    fun uploadImage(
        imageFile: File,
        apiKey: String,
        data: MutableLiveData<ImageUploadResponse>,
        error: MutableLiveData<Throwable>
    ) {
        // Implementation of the image upload logic would go here.
        // This might include making a network request, handling the response,
        // and updating the `data` or `error` LiveData objects accordingly.
    }

    // Function to retrieve video data from a server.
// It takes the following parameters:
// - data: A MutableLiveData object to hold the response of the video retrieval.
// - error: A MutableLiveData object to hold any errors that occur during the retrieval process.
    fun getVideo(
        data: MutableLiveData<VideoGetResponse>,
        error: MutableLiveData<Throwable>
    )
}