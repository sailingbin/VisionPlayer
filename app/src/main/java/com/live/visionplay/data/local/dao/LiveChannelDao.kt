package com.live.visionplay.data.local.dao

import androidx.room.*
import com.live.visionplay.data.local.entity.LiveChannelEntity
import kotlinx.coroutines.flow.Flow

/**
 * 直播频道数据访问对象
 */
@Dao
interface LiveChannelDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(channel: LiveChannelEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(channels: List<LiveChannelEntity>)

    @Update
    suspend fun update(channel: LiveChannelEntity)

    @Delete
    suspend fun delete(channel: LiveChannelEntity)

    @Query("DELETE FROM live_channels WHERE id = :channelId")
    suspend fun deleteById(channelId: Long)

    @Query("DELETE FROM live_channels")
    suspend fun deleteAll()

    /**
     * 根据ID查询频道
     */
    @Query("SELECT * FROM live_channels WHERE id = :channelId")
    suspend fun getById(channelId: Long): LiveChannelEntity?

    @Query("SELECT * FROM live_channels WHERE id = :channelId")
    fun getByIdFlow(channelId: Long): Flow<LiveChannelEntity?>

    /**
     * 获取所有频道
     */
    @Query("SELECT * FROM live_channels ORDER BY category ASC, sortOrder ASC")
    fun getAllFlow(): Flow<List<LiveChannelEntity>>

    /**
     * 根据分类获取频道
     */
    @Query("SELECT * FROM live_channels WHERE category = :category ORDER BY sortOrder ASC")
    fun getByCategoryFlow(category: String): Flow<List<LiveChannelEntity>>

    /**
     * 获取收藏的频道
     */
    @Query("SELECT * FROM live_channels WHERE isFavorite = 1 ORDER BY sortOrder ASC")
    fun getFavoritesFlow(): Flow<List<LiveChannelEntity>>

    /**
     * 获取所有分类
     */
    @Query("SELECT DISTINCT category FROM live_channels ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    /**
     * 搜索频道
     */
    @Query("SELECT * FROM live_channels WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchFlow(query: String): Flow<List<LiveChannelEntity>>

    /**
     * 更新频道可用状态
     */
    @Query("UPDATE live_channels SET isAvailable = :isAvailable, lastCheckTime = :checkTime, failCount = :failCount WHERE id = :channelId")
    suspend fun updateAvailability(channelId: Long, isAvailable: Boolean, checkTime: Long, failCount: Int)

    /**
     * 更新收藏状态
     */
    @Query("UPDATE live_channels SET isFavorite = :isFavorite WHERE id = :channelId")
    suspend fun updateFavorite(channelId: Long, isFavorite: Boolean)

    /**
     * 更新排序顺序
     */
    @Query("UPDATE live_channels SET sortOrder = :sortOrder WHERE id = :channelId")
    suspend fun updateSortOrder(channelId: Long, sortOrder: Int)

    /**
     * 获取频道总数
     */
    @Query("SELECT COUNT(*) FROM live_channels")
    fun getCount(): Flow<Int>

    /**
     * 获取分类中的频道数量
     */
    @Query("SELECT COUNT(*) FROM live_channels WHERE category = :category")
    fun getCountByCategory(category: String): Flow<Int>
}
