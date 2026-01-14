package com.live.visionplay.ui.home.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * 主页功能入口数据模型
 *
 * 表示主页面上的各个功能卡片。
 * 参考 Material Design 3 的卡片式导航设计。
 *
 * 主页面重构 - 第三阶段
 *
 * @param id 功能ID，用于标识功能类型
 * @param title 标题资源ID
 * @param description 描述资源ID
 * @param iconRes 图标资源ID
 * @param badge 徽章数字（可选，如本地视频数量）
 */
data class HomeFeature(
    val id: FeatureType,
    @StringRes val title: Int,
    @StringRes val description: Int,
    @DrawableRes val iconRes: Int,
    val badge: Int? = null
)

/**
 * 功能类型枚举
 */
enum class FeatureType {
    LOCAL_VIDEOS,    // 本地视频列表
    SINGLE_VIDEO,    // 单个视频选择
    REMOTE_VIDEO,    // 远端视频播放
    LIVE_TV          // 电视直播
}
