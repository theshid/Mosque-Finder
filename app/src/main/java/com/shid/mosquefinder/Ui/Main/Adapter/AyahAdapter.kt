package com.shid.mosquefinder.Ui.Main.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shid.mosquefinder.Data.database.entities.Ayah
import com.shid.mosquefinder.R
import kotlinx.android.synthetic.main.item_quran_ayah.view.*

class AyahAdapter(private val ayahList: List<Ayah>) :
    RecyclerView.Adapter<AyahAdapter.AyahViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AyahViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_quran_ayah, parent, false)
        return AyahViewHolder(view)
    }

    override fun onBindViewHolder(holder: AyahViewHolder, position: Int) {
       ayahList[position].run { holder.bind(this) }
    }

    override fun getItemCount(): Int {
        return ayahList.size
    }


    inner class AyahViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
            fun bind(ayah: Ayah){
                itemView.apply {
                    tv_item_ayah_verse.text = ayah.verse_number.toString()
                    tv_item_ayah_arab.text = ayah.originalText
                    tv_item_ayah_translate.text = ayah.translation
                }
            }

    }


}