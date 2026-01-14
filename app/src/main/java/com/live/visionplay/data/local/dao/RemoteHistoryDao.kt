package com.live.visionplay.data.local.dao

import androidx.room.*
import com.live.visionplay.data.local.entity.RemoteHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * 远端视频历史记录数据访问对象
 */
@Dao
interface RemoteHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: RemoteHistoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(histories: List<RemoteHistoryEntity>)

    @Update
    suspend fun update(history: RemoteHistoryEntity)

    @Delete
    suspend fun delete(history: RemoteHistoryEntity)

    @Query("DELETE FROM remote_history WHERE id = :historyId")
    suspend fun deleteById(historyId: Long)

    @Query("DELETE FROM remote_history WHERE url = :url")
    suspend fun deleteByUrl(url: String)

    @Query("DELETE FROM remote_history")
    suspend fun deleteAll()

    /**
     * 根据ID查询
     */
    @Query("SELECT * FROM remote_history WHERE id = :historyId")
    suspend fun getById(historyId: Long): RemoteHistoryEntity?

    @Query("SELECT * FROM remote_history WHERE id = :historyId")
    fun getByIdFlow(historyId: Long): Flow<RemoteHistoryEntity?>

    /**
     * 根据URL查询
     */
    @Query("SELECT * FROM remote_history WHERE url = :url")
    suspend fun getByUrl(url: String): RemoteHistoryEntity?

    /**
     * 获取所有历史记录（按最后访问时间排序）
     */
    @Query("SELECT * FROM remote_history ORDER BY lastAccessTime DESC")
    fun getAllFlow(): Flow<List<RemoteHistoryEntity>>

    /**
     * 获取最近的历史记录
     */
    @Query("SELECT * FROM remote_history ORDER BY lastAccessTime DESC LIMIT :limit")
    fun getRecentFlow(limit: Int = 20): Flow<List<RemoteHistoryEntity>>

    /**
     * 获取收藏的记录
     */
    @Query("SELECT * FROM remote_history WHERE isFavorite = 1 ORDER BY lastAccessTime DESC")
    fun getFavoritesFlow(): Flow<List<RemoteHistoryEntity>>

    /**
     * 搜索历史记录
     */
    @Query("SELECT * FROM remote_history WHERE url LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%' ORDER BY lastAccessTime DESC")
    fun searchFlow(query: String): Flow<List<RemoteHistoryEntity>>

    /**
     * 更新访问记录
     */
    @Query("UPDATE remote_history SET accessCount = accessCount + 1, lastAccessTime = :time WHERE id = :historyId")
    suspend fun updateAccessRecord(historyId: Long, time: Long)

    /**
     * 更新播放位置
     */
    @Query("UPDATE remote_history SET lastPlayPosition = :position WHERE id = :historyId")
    suspend fun updatePlayPosition(historyId: Long, position: Long)

    /**
     * 更新收藏状态
     */
    @Query("UPDATE remote_history SET isFavorite = :isFavorite WHERE id = :historyId")
    suspend fun updateFavorite(historyId: Long, isFavorite: Boolean)

    /**
     * 检查URL是否存在
     */
    @Query("SELECT EXISTS(SELECT 1 FROM remote_history WHERE url = :url)")
    suspend fun exists(url: String): Boolean

    /**
     * 获取总数
     */
    @Query("SELECT COUNT(*) FROM remote_history")
    fun getCount(): Flow<Int>
}
