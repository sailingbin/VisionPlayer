package com.live.visionplay.ui.remote

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.live.visionplay.R
import com.live.visionplay.databinding.ActivityRemoteVideoBinding

class RemoteVideoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRemoteVideoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRemoteVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.playButton.setOnClickListener {
            playRemoteVideo()
        }
    }

    private fun playRemoteVideo() {
        val url = binding.urlEditText.text.toString().trim()

        if (isValidUrl(url)) {
            // 检测localhost使用，提醒用户使用局域网IP
            if (url.contains("://localhost", ignoreCase = true) || url.contains("://127.0.0.1")) {
                Toast.makeText(this, getString(R.string.localhost_warning), Toast.LENGTH_LONG).show()
                return
            }

            // 判断是否为直播流协议（RTSP/RTMP），使用直播UI
            if (isLiveStreamProtocol(url)) {
                val intent = Intent(this, com.live.visionplay.player.ui.LiveStreamPlayerActivity::class.java).apply {
                    data = url.toUri()
                    putExtra(com.live.visionplay.player.ui.LiveStreamPlayerActivity.EXTRA_STREAM_NAME,
                        extractStreamName(url))
                }
                startActivity(intent)
            } else {
                // HTTP/HTTPS点播视频使用普通播放器
                val intent = Intent(this, com.live.visionplay.player.ui.VideoPlayerActivity::class.java).apply {
                    data = url.toUri()
                }
                startActivity(intent)
            }
        } else {
            Toast.makeText(this, getString(R.string.enter_network_stream_url), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 验证URL是否为有效的流媒体地址
     * 支持的协议：http、https、rtmp、rtmps、rtmpt、rtmpe、rtsp、rtsps
     */
    private fun isValidUrl(url: String): Boolean {
        if (url.isBlank()) {
            return false
        }

        // 检查是否为支持的流媒体协议
        val supportedProtocols = listOf(
            "http://",
            "https://",
            "rtmp://",     // Real-Time Messaging Protocol
            "rtmps://",    // RTMP over TLS/SSL
            "rtmpt://",    // RTMP tunneled over HTTP
            "rtmpe://",    // Encrypted RTMP
            "rtsp://",     // Real-Time Streaming Protocol
            "rtsps://"     // RTSP over TLS/SSL
        )

        return supportedProtocols.any { protocol ->
            url.startsWith(protocol, ignoreCase = true)
        }
    }

    /**
     * 判断是否为直播流协议
     * RTSP/RTMP及其变种都是直播协议，应使用LiveStreamPlayerActivity
     * HLS (.m3u8) 也是直播协议
     */
    private fun isLiveStreamProtocol(url: String): Boolean {
        val lowerUrl = url.lowercase()
        val liveProtocols = listOf(
            "rtsp://",
            "rtsps://",
            "rtmp://",
            "rtmps://",
            "rtmpt://",
            "rtmpe://"
        )

        // 检查协议
        if (liveProtocols.any { lowerUrl.startsWith(it) }) {
            return true
        }

        // 检查是否为HLS流（.m3u8）
        if (lowerUrl.endsWith(".m3u8") || lowerUrl.contains(".m3u8?")) {
            return true
        }

        return false
    }

    /**
     * 从URL提取流名称
     * 优先使用路径的最后一部分，如果为空则使用主机名
     */
    private fun extractStreamName(url: String): String {
        return try {
            val uri = url.toUri()
            // 尝试获取路径的最后一段作为流名称
            val pathSegments = uri.pathSegments
            if (!pathSegments.isNullOrEmpty()) {
                pathSegments.last()
            } else {
                // 如果没有路径，使用主机名
                uri.host ?: getString(R.string.live_stream)
            }
        } catch (_: Exception) {
            getString(R.string.live_stream)
        }
    }
}
