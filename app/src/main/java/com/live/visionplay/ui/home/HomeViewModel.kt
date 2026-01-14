package com.live.visionplay.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.live.visionplay.R
import com.live.visionplay.ui.home.model.FeatureType
import com.live.visionplay.ui.home.model.HomeFeature
import kotlinx.coroutines.launch

/**
 * 主页ViewModel
 *
 * 负责管理主页面的业务逻辑和数据状态。
 * 参考 Android 架构组件指南中的 ViewModel 模式。
 *
 * 主页面重构 - 第三阶段
 *
 * 职责：
 * 1. 提供功能入口列表数据
 * 2. 管理视频数量等统计信息
 * 3. 处理用户交互事件
 */
class HomeViewModel : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
    }

    // 功能入口列表
    private val _features = MutableLiveData<List<HomeFeature>>()
    val features: LiveData<List<HomeFeature>> = _features

    // 本地视频数量（用于显示徽章）
    private val _localVideoCount = MutableLiveData<Int>()
    val localVideoCount: LiveData<Int> = _localVideoCount

    init {
        initFeatures()
        loadVideoCount()
    }

    /**
     * 初始化功能入口列表
     */
    private fun initFeatures() {
        _features.value = listOf(
            HomeFeature(
                id = FeatureType.LOCAL_VIDEOS,
                title = R.string.local_videos,
                description = R.string.local_videos_description,
                iconRes = R.drawable.ic_video_library_24
            ),
            HomeFeature(
                id = FeatureType.SINGLE_VIDEO,
                title = R.string.single_video,
                description = R.string.single_video_description,
                iconRes = R.drawable.ic_movie_24
            ),
            HomeFeature(
                id = FeatureType.REMOTE_VIDEO,
                title = R.string.remote_video,
                description = R.string.remote_video_description,
                iconRes = R.drawable.ic_language_24
            ),
            HomeFeature(
                id = FeatureType.LIVE_TV,
                title = R.string.live_tv,
                description = R.string.live_tv_description,
                iconRes = R.drawable.ic_tv_24
            )
        )
    }

    /**
     * 加载本地视频数量
     *
     * TODO: 接入 VideoRepository 获取真实数据
     */
    private fun loadVideoCount() {
        viewModelScope.launch {
            try {
                // 暂时返回0，后续接入数据层
                _localVideoCount.value = 0
            } catch (_: Exception) {
                _localVideoCount.value = 0
            }
        }
    }

    /**
     * 刷新视频数量
     */
    fun refreshVideoCount() {
        loadVideoCount()
    }
}
