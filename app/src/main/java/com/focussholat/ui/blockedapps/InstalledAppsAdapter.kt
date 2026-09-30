package com.focussholat.ui.blockedapps

import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.focussholat.databinding.ItemAppBinding

class InstalledAppsAdapter(
    private val onBlockToggle: (AppDisplayItem, Boolean) -> Unit
) : ListAdapter<AppDisplayItem, InstalledAppsAdapter.AppViewHolder>(AppDiffCallback()) {

    inner class AppViewHolder(private val binding: ItemAppBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AppDisplayItem) {
            binding.tvAppName.text = item.appName
            binding.tvPackageName.text = item.packageName

            // Load app icon
            try {
                val pm = binding.root.context.packageManager
                val icon = pm.getApplicationIcon(item.packageName)
                binding.ivAppIcon.setImageDrawable(icon)
            } catch (e: PackageManager.NameNotFoundException) {
                binding.ivAppIcon.setImageResource(com.focussholat.R.drawable.ic_app_default)
            }

            binding.switchBlock.setOnCheckedChangeListener(null)
            binding.switchBlock.isChecked = item.isChecked
            binding.switchBlock.setOnCheckedChangeListener { _, isChecked ->
                onBlockToggle(item, isChecked)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemAppBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AppDiffCallback : DiffUtil.ItemCallback<AppDisplayItem>() {
        override fun areItemsTheSame(oldItem: AppDisplayItem, newItem: AppDisplayItem) =
            oldItem.packageName == newItem.packageName

        override fun areContentsTheSame(oldItem: AppDisplayItem, newItem: AppDisplayItem) =
            oldItem == newItem
    }
}
