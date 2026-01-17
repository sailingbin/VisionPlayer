package com.live.visionplay.data

import android.content.Context
import android.content.SharedPreferences
import com.live.visionplay.model.TvChannel
import org.json.JSONArray
import org.json.JSONObject

/**
 * 电视直播频道仓库
 * 支持本地存储和管理用户自定义频道
 */
object TvChannelRepository {
    private const val PREF_NAME = "tv_channels"
    private const val KEY_CHANNELS = "saved_channels"
    private var preferences: SharedPreferences? = null

    fun init(context: Context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun getChannels(): List<TvChannel> {
        val prefs = preferences ?: return getDefaultChannels()
        val jsonString = prefs.getString(KEY_CHANNELS, null)
        return if (jsonString != null) {
            try {
                parseChannels(jsonString)
            } catch (e: Exception) {
                e.printStackTrace()
                getDefaultChannels()
            }
        } else {
            val defaults = getDefaultChannels()
            saveChannels(defaults)
            defaults
        }
    }

    fun addChannel(channel: TvChannel) {
        val currentList = getChannels().toMutableList()
        currentList.add(channel)
        saveChannels(currentList)
    }

    fun removeChannel(channelId: String) {
        val currentList = getChannels().toMutableList()
        val iterator = currentList.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().id == channelId) {
                iterator.remove()
                break
            }
        }
        saveChannels(currentList)
    }

    fun updateChannel(channel: TvChannel) {
        val currentList = getChannels().toMutableList()
        val index = currentList.indexOfFirst { it.id == channel.id }
        if (index != -1) {
            currentList[index] = channel
            saveChannels(currentList)
        }
    }

    private fun saveChannels(channels: List<TvChannel>) {
        val prefs = preferences ?: return
        try {
            val jsonArray = JSONArray()
            channels.forEach { channel ->
                val jsonObject = JSONObject()
                jsonObject.put("id", channel.id)
                jsonObject.put("name", channel.name)
                jsonObject.put("streamUrl", channel.streamUrl)
                jsonObject.put("logoUrl", channel.logoUrl)
                jsonArray.put(jsonObject)
            }
            prefs.edit().putString(KEY_CHANNELS, jsonArray.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun parseChannels(jsonString: String): List<TvChannel> {
        val list = mutableListOf<TvChannel>()
        val jsonArray = JSONArray(jsonString)
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            list.add(
                TvChannel(
                    id = obj.optString("id"),
                    name = obj.optString("name"),
                    streamUrl = obj.optString("streamUrl"),
                    logoUrl = if (obj.has("logoUrl")) obj.optString("logoUrl") else null
                )
            )
        }
        return list
    }

    private fun getDefaultChannels(): List<TvChannel> {
        return emptyList()
    }


}
