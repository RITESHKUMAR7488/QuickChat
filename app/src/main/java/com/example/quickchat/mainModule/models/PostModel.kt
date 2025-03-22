package com.example.quickchat.mainModule.models

import com.example.quickchat.onboardingModule.models.UserModel
import java.io.Serializable

data class PostModel(
    var communityId: String? = null,
    var userId: String? = null,
    var title: String? = null,
    var description: String? = null,
    var imageUrls: List<String>? = null,
    var detailModel: DetailModel?=null,
    var postId:String?=null,
    var imageUrl: String? = null,
    var likes: List<String>? = null,
    var userModels: List<UserModel>? = null,

    ):Serializable
