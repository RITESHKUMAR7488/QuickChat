package com.example.quickchat.communityModule.models

import java.io.Serializable

data class CommunityModels(

    var communityId: String? = null,
    var communityName: String? = null,
    var communityDescription: String? = null,
    var imageUrl: String? = null,
    var email: String? = null,
    var mobileNumber: String? = null,
    var address: String? = null,
    var id: String? = null,
    var userId: String? = null,
): Serializable
