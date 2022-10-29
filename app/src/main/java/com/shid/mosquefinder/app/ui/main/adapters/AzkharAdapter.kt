package com.shid.mosquefinder.app.ui.main.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.ui.main.views.AzkharActivity
import com.shid.mosquefinder.app.utils.helper_class.Constants
import com.shid.mosquefinder.data.model.AzkarII
import dev.kosrat.muslimdata.models.AzkarItem
import kotlinx.android.synthetic.main.item_azkhar.view.*
import timber.log.Timber
import java.util.*

internal class AzkharAdapter(val listener: (AzkarII) -> Unit) :
    ListAdapter<AzkarII, AzkharAdapter.ItemViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<AzkarII>() {
            override fun areItemsTheSame(oldItem: AzkarII, newItem: AzkarII): Boolean =
                oldItem.translation == newItem.translation

            override fun areContentsTheSame(oldItem: AzkarII, newItem: AzkarII): Boolean =
                oldItem == newItem
        }

        var itemToSend:AzkarII?=null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_azkhar, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(item)
        holder.itemView.btn_translate.setOnClickListener {
            listener(item)
            itemToSend = item
        }
        Timber.d("position:$position")
    }


    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(azkarItem: AzkarII) {
            if (Locale.getDefault().language.contentEquals(Constants.FRENCH_VERSION)) {
                itemView.btn_translate.visibility = View.VISIBLE
                itemView.tv_item_translate_french.text = azkarItem.item
            }
            itemView.tv_item_id.text = azkarItem.itemId.toString()
            itemView.tv_item_arab.text = azkarItem.item
            itemView.tv_item_translate.text = azkarItem.translation
            itemView.tv_item_source.text = azkarItem.reference
        }

    }

}