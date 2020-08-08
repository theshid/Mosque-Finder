package com.shid.mosquefinder.Ui.Main.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shid.mosquefinder.Data.Model.ClusterMarker
import com.shid.mosquefinder.R
import kotlinx.android.synthetic.main.item_search.view.*

class SearchAdapter constructor(var context: Context, var listener: OnClickSearch) : RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {
    var list: MutableList<ClusterMarker>? = null
    var onClickSearch: OnClickSearch? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = SearchViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_search, parent, false)
    )

    override fun getItemCount() = list?.size ?: 0

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        list?.get(position)?.run { holder.bind(this) }
    }

    inline fun setOnClickSearch(crossinline onClickSearch: (clusterMarker: ClusterMarker) -> Unit) {
        this.onClickSearch = object : OnClickSearch {
            override fun onClickSearch(clusterMarker: ClusterMarker) {
                onClickSearch(clusterMarker)
            }
        }
    }

    inner class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(clusterMarker: ClusterMarker) {
            itemView.apply {
                nameText.text = clusterMarker.title
                setOnClickListener {
                    onClickSearch?.onClickSearch(clusterMarker)
                }
            }
        }
    }

    interface OnClickSearch {
        fun onClickSearch(clusterMarker: ClusterMarker)
    }
}