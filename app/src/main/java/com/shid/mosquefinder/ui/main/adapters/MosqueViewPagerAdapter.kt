package com.shid.mosquefinder.ui.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.shid.mosquefinder.R
import kotlinx.android.synthetic.main.mosque_item.view.*

class MosqueViewPagerAdapter(var list: MutableList<String>, var context: Context) :
    RecyclerView.Adapter<MosqueViewPagerAdapter.ViewHolder>() {

    private lateinit var layoutInflater: LayoutInflater


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MosqueViewPagerAdapter.ViewHolder {
        layoutInflater = LayoutInflater.from(context)
        val view: View = layoutInflater.inflate(R.layout.mosque_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: MosqueViewPagerAdapter.ViewHolder, position: Int) {
        list[position].run { holder.bind(this) }
    }

    override fun getItemCount(): Int = list.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(link: String) {
            itemView.apply {
                mosque_img.load(link)
            }
        }

    }
}