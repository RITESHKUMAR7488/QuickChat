package com.example.quickchat.mainModule.models

data class ChatModel(
    val id: String? = null,
    val role: String? = null,
    val name: String? = null,
    val image: String? = null,
    val invisible: Boolean? = null,
    val language: String? = null,
    val banned: Boolean? = null,
    val online: Boolean? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val lastActive: String? = null,
    val totalUnreadCount: Int? = null,
    val unreadChannels: Int? = null,
    val unreadThreads: Int? = null,
    val mutes: List<Any>? = null,
    val teams: List<Any>? = null,
    val channelMutes: List<Any>? = null,
    val blockedUserIds: List<String>? = null,
    val extraData: ExtraData? = null,
    val deactivatedAt: String? = null
)

data class ExtraData(
    val shadow_banned: Boolean? = null
)