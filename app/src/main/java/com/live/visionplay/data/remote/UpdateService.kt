package com.live.visionplay.data.remote

import com.live.visionplay.data.VersionInfo
import retrofit2.http.GET

/**
 * 定义版本更新API的网络服务接口。
 */
interface UpdateService {

    /**
     * 从服务器获取最新的版本信息。
     * 这里的 "sailing/VisionPlayer/main/docs/version-info.json" 是一个示例路径，
     * 您需要将其替换为您的JSON文件在仓库中的实际路径。
     */
    @GET("docs/version-info.json")
    suspend fun getLatestVersion(): VersionInfo
}
