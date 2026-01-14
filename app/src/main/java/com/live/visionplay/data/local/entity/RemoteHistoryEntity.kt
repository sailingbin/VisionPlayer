package com.live.visionplay.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * 远端视频历史记录实体
 *
 * 存储用户输入的远端视频URL历史记录
 */
@Entity(
    tableName = "remote_history",
    indices = [
        Index(value = ["url"], unique = true),
        Index(value = ["lastAccessTime"])
    ]
)
data class RemoteHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // URL信息
    val url: String,               // 视频URL
    val title: String? = null,     // 视频标题（用户自定义或从元数据获取）
    val thumbnailUrl: String? = null,  // 缩略图URL

    // 播放信息
    val lastPlayPosition: Long = 0,    // 上次播放位置（毫秒）
    val duration: Long = 0,            // 总时长（毫秒，如果已知）

    // 统计信息
    val accessCount: Int = 0,          // 访问次数
    val lastAccessTime: Long,          // 最后访问时间（时间戳）

    // 收藏
    val isFavorite: Boolean = false,

    // 时间戳
    val addedTime: Long                // 添加时间（时间戳）
)
