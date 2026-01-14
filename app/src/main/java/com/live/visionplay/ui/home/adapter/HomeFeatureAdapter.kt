package com.live.visionplay.ui.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.live.visionplay.R
import com.live.visionplay.ui.home.model.HomeFeature

/**
 * 主页功能卡片适配器
 *
 * 使用 ListAdapter 和 DiffUtil 优化性能。
 * 参考 Android RecyclerView 最佳实践。
 *
 * 主页面重构 - 第三阶段
 *
 * @param onFeatureClick 卡片点击回调
 */
class HomeFeatureAdapter(
    private val onFeatureClick: (HomeFeature) -> Unit
) : ListAdapter<HomeFeature, HomeFeatureAdapter.FeatureViewHolder>(FeatureDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeatureViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_feature_card, parent, false)
        return FeatureViewHolder(view, onFeatureClick)
    }

    override fun onBindViewHolder(holder: FeatureViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * ViewHolder for feature card
     */
    class FeatureViewHolder(
        itemView: View,
        private val onFeatureClick: (HomeFeature) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val cardFeature: MaterialCardView = itemView.findViewById(R.id.cardFeature)
        private val ivFeatureIcon: ImageView = itemView.findViewById(R.id.ivFeatureIcon)
        private val tvFeatureTitle: TextView = itemView.findViewById(R.id.tvFeatureTitle)
        private val tvFeatureDescription: TextView = itemView.findViewById(R.id.tvFeatureDescription)

        fun bind(feature: HomeFeature) {
            // Set data
            ivFeatureIcon.setImageResource(feature.iconRes)
            tvFeatureTitle.setText(feature.title)
            tvFeatureDescription.setText(feature.description)

            // Set click listener
            cardFeature.setOnClickListener {
                onFeatureClick(feature)
            }

            // Badge support (future enhancement)
            // TODO: Implement badge when needed
        }
    }

    /**
     * DiffUtil callback for efficient list updates
     */
    private class FeatureDiffCallback : DiffUtil.ItemCallback<HomeFeature>() {
        override fun areItemsTheSame(oldItem: HomeFeature, newItem: HomeFeature): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: HomeFeature, newItem: HomeFeature): Boolean {
            return oldItem == newItem
        }
    }
}
