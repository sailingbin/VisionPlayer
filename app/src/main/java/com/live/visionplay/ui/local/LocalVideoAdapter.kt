package com.live.visionplay.ui.local

import android.annotation.SuppressLint
import android.os.Build
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.live.visionplay.R
import com.live.visionplay.model.Video
import java.io.File
import java.util.concurrent.TimeUnit

class LocalVideoAdapter(
    private val videos: List<Video>,
    private val onClick: (Video) -> Unit
) : RecyclerView.Adapter<LocalVideoAdapter.VideoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_local_video, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = videos[position]
        holder.bind(video)
        holder.itemView.setOnClickListener { onClick(video) }
    }

    override fun getItemCount(): Int = videos.size

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val thumbnail: ImageView = itemView.findViewById(R.id.video_thumbnail)
        private val name: TextView = itemView.findViewById(R.id.video_name)
        private val path: TextView = itemView.findViewById(R.id.video_path)
        private val duration: TextView = itemView.findViewById(R.id.video_duration)
        private val size: TextView = itemView.findViewById(R.id.video_size)

        fun bind(video: Video) {
            name.text = video.name
            path.text = File(video.path).parent
            duration.text = formatDuration(video.duration.toLong())
            size.text = formatSize(video.size)

            try {
                val thumb = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    itemView.context.contentResolver.loadThumbnail(
                        video.uri, Size(120, 70), null)
                } else {
                    // For older versions, you might need a different approach or a library
                    // For simplicity, we'll just set a placeholder
                    null
                }
                if (thumb != null) {
                    thumbnail.setImageBitmap(thumb)
                } else {
                    thumbnail.setImageResource(R.color.purple_200)
                }
            } catch (_: Exception) {
                thumbnail.setImageResource(R.color.purple_200)
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun formatDuration(millis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) -
                TimeUnit.MINUTES.toSeconds(minutes)
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }
}
