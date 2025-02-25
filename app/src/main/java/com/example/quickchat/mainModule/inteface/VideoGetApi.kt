package com.example.quickchat.mainModule.inteface

import com.example.quickchat.mainModule.models.VideoGetResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart

interface VideoGetApi {
    @GET("/videos/popular")
    fun getVideo(
        @Header("Authorization") apiKey: String = "WmWIDjAHDqdkzPDHnz8bc5CAvYXTD9GBmzUVm4sJUQ4jSyYPoaGWdSgc"
    )
            : Call<VideoGetResponse>

}