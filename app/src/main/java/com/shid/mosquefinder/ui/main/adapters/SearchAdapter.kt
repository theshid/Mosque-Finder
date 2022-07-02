package com.shid.mosquefinder.ui.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.shid.mosquefinder.data.model.ClusterMarker
import com.shid.mosquefinder.R
import kotlinx.android.synthetic.main.item_search.view.*
import java.util.*
import kotlin.collections.ArrayList

class SearchAdapter constructor(var context: Context, var listener: OnClickSearch) : RecyclerView.Adapter<SearchAdapter.SearchViewHolder>(),
Filterable{
    var list: MutableList<ClusterMarker>? = null
    var mosqueList: MutableList<ClusterMarker>? = null
    var onClickSearch: OnClickSearch? = null




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = SearchViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_search, parent, false)
    )

    override fun getItemCount() = mosqueList?.size ?: 0

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        mosqueList?.get(position)?.run { holder.bind(this) }
    }

    inline fun setOnClickSearch(crossinline onClickSearch: (clusterMarker: ClusterMarker) -> Unit) {
        this.onClickSearch = object : OnClickSearch {
            override fun onClickSearch(clusterMarker: ClusterMarker) {
                onClickSearch(clusterMarker)
            }
        }
    }

    inner class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
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

    override fun getFilter(): Filter {
        return object:Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                    if (list?.isNotEmpty()!!){
                        mosqueList = list!!
                    }

                } else {
                    val resultList = ArrayList<ClusterMarker>()
                    for (row in list!!) {
                        if (row.title!!.toLowerCase(Locale.ROOT).contains(charSearch.toLowerCase(Locale.ROOT))) {
                            resultList.add(row)
                        }
                    }
                    mosqueList = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = mosqueList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                mosqueList = results?.values as ArrayList<ClusterMarker>
                notifyDataSetChanged()
            }

        }
    }
}