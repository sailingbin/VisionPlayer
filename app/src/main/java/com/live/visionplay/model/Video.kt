package com.live.visionplay.model

import android.net.Uri

data class Video(
    val uri: Uri,
    val name: String,
    val duration: Int,
    val size: Long,
    val path: String
)
