package com.live.visionplay.data

import com.google.gson.annotations.SerializedName

/**
 * 用于表示从服务器获取的版本信息的数据模型。
 *
 * @property versionCode 版本号，用于程序判断是否更新。
 * @property versionName 版本名称，用于UI展示，例如 "1.0.1"。
 * @property releaseNotes 更新日志列表，每条是一个字符串。
 * @property downloadUrl 新版本的下载地址。
 */
data class VersionInfo(
    @SerializedName("versionCode")
    val versionCode: Int,

    @SerializedName("versionName")
    val versionName: String,

    @SerializedName("releaseNotes")
    val releaseNotes: List<String>,

    @SerializedName("downloadUrl")
    val downloadUrl: String
)
