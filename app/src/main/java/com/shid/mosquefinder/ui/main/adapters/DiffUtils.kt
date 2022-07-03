package com.shid.mosquefinder.ui.main.adapters

import androidx.recyclerview.widget.DiffUtil
import dev.kosrat.muslimdata.models.AzkarItem

class DefaultAzkharItemDiffCallback : DiffUtil.ItemCallback<AzkarItem>() {

    override fun areItemsTheSame(oldItem: AzkarItem, newItem: AzkarItem): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: AzkarItem, newItem: AzkarItem): Boolean {
        return oldItem.itemId == newItem.itemId
    }
}