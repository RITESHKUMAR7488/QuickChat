package com.example.quickchat.mainModule.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class VideoUploadResponse(
    @SerializedName("data") @Expose val data: VideoData? = null
)

data class VideoDatas(
    @SerializedName("video_quality") @Expose val videoQuality: String? = null,
    @SerializedName("test") @Expose val test: Boolean? = null,
    @SerializedName("status") @Expose val status: String? = null,
    @SerializedName("progress") @Expose val progress: Progress? = null,
    @SerializedName("playback_ids") @Expose val playbackIds: List<PlaybackId>? = null,
    @SerializedName("mp4_support") @Expose val mp4Support: String? = null,
    @SerializedName("max_resolution_tier") @Expose val maxResolutionTier: String? = null,
    @SerializedName("master_access") @Expose val masterAccess: String? = null,
    @SerializedName("ingest_type") @Expose val ingestType: String? = null,
    @SerializedName("id") @Expose val id: String? = null,
    @SerializedName("encoding_tier") @Expose val encodingTier: String? = null,
    @SerializedName("created_at") @Expose val createdAt: String? = null
)

data class Progress(
    @SerializedName("state") @Expose val state: String? = null
)

data class PlaybackId(
    @SerializedName("policy") @Expose val policy: String? = null,
    @SerializedName("id") @Expose val id: String? = null
)
