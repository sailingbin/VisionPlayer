package com.live.visionplay.data.local

import android.content.Context
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import android.util.Log
import com.live.visionplay.data.local.dao.VideoDao
import com.live.visionplay.data.local.entity.VideoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 视频扫描器
 *
 * 负责扫描本地视频文件并提取元数据。
 * 参考 Android 源码中的 MediaScanner 设计思路，使用 MediaStore 和 MediaMetadataRetriever。
 *
 * 功能特性：
 * 1. 扫描指定目录的视频文件
 * 2. 使用 MediaStore 快速查询系统媒体库
 * 3. 提取视频元数据（时长、分辨率、比特率等）
 * 4. 生成缩略图
 * 5. 批量插入数据库
 *
 * 第二阶段：数据层开发
 *
 * @param context 应用上下文
 * @param videoDao 视频数据访问对象
 * @param thumbnailManager 缩略图管理器
 */
class VideoScanner(
    private val context: Context,
    private val videoDao: VideoDao,
    private val thumbnailManager: ThumbnailManager
) {

    companion object {
        private const val TAG = "VideoScanner"

        // 支持的视频格式
        private val SUPPORTED_VIDEO_EXTENSIONS = setOf(
            "mp4", "mkv", "avi", "mov", "wmv", "flv",
            "webm", "m4v", "3gp", "ts", "m2ts"
        )

        // 最小视频文件大小（字节）
        private const val MIN_VIDEO_SIZE = 1024 * 1024 // 1MB
    }

    /**
     * 扫描所有视频
     *
     * 使用 MediaStore 查询系统媒体库中的所有视频
     *
     * @return 扫描到的视频数量
     */
    suspend fun scanAllVideos(): Int = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting to scan all videos from MediaStore")

            val videos = queryVideosFromMediaStore()
            Log.d(TAG, "Found ${videos.size} videos from MediaStore")

            // 批量插入数据库
            if (videos.isNotEmpty()) {
                videoDao.insertAll(videos)
            }

            videos.size
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning all videos", e)
            0
        }
    }

    /**
     * 扫描指定目录
     *
     * @param directory 要扫描的目录
     * @return 扫描到的视频数量
     */
    suspend fun scanDirectory(directory: File): Int = withContext(Dispatchers.IO) {
        try {
            if (!directory.exists() || !directory.isDirectory) {
                Log.w(TAG, "Directory does not exist or is not a directory: ${directory.absolutePath}")
                return@withContext 0
            }

            Log.d(TAG, "Scanning directory: ${directory.absolutePath}")

            val videos = mutableListOf<VideoEntity>()
            scanDirectoryRecursive(directory, videos)

            Log.d(TAG, "Found ${videos.size} videos in ${directory.absolutePath}")

            // 批量插入数据库
            if (videos.isNotEmpty()) {
                videoDao.insertAll(videos)
            }

            videos.size
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning directory: ${directory.absolutePath}", e)
            0
        }
    }

    /**
     * 递归扫描目录
     *
     * @param directory 当前目录
     * @param videos 视频列表（输出参数）
     */
    private suspend fun scanDirectoryRecursive(directory: File, videos: MutableList<VideoEntity>) {
        try {
            directory.listFiles()?.forEach { file ->
                when {
                    file.isDirectory -> {
                        // 递归扫描子目录
                        scanDirectoryRecursive(file, videos)
                    }
                    file.isFile && isVideoFile(file) -> {
                        // 处理视频文件
                        scanVideoFile(file)?.let { videos.add(it) }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning directory: ${directory.absolutePath}", e)
        }
    }

    /**
     * 使用 MediaStore 查询视频
     *
     * @return 视频实体列表
     */
    private suspend fun queryVideosFromMediaStore(): List<VideoEntity> {
        val videos = mutableListOf<VideoEntity>()

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DATE_MODIFIED
        )

        val selection = "${MediaStore.Video.Media.SIZE} >= ?"
        val selectionArgs = arrayOf(MIN_VIDEO_SIZE.toString())
        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        try {
            context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
                val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
                val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
                val addedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                val modifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)

                while (cursor.moveToNext()) {
                    try {
                        val filePath = cursor.getString(pathColumn)
                        val file = File(filePath)

                        if (!file.exists()) continue

                        // 生成缩略图
                        val thumbnailPath = thumbnailManager.generateThumbnail(filePath)

                        // 提取额外的元数据
                        val metadata = extractMetadata(filePath)

                        val video = VideoEntity(
                            fileName = cursor.getString(nameColumn),
                            filePath = filePath,
                            folderPath = file.parent ?: "",
                            fileSize = cursor.getLong(sizeColumn),
                            mimeType = cursor.getString(mimeColumn),
                            duration = cursor.getLong(durationColumn),
                            width = cursor.getInt(widthColumn),
                            height = cursor.getInt(heightColumn),
                            bitrate = metadata.bitrate,
                            frameRate = metadata.frameRate,
                            thumbnailPath = thumbnailPath,
                            addedTime = cursor.getLong(addedColumn) * 1000,
                            modifiedTime = cursor.getLong(modifiedColumn) * 1000
                        )

                        videos.add(video)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing video from MediaStore", e)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying MediaStore", e)
        }

        return videos
    }

    /**
     * 扫描单个视频文件
     *
     * @param file 视频文件
     * @return 视频实体或 null
     */
    private suspend fun scanVideoFile(file: File): VideoEntity? {
        return try {
            Log.d(TAG, "Scanning video: ${file.absolutePath}")

            // 生成缩略图
            val thumbnailPath = thumbnailManager.generateThumbnail(file.absolutePath)

            // 提取元数据
            val metadata = extractMetadata(file.absolutePath)

            VideoEntity(
                fileName = file.name,
                filePath = file.absolutePath,
                folderPath = file.parent ?: "",
                fileSize = file.length(),
                mimeType = getMimeType(file),
                duration = metadata.duration,
                width = metadata.width,
                height = metadata.height,
                bitrate = metadata.bitrate,
                frameRate = metadata.frameRate,
                thumbnailPath = thumbnailPath,
                addedTime = System.currentTimeMillis(),
                modifiedTime = file.lastModified()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning video file: ${file.absolutePath}", e)
            null
        }
    }

    /**
     * 提取视频元数据
     *
     * @param videoPath 视频文件路径
     * @return 视频元数据
     */
    private fun extractMetadata(videoPath: String): VideoMetadata {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(videoPath)

            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0L

            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                ?.toIntOrNull() ?: 0

            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                ?.toIntOrNull() ?: 0

            val bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
                ?.toLongOrNull() ?: 0L

            val frameRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)
                ?.toFloatOrNull() ?: 0f

            VideoMetadata(
                duration = duration,
                width = width,
                height = height,
                bitrate = bitrate,
                frameRate = frameRate
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting metadata from $videoPath", e)
            VideoMetadata()
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error releasing MediaMetadataRetriever", e)
            }
        }
    }

    /**
     * 判断是否为视频文件
     *
     * @param file 文件对象
     * @return 是否为视频文件
     */
    private fun isVideoFile(file: File): Boolean {
        if (file.length() < MIN_VIDEO_SIZE) return false

        val extension = file.extension.lowercase()
        return extension in SUPPORTED_VIDEO_EXTENSIONS
    }

    /**
     * 获取文件的 MIME 类型
     *
     * @param file 文件对象
     * @return MIME 类型
     */
    private fun getMimeType(file: File): String {
        return when (file.extension.lowercase()) {
            "mp4" -> "video/mp4"
            "mkv" -> "video/x-matroska"
            "avi" -> "video/x-msvideo"
            "mov" -> "video/quicktime"
            "wmv" -> "video/x-ms-wmv"
            "flv" -> "video/x-flv"
            "webm" -> "video/webm"
            "m4v" -> "video/x-m4v"
            "3gp" -> "video/3gpp"
            "ts" -> "video/mp2t"
            "m2ts" -> "video/mp2t"
            else -> "video/*"
        }
    }

    /**
     * 视频元数据数据类
     */
    private data class VideoMetadata(
        val duration: Long = 0L,
        val width: Int = 0,
        val height: Int = 0,
        val bitrate: Long = 0L,
        val frameRate: Float = 0f
    )
}
