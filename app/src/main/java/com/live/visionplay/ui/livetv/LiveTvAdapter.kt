package com.live.visionplay.ui.livetv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.live.visionplay.R
import com.live.visionplay.model.TvChannel

class LiveTvAdapter(
    private var channels: List<TvChannel>,
    private val onClick: (TvChannel) -> Unit,
    private val onLongClick: (TvChannel) -> Unit
) : RecyclerView.Adapter<LiveTvAdapter.ChannelViewHolder>() {

    fun updateData(newChannels: List<TvChannel>) {
        channels = newChannels
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_live_tv_channel, parent, false)
        return ChannelViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        val channel = channels[position]
        holder.bind(channel)
        holder.itemView.setOnClickListener { onClick(channel) }
        holder.itemView.setOnLongClickListener {
            onLongClick(channel)
            true
        }
    }

    override fun getItemCount(): Int = channels.size

    class ChannelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val logo: ImageView = itemView.findViewById(R.id.channel_logo)
        private val name: TextView = itemView.findViewById(R.id.channel_name)

        fun bind(channel: TvChannel) {
            name.text = channel.name

            // 根据频道类型选择合适的默认缩略图
            val placeholderDrawable = when {
                // CCTV频道 - 红色背景
                channel.id.startsWith("cctv", ignoreCase = true) ||
                channel.name.contains("CCTV", ignoreCase = true) -> {
                    R.drawable.ic_tv_cctv_placeholder
                }
                // 卫视频道 - 蓝色背景
                channel.name.contains("卫视", ignoreCase = true) -> {
                    R.drawable.ic_tv_satellite_placeholder
                }
                // 国际频道 - 绿色背景（判断常见国际频道关键词）
                channel.name.contains("DW", ignoreCase = true) ||
                channel.name.contains("France", ignoreCase = true) ||
                channel.name.contains("Euro", ignoreCase = true) ||
                channel.name.contains("Bloomberg", ignoreCase = true) ||
                channel.name.contains("CNN", ignoreCase = true) ||
                channel.name.contains("ABC", ignoreCase = true) ||
                channel.name.contains("CGTN", ignoreCase = true) ||
                channel.name.contains("NHK", ignoreCase = true) ||
                channel.name.contains("Arirang", ignoreCase = true) ||
                channel.name.contains("Red Bull", ignoreCase = true) ||
                channel.name.contains("NASA", ignoreCase = true) ||
                channel.name.contains("凤凰", ignoreCase = true) -> {
                    R.drawable.ic_tv_international_placeholder
                }
                // 其他频道 - 紫色背景
                else -> {
                    R.drawable.ic_tv_default_placeholder
                }
            }

            // 使用Coil加载频道logo，失败时显示对应的默认缩略图
            logo.load(channel.logoUrl) {
                crossfade(true)
                placeholder(placeholderDrawable)
                error(placeholderDrawable)
            }
        }
    }
}
