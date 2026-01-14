package com.live.visionplay.data

import com.live.visionplay.data.remote.ApiClient

/**
 * 负责提供版本更新相关的数据。
 */
class UpdateRepository {

    private val updateService = ApiClient.updateService

    /**
     * 从远程服务器获取最新的版本信息。
     * @return [VersionInfo] 数据对象。
     */
    suspend fun getLatestVersion(): VersionInfo {
        return updateService.getLatestVersion()
    }
}
