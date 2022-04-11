package com.shid.mosquefinder.Ui.Main.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shid.mosquefinder.Data.database.entities.Ayah
import com.shid.mosquefinder.R
import dev.kosrat.muslimdata.models.NameOfAllah
import kotlinx.android.synthetic.main.item_names.view.*
import kotlinx.android.synthetic.main.item_quran_ayah.view.*

class NameAdapter():RecyclerView.Adapter<NameAdapter.NameViewHolder>() {
    private var listData = ArrayList<NameOfAllah>()


    fun setData(newListData: List<NameOfAllah>) {
        listData.clear()
        listData.addAll(newListData)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NameAdapter.NameViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_names, parent, false)
        return NameViewHolder(view)
    }

    override fun onBindViewHolder(holder: NameAdapter.NameViewHolder, position: Int) {
        listData[position].run { holder.bind(this) }
      /*  holder.itemView.setOnClickListener(View.OnClickListener {
            listData[position].let { it -> onClickName.onClickName(it) }
        })*/
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    inner class NameViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        fun bind(name: NameOfAllah) {
            itemView.apply {
                tvNumberName.text = name.number.toString()
                tvArabic.text = name.name
                tvTransliteration.text = name.translation
            }
        }

    }
}