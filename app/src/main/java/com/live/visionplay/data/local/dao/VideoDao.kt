package com.live.visionplay.data.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.live.visionplay.data.local.entity.VideoEntity
import kotlinx.coroutines.flow.Flow

/**
 * 视频数据访问对象
 */
@Dao
interface VideoDao {

    /**
     * 插入视频
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(video: VideoEntity): Long

    /**
     * 批量插入视频
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(videos: List<VideoEntity>)

    /**
     * 更新视频
     */
    @Update
    suspend fun update(video: VideoEntity)

    /**
     * 删除视频
     */
    @Delete
    suspend fun delete(video: VideoEntity)

    /**
     * 根据ID删除视频
     */
    @Query("DELETE FROM videos WHERE id = :videoId")
    suspend fun deleteById(videoId: Long)

    /**
     * 根据文件路径删除视频
     */
    @Query("DELETE FROM videos WHERE filePath = :filePath")
    suspend fun deleteByPath(filePath: String)

    /**
     * 清空所有视频
     */
    @Query("DELETE FROM videos")
    suspend fun deleteAll()

    /**
     * 根据ID查询视频
     */
    @Query("SELECT * FROM videos WHERE id = :videoId")
    suspend fun getById(videoId: Long): VideoEntity?

    /**
     * 根据ID查询视频（Flow）
     */
    @Query("SELECT * FROM videos WHERE id = :videoId")
    fun getByIdFlow(videoId: Long): Flow<VideoEntity?>

    /**
     * 根据文件路径查询视频
     */
    @Query("SELECT * FROM videos WHERE filePath = :filePath")
    suspend fun getByPath(filePath: String): VideoEntity?

    /**
     * 获取所有视频（分页）
     */
    @Query("SELECT * FROM videos ORDER BY addedTime DESC")
    fun getAllPaged(): PagingSource<Int, VideoEntity>

    /**
     * 获取所有视频（Flow）
     */
    @Query("SELECT * FROM videos ORDER BY addedTime DESC")
    fun getAllFlow(): Flow<List<VideoEntity>>

    /**
     * 根据文件夹路径获取视频（分页）
     */
    @Query("SELECT * FROM videos WHERE folderPath = :folderPath ORDER BY fileName ASC")
    fun getByFolderPaged(folderPath: String): PagingSource<Int, VideoEntity>

    /**
     * 根据文件夹路径获取视频（Flow）
     */
    @Query("SELECT * FROM videos WHERE folderPath = :folderPath ORDER BY fileName ASC")
    fun getByFolderFlow(folderPath: String): Flow<List<VideoEntity>>

    /**
     * 获取收藏的视频（分页）
     */
    @Query("SELECT * FROM videos WHERE isFavorite = 1 ORDER BY addedTime DESC")
    fun getFavoritesPaged(): PagingSource<Int, VideoEntity>

    /**
     * 获取收藏的视频（Flow）
     */
    @Query("SELECT * FROM videos WHERE isFavorite = 1 ORDER BY addedTime DESC")
    fun getFavoritesFlow(): Flow<List<VideoEntity>>

    /**
     * 获取最近播放的视频（分页）
     */
    @Query("SELECT * FROM videos WHERE lastPlayTime > 0 ORDER BY lastPlayTime DESC LIMIT :limit")
    fun getRecentPlayedPaged(limit: Int = 20): PagingSource<Int, VideoEntity>

    /**
     * 获取最近播放的视频（Flow）
     */
    @Query("SELECT * FROM videos WHERE lastPlayTime > 0 ORDER BY lastPlayTime DESC LIMIT :limit")
    fun getRecentPlayedFlow(limit: Int = 20): Flow<List<VideoEntity>>

    /**
     * 搜索视频（分页）
     */
    @Query("SELECT * FROM videos WHERE fileName LIKE '%' || :query || '%' ORDER BY fileName ASC")
    fun searchPaged(query: String): PagingSource<Int, VideoEntity>

    /**
     * 搜索视频（Flow）
     */
    @Query("SELECT * FROM videos WHERE fileName LIKE '%' || :query || '%' ORDER BY fileName ASC")
    fun searchFlow(query: String): Flow<List<VideoEntity>>

    /**
     * 获取所有文件夹路径
     */
    @Query("SELECT DISTINCT folderPath FROM videos ORDER BY folderPath ASC")
    fun getAllFolders(): Flow<List<String>>

    /**
     * 获取视频总数
     */
    @Query("SELECT COUNT(*) FROM videos")
    fun getCount(): Flow<Int>

    /**
     * 获取文件夹中的视频数量
     */
    @Query("SELECT COUNT(*) FROM videos WHERE folderPath = :folderPath")
    fun getCountByFolder(folderPath: String): Flow<Int>

    /**
     * 更新播放记录
     */
    @Query("UPDATE videos SET lastPlayPosition = :position, lastPlayTime = :time, playCount = playCount + 1 WHERE id = :videoId")
    suspend fun updatePlayRecord(videoId: Long, position: Long, time: Long)

    /**
     * 更新收藏状态
     */
    @Query("UPDATE videos SET isFavorite = :isFavorite WHERE id = :videoId")
    suspend fun updateFavorite(videoId: Long, isFavorite: Boolean)

    /**
     * 检查文件路径是否存在
     */
    @Query("SELECT EXISTS(SELECT 1 FROM videos WHERE filePath = :filePath)")
    suspend fun exists(filePath: String): Boolean
}
