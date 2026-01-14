package com.live.visionplay.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.live.visionplay.R
import com.live.visionplay.ui.home.adapter.HomeFeatureAdapter
import com.live.visionplay.ui.home.model.FeatureType
import com.live.visionplay.ui.home.model.HomeFeature

/**
 * 主页Activity - 重构版
 *
 * 采用 Material Design 3 设计，提供4个主要功能入口：
 * 1. 本地视频 - 浏览设备上的所有视频文件
 * 2. 单个视频 - 选择单个文件播放
 * 3. 远端播放 - 输入URL播放网络视频
 * 4. 电视直播 - 观看电视频道
 *
 * 参考 Android 架构组件指南，使用 MVVM 架构。
 */
class HomeActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "HomeActivity"
    }

    // ViewModel
    private val viewModel: HomeViewModel by viewModels()

    // Views
    private lateinit var toolbar: MaterialToolbar
    private lateinit var recyclerViewFeatures: RecyclerView

    // Adapter
    private lateinit var featureAdapter: HomeFeatureAdapter

    // Permission and File Picker
    private val selectVideoLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            playVideo(it)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            selectVideoLauncher.launch("video/*")
        } else {
            Toast.makeText(
                this,
                R.string.permission_rationale,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        setupToolbar()
        setupRecyclerView()
        observeViewModel()
    }

    /**
     * 设置 Toolbar
     */
    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white))
        toolbar.overflowIcon?.setTint(ContextCompat.getColor(this, R.color.white))
    }

    /**
     * 设置 RecyclerView
     */
    private fun setupRecyclerView() {
        recyclerViewFeatures = findViewById(R.id.recyclerViewFeatures)

        featureAdapter = HomeFeatureAdapter { feature ->
            onFeatureClick(feature)
        }

        recyclerViewFeatures.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = featureAdapter
            setHasFixedSize(true)
        }
    }

    /**
     * 观察 ViewModel 数据
     */
    private fun observeViewModel() {
        viewModel.features.observe(this) { features ->
            featureAdapter.submitList(features)
        }

        viewModel.localVideoCount.observe(this) { _ ->
            // TODO: 更新本地视频卡片的徽章
        }
    }

    /**
     * 处理功能卡片点击
     */
    private fun onFeatureClick(feature: HomeFeature) {
        when (feature.id) {
            FeatureType.LOCAL_VIDEOS -> {
                val intent =
                    Intent(this, com.live.visionplay.ui.local.LocalVideosActivity::class.java)
                startActivity(intent)
            }

            FeatureType.SINGLE_VIDEO -> {
                checkPermissionAndPickVideo()
            }

            FeatureType.REMOTE_VIDEO -> {
                val intent =
                    Intent(this, com.live.visionplay.ui.remote.RemoteVideoActivity::class.java)
                startActivity(intent)
            }

            FeatureType.LIVE_TV -> {
                val intent = Intent(this, com.live.visionplay.ui.livetv.LiveTvActivity::class.java)
                startActivity(intent)
            }
        }
    }

    /**
     * 检查权限并选择视频
     */
    private fun checkPermissionAndPickVideo() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_VIDEO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                selectVideoLauncher.launch("video/*")
            }

            shouldShowRequestPermissionRationale(permission) -> {
                Toast.makeText(
                    this,
                    R.string.permission_rationale,
                    Toast.LENGTH_LONG
                ).show()
                requestPermissionLauncher.launch(permission)
            }

            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    /**
     * 播放视频
     */
    private fun playVideo(uri: Uri) {
        val intent =
            Intent(this, com.live.visionplay.player.ui.VideoPlayerActivity::class.java).apply {
                data = uri
            }
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                // TODO: 打开设置页面
                Toast.makeText(this, "设置功能即将推出", Toast.LENGTH_SHORT).show()
                true
            }

            R.id.action_about -> {
                startActivity(Intent(this, com.live.visionplay.ui.about.AboutActivity::class.java))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        // 刷新视频数量
        viewModel.refreshVideoCount()
    }
}
