package com.example.quickchat.mainModule.models


import com.example.quickchat.communityModule.models.CommunityModels

sealed class MainPostModel {
    data class TypeOneItem(val data: PostModel) : MainPostModel()
    data class TypeTwoItem(val data: CommunityModels) : MainPostModel()
    data class CommunityChunk(val data: List<AllCommunityModel>) : MainPostModel() // New type
}
