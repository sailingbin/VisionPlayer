package com.live.visionplay.ui.local

import android.Manifest
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.live.visionplay.R
import com.live.visionplay.databinding.ActivityLocalVideosBinding
import com.live.visionplay.model.Video
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LocalVideosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLocalVideosBinding
    private lateinit var videoAdapter: LocalVideoAdapter

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                loadVideos()
            } else {
                Toast.makeText(this, R.string.error_permission_denied, Toast.LENGTH_SHORT).show()
                val emptyStateView = binding.root.findViewById<View>(R.id.empty_state)
                if (emptyStateView != null) {
                    val emptyBinding = com.live.visionplay.databinding.LayoutEmptyStateBinding.bind(emptyStateView)
                    emptyBinding.emptyStateRoot.visibility = View.VISIBLE
                    emptyBinding.emptyIcon.setImageResource(R.drawable.ic_video_library_24)
                    emptyBinding.emptyTitle.setText(R.string.local_videos)
                    emptyBinding.emptyDescription.setText(R.string.error_permission_denied)
                    emptyBinding.emptyActionButton.apply {
                        visibility = View.VISIBLE
                        setText(R.string.settings)
                        setOnClickListener {
                            // 跳转到应用设置页面
                            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = android.net.Uri.fromParts("package", packageName, null)
                            }
                            startActivity(intent)
                        }
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocalVideosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        setupRecyclerView()
        checkPermissionAndLoadVideos()
    }

    private fun setupRecyclerView() {
        videoAdapter = LocalVideoAdapter(emptyList()) { video ->
            val intent = Intent(this, com.live.visionplay.player.ui.VideoPlayerActivity::class.java).apply {
                data = video.uri
            }
            startActivity(intent)
        }
        binding.videosRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@LocalVideosActivity)
            adapter = videoAdapter
        }
    }

    private fun checkPermissionAndLoadVideos() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_VIDEO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                loadVideos()
            }
            shouldShowRequestPermissionRationale(permission) -> {
                // Show a rationale UI to the user if needed
                Toast.makeText(this, R.string.permission_rationale, Toast.LENGTH_LONG).show()
                requestPermissionLauncher.launch(permission)
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    private fun loadVideos() {
        lifecycleScope.launch {
            val videos = queryVideos()
            if (videos.isNotEmpty()) {
                videoAdapter = LocalVideoAdapter(videos) { video ->
                    val intent = Intent(this@LocalVideosActivity, com.live.visionplay.player.ui.VideoPlayerActivity::class.java).apply {
                        data = video.uri
                    }
                    startActivity(intent)
                }
                binding.videosRecyclerView.adapter = videoAdapter
                binding.root.findViewById<View>(R.id.empty_state)?.visibility = View.GONE
                binding.videosRecyclerView.visibility = View.VISIBLE
            } else {
                val emptyStateView = binding.root.findViewById<View>(R.id.empty_state)
                if (emptyStateView != null) {
                    val emptyBinding = com.live.visionplay.databinding.LayoutEmptyStateBinding.bind(emptyStateView)
                    emptyBinding.emptyStateRoot.visibility = View.VISIBLE
                    emptyBinding.emptyIcon.setImageResource(R.drawable.ic_video_library_24)
                    emptyBinding.emptyTitle.setText(R.string.local_videos)
                    emptyBinding.emptyDescription.setText(R.string.empty_local_videos_desc)
                    emptyBinding.emptyActionButton.visibility = View.GONE
                }
                binding.videosRecyclerView.visibility = View.GONE
            }
        }
    }

    private suspend fun queryVideos(): List<Video> = withContext(Dispatchers.IO) {
        val videoList = mutableListOf<Video>()
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATA
        )
        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        application.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val duration = cursor.getInt(durationColumn)
                val size = cursor.getLong(sizeColumn)
                val path = cursor.getString(pathColumn)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                videoList.add(Video(contentUri, name, duration, size, path))
            }
        }
        return@withContext videoList
    }
}
