package com.live.visionplay.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.live.visionplay.data.local.dao.LiveChannelDao
import com.live.visionplay.data.local.dao.RemoteHistoryDao
import com.live.visionplay.data.local.dao.VideoDao
import com.live.visionplay.data.local.entity.LiveChannelEntity
import com.live.visionplay.data.local.entity.RemoteHistoryEntity
import com.live.visionplay.data.local.entity.VideoEntity

/**
 * VisionPlayer 数据库
 *
 * Room 数据库实例，包含所有的表和DAO
 */
@Database(
    entities = [
        VideoEntity::class,
        LiveChannelEntity::class,
        RemoteHistoryEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class VisionPlayerDatabase : RoomDatabase() {

    abstract fun videoDao(): VideoDao
    abstract fun liveChannelDao(): LiveChannelDao
    abstract fun remoteHistoryDao(): RemoteHistoryDao

    companion object {
        private const val DATABASE_NAME = "visionplayer.db"

        @Volatile
        private var INSTANCE: VisionPlayerDatabase? = null

        fun getInstance(context: Context): VisionPlayerDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): VisionPlayerDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                VisionPlayerDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration()  // 开发阶段允许破坏性迁移
                .build()
        }
    }
}
