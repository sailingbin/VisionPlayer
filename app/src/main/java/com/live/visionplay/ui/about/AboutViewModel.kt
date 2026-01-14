package com.live.visionplay.ui.about

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.live.visionplay.data.UpdateRepository
import com.live.visionplay.data.VersionInfo

/**
 * “关于”页面的ViewModel，负责处理版本更新检查的逻辑。
 */
class AboutViewModel(private val updateRepository: UpdateRepository) : ViewModel() {

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState

    /**
     * 检查应用更新。
     * @param currentVersionCode 当前应用的版本号。
     */
    fun checkForUpdates(currentVersionCode: Int) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Loading
            try {
                val latestVersion = updateRepository.getLatestVersion()
                if (latestVersion.versionCode > currentVersionCode) {
                    _updateState.value = UpdateState.UpdateAvailable(latestVersion)
                } else {
                    _updateState.value = UpdateState.NoUpdate
                }
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }
}

/**
 * 定义版本更新检查过程中的各种状态。
 */
sealed class UpdateState {
    /** 空闲状态 */
    object Idle : UpdateState()
    /** 检查中 */
    object Loading : UpdateState()
    /** 已是最新版本 */
    object NoUpdate : UpdateState()
    /** 有可用更新 */
    data class UpdateAvailable(val versionInfo: VersionInfo) : UpdateState()
    /** 发生错误 */
    data class Error(val message: String) : UpdateState()
}
