package com.shid.mosquefinder.Ui.Main.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.shid.mosquefinder.Data.Model.BeautifulMosques
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Ui.Main.View.DetailActivity
import kotlinx.android.synthetic.main.item_mosque.view.*


class MosqueAdapter(var list: MutableList<BeautifulMosques>, var context: Context) :
    RecyclerView.Adapter<MosqueAdapter.ViewHolder>() {
    private lateinit var layoutInflater: LayoutInflater
    private var onClickedTime = System.currentTimeMillis()
    var onClickMosque:OnClickMosque ?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MosqueAdapter.ViewHolder {
        layoutInflater = LayoutInflater.from(context)
        val view: View = layoutInflater.inflate(R.layout.item_mosque, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: MosqueAdapter.ViewHolder, position: Int) {
        list[position].run { holder.bind(this) }
    }

    override fun getItemCount(): Int = list.size

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
               /* setOnClickListener {
                    onClickMosque?.onClickSearch(mosque)
                }*/

            }
        }
    }

    interface OnClickMosque {
        fun onClickSearch(mosque: BeautifulMosques)
    }

    inline fun setOnClickMosque(crossinline onClickMosque: (mosque: BeautifulMosques) -> Unit) {
        this.onClickMosque = object : MosqueAdapter.OnClickMosque {
            override fun onClickSearch(mosque: BeautifulMosques) {
                onClickMosque(mosque)
            }
        }
    }
}