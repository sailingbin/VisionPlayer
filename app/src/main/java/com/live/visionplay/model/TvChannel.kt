package com.live.visionplay.model

data class TvChannel(
    val id: String,
    val name: String,
    val streamUrl: String,
    val logoUrl: String? = null // Optional logo URL
)
