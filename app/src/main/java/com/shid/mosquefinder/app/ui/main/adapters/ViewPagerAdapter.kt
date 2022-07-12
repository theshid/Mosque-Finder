package com.shid.mosquefinder.app.ui.main.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shid.mosquefinder.R
import com.shid.mosquefinder.data.model.Quotes
import kotlinx.android.synthetic.main.item_pager.view.*
import java.util.*

class ViewPagerAdapter() :
    ListAdapter<Quotes, ViewPagerAdapter.ViewHolder>(DIFF_CALLBACK) {

    private lateinit var layoutInflater: LayoutInflater

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Quotes>() {
            override fun areItemsTheSame(oldItem: Quotes, newItem: Quotes): Boolean =
                oldItem.quote == newItem.quote

            override fun areContentsTheSame(oldItem: Quotes, newItem: Quotes): Boolean =
                oldItem == newItem
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerAdapter.ViewHolder {
        layoutInflater = LayoutInflater.from(parent.context)
        val view: View = layoutInflater.inflate(R.layout.item_pager, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewPagerAdapter.ViewHolder, position: Int) {
        val quote = getItem(position)
        holder.bind(quote)
        //list[position].run { holder.bind(this) }
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(quotes: Quotes) {
            itemView.apply {
                author.text = quotes.author
                if (Locale.getDefault().language == "fr") {
                    quote_text.text = " ''" + quotes.quote_fr + "'' "
                } else {
                    quote_text.text = " ''" + quotes.quote + "'' "
                }

            }
        }

    }
}