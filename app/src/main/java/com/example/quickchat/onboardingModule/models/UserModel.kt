package com.example.quickchat.onboardingModule.models

import java.io.Serializable

data class UserModel(
    var uid: String? = null,
    var email: String? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var password: String? = null,
    ) : Serializable
