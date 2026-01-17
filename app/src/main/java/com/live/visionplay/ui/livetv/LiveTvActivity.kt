package com.live.visionplay.ui.livetv

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.size
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.live.visionplay.R
import com.live.visionplay.data.TvChannelRepository
import com.live.visionplay.databinding.ActivityLiveTvBinding
import com.live.visionplay.model.TvChannel
import com.live.visionplay.player.ui.LiveStreamPlayerActivity
import java.util.UUID

class LiveTvActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLiveTvBinding
    private lateinit var liveTvAdapter: LiveTvAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLiveTvBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化仓库
        TvChannelRepository.init(this)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        setupRecyclerView()
        loadChannels()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.live_tv_menu, menu)
        // 确保菜单项图标颜色正确
        for (i in 0 until (menu?.size ?: 0)) {
            val item = menu?.getItem(i)
            item?.icon?.setTint(ContextCompat.getColor(this, R.color.white))
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_channel -> {
                showAddChannelDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView() {
        liveTvAdapter = LiveTvAdapter(
            emptyList(),
            onClick = { channel -> playChannel(channel) },
            onLongClick = { channel -> showEditChannelDialog(channel) }
        )
        binding.channelsRecyclerView.apply {
            layoutManager = GridLayoutManager(this@LiveTvActivity, 3)
            adapter = liveTvAdapter
        }
    }

    private fun loadChannels() {
        val channels = TvChannelRepository.getChannels()
        if (channels.isNotEmpty()) {
            liveTvAdapter.updateData(channels)
            binding.root.findViewById<View>(R.id.empty_state)?.visibility = View.GONE
            binding.channelsRecyclerView.visibility = View.VISIBLE
        } else {
            // 手动绑定 include 布局，避开 View Binding 可能生成的 null 问题
            val emptyStateView = binding.root.findViewById<View>(R.id.empty_state)
            if (emptyStateView != null) {
                val emptyBinding = com.live.visionplay.databinding.LayoutEmptyStateBinding.bind(emptyStateView)
                emptyBinding.emptyStateRoot.visibility = View.VISIBLE
                emptyBinding.emptyIcon.setImageResource(R.drawable.ic_tv_24)
                emptyBinding.emptyTitle.setText(R.string.live_tv)
                emptyBinding.emptyDescription.setText(R.string.empty_live_tv_desc)
                emptyBinding.emptyActionButton.apply {
                    visibility = View.VISIBLE
                    setText(R.string.add_channel)
                    setOnClickListener { showAddChannelDialog() }
                }
            }
            binding.channelsRecyclerView.visibility = View.GONE
        }
    }

    private fun showAddChannelDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_channel, null)
        val nameInput = dialogView.findViewById<TextInputEditText>(R.id.channel_name_input)
        val urlInput = dialogView.findViewById<TextInputEditText>(R.id.channel_url_input)
        val logoUrlInput = dialogView.findViewById<TextInputEditText>(R.id.channel_logo_url_input)

        AlertDialog.Builder(this)
            .setTitle(R.string.add_channel_title)
            .setView(dialogView)
            .setPositiveButton(R.string.add) { _, _ ->
                val name = nameInput.text.toString().trim()
                val url = urlInput.text.toString().trim()
                val logoUrl = logoUrlInput.text.toString().trim()

                if (name.isNotEmpty() && url.isNotEmpty()) {
                    val newChannel = TvChannel(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        streamUrl = url,
                        logoUrl = if (logoUrl.isNotBlank()) logoUrl else null
                    )
                    TvChannelRepository.addChannel(newChannel)
                    loadChannels()
                    Toast.makeText(this, R.string.add_success, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, R.string.please_input_complete_info, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showEditChannelDialog(channel: TvChannel) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_channel, null)
        val nameInput = dialogView.findViewById<TextInputEditText>(R.id.channel_name_input)
        val urlInput = dialogView.findViewById<TextInputEditText>(R.id.channel_url_input)
        val logoUrlInput = dialogView.findViewById<TextInputEditText>(R.id.channel_logo_url_input)

        nameInput.setText(channel.name)
        urlInput.setText(channel.streamUrl)
        logoUrlInput.setText(channel.logoUrl)

        AlertDialog.Builder(this)
            .setTitle(R.string.edit_channel_title)
            .setView(dialogView)
            .setPositiveButton(R.string.save) { _, _ ->
                val name = nameInput.text.toString().trim()
                val url = urlInput.text.toString().trim()
                val logoUrl = logoUrlInput.text.toString().trim()

                if (name.isNotEmpty() && url.isNotEmpty()) {
                    val updatedChannel = channel.copy(
                        name = name,
                        streamUrl = url,
                        logoUrl = logoUrl
                    )
                    TvChannelRepository.updateChannel(updatedChannel)
                    loadChannels()
                    Toast.makeText(this, R.string.update_success, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, R.string.please_input_complete_info, Toast.LENGTH_SHORT).show()
                }
            }
            .setNeutralButton(R.string.delete) { _, _ ->
                showDeleteConfirmDialog(channel)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showDeleteConfirmDialog(channel: TvChannel) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete)
            .setMessage(getString(R.string.delete_channel_confirm))
            .setPositiveButton(R.string.delete) { _, _ ->
                TvChannelRepository.removeChannel(channel.id)
                loadChannels()
                Toast.makeText(this, R.string.delete_success, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun playChannel(channel: TvChannel) {
        val intent = Intent(this, LiveStreamPlayerActivity::class.java).apply {
            data = channel.streamUrl.toUri()
            putExtra(LiveStreamPlayerActivity.EXTRA_STREAM_NAME, channel.name)
        }
        startActivity(intent)
    }
}
