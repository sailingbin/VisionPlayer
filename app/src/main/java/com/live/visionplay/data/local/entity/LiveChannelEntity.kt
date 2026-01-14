package com.live.visionplay.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * 电视直播频道实体
 *
 * 存储直播频道信息（类似电视家）
 */
@Entity(
    tableName = "live_channels",
    indices = [
        Index(value = ["category"]),
        Index(value = ["sortOrder"])
    ]
)
data class LiveChannelEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 频道信息
    val name: String,              // 频道名称
    val category: String,          // 分类（央视、卫视、地方台等）
    val logo: String? = null,      // 频道logo URL
    val number: String? = null,    // 频道号码

    // 直播源
    val streamUrl: String,         // 主直播源URL
    val backupUrls: String? = null, // 备用源（JSON数组格式）
    val streamType: String = "http", // 流类型：http, rtmp, rtsp等

    // 状态
    val isAvailable: Boolean = true,    // 是否可用
    val lastCheckTime: Long = 0,        // 上次检查时间
    val failCount: Int = 0,             // 连续失败次数

    // 用户设置
    val isFavorite: Boolean = false,    // 是否收藏
    val sortOrder: Int = 0,             // 排序顺序

    // 时间戳
    val addedTime: Long,                // 添加时间
    val updatedTime: Long               // 更新时间
)
