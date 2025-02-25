package com.example.quickchat.mainModule.models

import com.google.gson.annotations.SerializedName

data class VideoGetResponse(
    val page: Int,
    @SerializedName("per_page") val perPage: Int,
    val videos: List<VideoData>,
    @SerializedName("total_results") val totalResults: Int,
    @SerializedName("next_page") val nextPage: String?,
    val url: String
)

data class VideoData(
    val id: Int,
    val width: Int,
    val height: Int,
    val duration: Int,
    @SerializedName("full_res") val fullRes: String?,
    val tags: List<String>,
    val url: String,
    val image: String,
    @SerializedName("avg_color") val avgColor: String?,
    val user: User,
    @SerializedName("video_files") val videoFiles: List<VideoFile>,
    @SerializedName("video_pictures") val videoPictures: List<VideoPicture>
)

data class User(
    val id: Int,
    val name: String,
    val url: String
)

data class VideoFile(
    val id: Int,
    val quality: String,
    @SerializedName("file_type") val fileType: String,
    val width: Int,
    val height: Int,
    val fps: Float,
    val link: String,
    val size: Int
)

data class VideoPicture(
    val id: Int,
    val nr: Int,
    val picture: String
)
