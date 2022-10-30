package com.shid.mosquefinder.app.ui.main.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.ui.main.views.DetailActivity
import com.shid.mosquefinder.data.model.BeautifulMosques
import kotlinx.android.synthetic.main.item_mosque.view.*


class MosqueAdapter() :
    ListAdapter<BeautifulMosques, MosqueAdapter.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<BeautifulMosques>() {
            override fun areItemsTheSame(
                oldItem: BeautifulMosques,
                newItem: BeautifulMosques
            ): Boolean =
                oldItem.name == newItem.name

            override fun areContentsTheSame(
                oldItem: BeautifulMosques,
                newItem: BeautifulMosques
            ): Boolean =
                oldItem == newItem
        }
    }

    private lateinit var layoutInflater: LayoutInflater
    private var onClickedTime = System.currentTimeMillis()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MosqueAdapter.ViewHolder {
        layoutInflater = LayoutInflater.from(parent.context)
        val view: View = layoutInflater.inflate(R.layout.item_mosque, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: MosqueAdapter.ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(mosque: BeautifulMosques) {
            itemView.apply {
                name.text = mosque.name
                image.load(mosque.link)

                rootView.setOnClickListener {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - onClickedTime > transformationLayout.duration) {
                        onClickedTime = currentTime
                        DetailActivity.startActivity(transformationLayout, mosque)
                    }
                }

            }
        }
    }

}