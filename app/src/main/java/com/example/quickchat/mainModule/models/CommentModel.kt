package com.example.quickchat.mainModule.models

import java.io.Serializable

data class CommentModel(

    var commentId: String? = null,
    var text: String? = null,
    var postId: String? = null,
    var userId: String? = null,
    var role: String? = null,
    var imageUrl: String? = null,
    var likes: Int? = null,
    var timestamp: Long? = null
) : Serializable

