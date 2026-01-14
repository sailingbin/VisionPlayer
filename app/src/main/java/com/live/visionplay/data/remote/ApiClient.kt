package com.live.visionplay.data.remote

import com.live.visionplay.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * 提供Retrofit客户端实例的单例对象。
 */
object ApiClient {

    // 使用GitHub Raw内容的基础URL作为示例。
    // 您应该将其替换为您自己的服务器地址或实际的GitHub Raw地址。
    private const val BASE_URL = "https://raw.githubusercontent.com/sailingbin/VisionPlayer/main/"

    /**
     * 创建并配置Retrofit实例。
     */
    private val retrofit: Retrofit by lazy {
        // 配置OkHttpClient，并添加日志拦截器以便在Debug模式下查看网络请求
        val httpClient = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
            httpClient.addInterceptor(logging)
        }

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * 创建UpdateService的实例。
     */
    val updateService: UpdateService by lazy {
        retrofit.create(UpdateService::class.java)
    }
}
