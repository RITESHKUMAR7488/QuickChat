package com.example.quickchat.mainModule.models

import java.io.Serializable

data class PostModel(
    var communityId: String? = null,
    var userId: String? = null,
    var title: String? = null,
    var description: String? = null,
    var imageUrls: List<String>? = null,
    var detailModel: DetailModel,
    var postId:String?=null

):Serializable
