package com.live.visionplay.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * 本地视频实体
 *
 * 存储扫描到的本地视频文件信息
 */
@Entity(
    tableName = "videos",
    indices = [
        Index(value = ["filePath"], unique = true),
        Index(value = ["folderPath"]),
        Index(value = ["addedTime"])
    ]
)
data class VideoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 文件信息
    val fileName: String,
    val filePath: String,
    val folderPath: String,
    val fileSize: Long,            // 文件大小（字节）
    val mimeType: String,

    // 视频元数据
    val duration: Long,            // 时长（毫秒）
    val width: Int,                // 视频宽度
    val height: Int,               // 视频高度
    val bitrate: Long,             // 比特率
    val frameRate: Float,          // 帧率

    // 缩略图
    val thumbnailPath: String? = null,  // 缩略图路径

    // 播放记录
    val lastPlayPosition: Long = 0,     // 上次播放位置（毫秒）
    val playCount: Int = 0,             // 播放次数
    val lastPlayTime: Long = 0,         // 上次播放时间（时间戳）

    // 收藏和标签
    val isFavorite: Boolean = false,
    val tags: String? = null,           // 标签（JSON数组格式）

    // 时间戳
    val addedTime: Long,                // 添加时间（时间戳）
    val modifiedTime: Long              // 文件修改时间（时间戳）
)
