package com.example.quickchat.mainModule.inteface

import com.example.quickchat.mainModule.models.VideoUploadResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface VideoUploadApi {


        @Multipart
        @POST("video/v1/uploads") // Updated endpoint for video uploads
        fun uploadVideo(
            @Query("key") apiKey: String, // API key for authentication
            @Query("action") action: String = "upload", // Optional: Specify the action
            @Part video: MultipartBody.Part // Video file to upload
        ): Call<VideoUploadResponse> // Response type for video upload

}