package com.live.visionplay

import android.app.Application
import android.util.Log
import androidx.work.Configuration

/**
 * VisionPlayer 应用程序类
 *
 * 负责应用级别的初始化工作，包括：
 * 1. 全局异常处理器设置
 * 2. WorkManager 后台任务初始化
 * 3. 应用级别的配置和资源初始化
 *
 * 设计思路参考 Android 源码中 Application 的标准实践：
 * - 在 onCreate() 中进行轻量级初始化
 * - 避免阻塞主线程的耗时操作
 * - 使用 lazy 延迟初始化非关键组件
 *
 * @since 1.0
 */
class VisionPlayerApplication : Application(), Configuration.Provider {

    companion object {
        private const val TAG = "VisionPlayerApp"

        /**
         * 应用实例（单例模式）
         * 注意：虽然提供全局访问，但应避免滥用，优先使用依赖注入
         */
        lateinit var instance: VisionPlayerApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 设置全局异常处理器
        setupGlobalExceptionHandler()

        // 初始化 WorkManager（自动从 Configuration.Provider 获取配置）
        initializeWorkManager()

        Log.i(TAG, "VisionPlayer application initialized")
    }

    /**
     * 提供 WorkManager 配置
     *
     * 实现 Configuration.Provider 接口，自定义 WorkManager 配置。
     * 参考 WorkManager 官方文档的按需初始化模式。
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) Log.DEBUG else Log.ERROR)
            .build()

    /**
     * 初始化 WorkManager 后台任务
     *
     * 在应用启动时调度周期性视频扫描任务。
     * 使用 WorkManager 确保即使应用关闭，扫描任务也能在后台执行。
     */
    private fun initializeWorkManager() {
        try {
            // 注意：周期性扫描默认不启用，避免频繁后台扫描消耗资源
            // 可以在设置界面提供选项让用户启用
            // VideoScanScheduler.schedulePeriodicScan(this)

            Log.d(TAG, "WorkManager initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize WorkManager", e)
        }
    }

    /**
     * 设置全局异常处理器
     *
     * 捕获未被处理的异常，在应用崩溃前记录详细信息。
     * 参考 Android 源码中 Thread.UncaughtExceptionHandler 的设计。
     */
    private fun setupGlobalExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                Log.e(TAG, "Uncaught exception in thread: ${thread.name}", throwable)
            } catch (e: Exception) {
                Log.e(TAG, "Error in exception handler", e)
            } finally {
                // 调用原始的异常处理器，确保应用正常崩溃流程
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }

        Log.d(TAG, "Global exception handler setup completed")
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Log.w(TAG, "Low memory warning")
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Log.d(TAG, "onTrimMemory: level=$level")
    }
}
