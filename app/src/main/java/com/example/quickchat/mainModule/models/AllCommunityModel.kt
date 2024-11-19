package com.example.quickchat.mainModule.models

import java.io.Serializable

data class AllCommunityModel(
    var communityId: String? = null,
    var communityName: String? = null,
    var communityDescription: String? = null,
    var role:String?=null,
    var imageUrl: String? = null,
    var email: String? = null,
    var mobileNumber: String? = null,
    var address: String? = null,
    var userId: String? = null,
): Serializable
