package com.live.visionplay.data.local

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.util.Log
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import androidx.core.graphics.scale

/**
 * 缩略图管理器
 *
 * 负责视频缩略图的生成、存储和管理。
 * 参考 Android 源码中的 MediaMetadataRetriever 使用最佳实践。
 *
 * 功能特性：
 * 1. 从视频文件提取缩略图帧
 * 2. 缩略图压缩和存储
 * 3. 缓存管理
 * 4. 支持本地文件和 Content URI
 *
 * 第二阶段：数据层开发
 *
 * @param context 应用上下文
 */
class ThumbnailManager(private val context: Context) {

    companion object {
        private const val TAG = "ThumbnailManager"

        // 缩略图目录名
        private const val THUMBNAIL_DIR = "thumbnails"

        // 缩略图质量配置
        private const val THUMBNAIL_WIDTH = 320
        private const val THUMBNAIL_HEIGHT = 180
        private const val JPEG_QUALITY = 85

        // 提取缩略图的时间位置（微秒）
        private const val THUMBNAIL_TIME_US = 1_000_000L // 1秒处
    }

    /**
     * 获取缩略图目录
     */
    private val thumbnailDir: File by lazy {
        File(context.cacheDir, THUMBNAIL_DIR).apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }

    /**
     * 为视频生成缩略图
     *
     * @param videoPath 视频文件路径
     * @return 缩略图文件路径，失败返回 null
     */
    suspend fun generateThumbnail(videoPath: String): String? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Generating thumbnail for: $videoPath")

            // 检查缩略图是否已存在
            val thumbnailFile = getThumbnailFile(videoPath)
            if (thumbnailFile.exists()) {
                Log.d(TAG, "Thumbnail already exists: ${thumbnailFile.absolutePath}")
                return@withContext thumbnailFile.absolutePath
            }

            // 提取视频帧
            val bitmap = extractFrameFromVideo(videoPath)
            if (bitmap == null) {
                Log.e(TAG, "Failed to extract frame from video: $videoPath")
                return@withContext null
            }

            // 缩放和压缩
            val scaledBitmap = scaleBitmap(bitmap, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT)
            bitmap.recycle()

            // 保存到文件
            val success = saveBitmapToFile(scaledBitmap, thumbnailFile)
            scaledBitmap.recycle()

            if (success) {
                Log.d(TAG, "Thumbnail saved: ${thumbnailFile.absolutePath}")
                thumbnailFile.absolutePath
            } else {
                Log.e(TAG, "Failed to save thumbnail: ${thumbnailFile.absolutePath}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating thumbnail for $videoPath", e)
            null
        }
    }

    /**
     * 从视频中提取一帧作为缩略图
     *
     * @param videoPath 视频文件路径
     * @return Bitmap 或 null
     */
    private fun extractFrameFromVideo(videoPath: String): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            // 设置数据源
            when {
                videoPath.startsWith("content://") -> {
                    retriever.setDataSource(context, videoPath.toUri())
                }
                else -> {
                    retriever.setDataSource(videoPath)
                }
            }

            // 提取第1秒处的帧
            retriever.getFrameAtTime(
                THUMBNAIL_TIME_US,
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting frame from $videoPath", e)
            null
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error releasing MediaMetadataRetriever", e)
            }
        }
    }

    /**
     * 缩放 Bitmap
     *
     * @param source 原始 Bitmap
     * @param targetWidth 目标宽度
     * @param targetHeight 目标高度
     * @return 缩放后的 Bitmap
     */
    private fun scaleBitmap(source: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val sourceWidth = source.width
        val sourceHeight = source.height

        // 计算缩放比例，保持宽高比
        val widthRatio = targetWidth.toFloat() / sourceWidth
        val heightRatio = targetHeight.toFloat() / sourceHeight
        val scaleFactor = minOf(widthRatio, heightRatio)

        val scaledWidth = (sourceWidth * scaleFactor).toInt()
        val scaledHeight = (sourceHeight * scaleFactor).toInt()

        return source.scale(scaledWidth, scaledHeight)
    }

    /**
     * 保存 Bitmap 到文件
     *
     * @param bitmap 要保存的 Bitmap
     * @param file 目标文件
     * @return 是否成功
     */
    private fun saveBitmapToFile(bitmap: Bitmap, file: File): Boolean {
        return try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving bitmap to file: ${file.absolutePath}", e)
            false
        }
    }

    /**
     * 获取缩略图文件对象
     *
     * 根据视频路径生成唯一的缩略图文件名（使用 MD5 哈希）
     *
     * @param videoPath 视频文件路径
     * @return 缩略图文件对象
     */
    fun getThumbnailFile(videoPath: String): File {
        val hash = md5(videoPath)
        return File(thumbnailDir, "$hash.jpg")
    }

    /**
     * 删除缩略图
     *
     * @param videoPath 视频文件路径
     * @return 是否成功删除
     */
    fun deleteThumbnail(videoPath: String): Boolean {
        return try {
            val file = getThumbnailFile(videoPath)
            if (file.exists()) {
                file.delete()
            } else {
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting thumbnail for $videoPath", e)
            false
        }
    }

    /**
     * 清理所有缩略图缓存
     *
     * @return 删除的文件数量
     */
    fun clearAllThumbnails(): Int {
        return try {
            thumbnailDir.listFiles()?.let { files ->
                files.count { it.delete() }
            } ?: 0
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing thumbnails", e)
            0
        }
    }

    /**
     * 获取缩略图缓存大小
     *
     * @return 缓存大小（字节）
     */
    fun getCacheSize(): Long {
        return try {
            thumbnailDir.listFiles()?.sumOf { it.length() } ?: 0L
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating cache size", e)
            0L
        }
    }

    /**
     * 计算字符串的 MD5 哈希值
     *
     * @param input 输入字符串
     * @return MD5 哈希值（十六进制字符串）
     */
    private fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
