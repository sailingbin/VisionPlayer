package com.live.visionplay.ui.about

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.live.visionplay.BuildConfig
import com.live.visionplay.R
import com.live.visionplay.data.UpdateRepository
import com.live.visionplay.data.VersionInfo
import com.live.visionplay.databinding.ActivityAboutBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding
    private var loadingDialog: AlertDialog? = null

    // 使用自定义Factory来创建ViewModel实例
    private val viewModel: AboutViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AboutViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return AboutViewModel(UpdateRepository()) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // 设置版本名
        binding.appVersionText.text = "Version ${BuildConfig.VERSION_NAME}"

        // 监听检查更新按钮
        binding.checkForUpdatesButton.setOnClickListener {
            viewModel.checkForUpdates(BuildConfig.VERSION_CODE)
        }

        // 监听在线隐私协议按钮
        binding.btnViewPrivacyOnline.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = getString(R.string.privacy_policy_url).toUri()
            }
            startActivity(intent)
        }

        // 观察ViewModel的状态
        observeUpdateState()
    }

    private fun observeUpdateState() {
        lifecycleScope.launch {
            viewModel.updateState.collectLatest { state ->
                when (state) {
                    is UpdateState.Loading -> showLoadingDialog()
                    is UpdateState.NoUpdate -> {
                        dismissLoadingDialog()
                        Toast.makeText(this@AboutActivity, getString(R.string.you_are_up_to_date), Toast.LENGTH_SHORT).show()
                    }
                    is UpdateState.UpdateAvailable -> {
                        dismissLoadingDialog()
                        showUpdateAvailableDialog(state.versionInfo)
                    }
                    is UpdateState.Error -> {
                        dismissLoadingDialog()
                        Toast.makeText(this@AboutActivity, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                    }
                    is UpdateState.Idle -> {
                        // 初始状态，不做任何事
                    }
                }
            }
        }
    }

    private fun showLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = MaterialAlertDialogBuilder(this)
                .setTitle(R.string.checking_for_updates)
                .setMessage(R.string.please_wait)
                .setCancelable(false)
                .create()
        }
        loadingDialog?.show()
    }

private fun dismissLoadingDialog() {
    loadingDialog?.dismiss()
}

private fun showUpdateAvailableDialog(versionInfo: VersionInfo) {
    val releaseNotes = versionInfo.releaseNotes.joinToString("\n") { "• $it" }
    MaterialAlertDialogBuilder(this)
        .setTitle(getString(R.string.new_version_found, versionInfo.versionName))
        .setMessage(releaseNotes)
        .setNegativeButton(R.string.later, null)
        .setPositiveButton(R.string.update_now) { _, _ ->
            val intent = Intent(Intent.ACTION_VIEW, versionInfo.downloadUrl.toUri())
            startActivity(intent)
        }
        .show()
}
}