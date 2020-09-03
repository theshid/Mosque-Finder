package com.shid.mosquefinder.Ui.Main.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shid.mosquefinder.Data.Model.Quotes
import com.shid.mosquefinder.R
import kotlinx.android.synthetic.main.item_pager.view.*
import java.util.*

class ViewPagerAdapter(var list: MutableList<Quotes>, var context: Context) :
    RecyclerView.Adapter<ViewPagerAdapter.ViewHolder>() {

    private lateinit var layoutInflater: LayoutInflater


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerAdapter.ViewHolder {
        layoutInflater = LayoutInflater.from(context)
        val view: View = layoutInflater.inflate(R.layout.item_pager, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewPagerAdapter.ViewHolder, position: Int) {
        list[position].run { holder.bind(this) }
    }

    override fun getItemCount(): Int = list.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(quotes: Quotes) {
            itemView.apply {
                author.text = quotes.author
                if (Locale.getDefault().language == "fr"){
                    quote_text.text = " ''" + quotes.quote_fr + "'' "
                }else{
                    quote_text.text = " ''" + quotes.quote + "'' "
                }

            }
        }

    }
}