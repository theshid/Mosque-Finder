package com.shid.mosquefinder.app.ui.main.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shid.mosquefinder.data.model.pojo.VerseResponse
import com.shid.mosquefinder.data.local.database.entities.AyahDb
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.ui.main.view_models.AyahViewModel
import kotlinx.android.synthetic.main.item_quran_ayah.view.*
import java.util.*
import kotlin.collections.ArrayList

class AyahAdapter(val viewmodel:AyahViewModel) :
    RecyclerView.Adapter<AyahAdapter.AyahViewHolder>() {

    private var listData = ArrayList<AyahDb>()
    private var frenchList = ArrayList<VerseResponse>()
    lateinit var onClickAyah: OnClickAyah

    interface OnClickAyah {
        fun onClickAyah(ayah: AyahDb)
    }

    fun setOnItemClick(mOnClickAyah: OnClickAyah) {
        onClickAyah = mOnClickAyah
    }

    fun setData(newListData: List<AyahDb>) {
        listData.clear()
        listData.addAll(newListData)
        notifyDataSetChanged()
    }

    fun setNetworkList(list: List<VerseResponse>){
        frenchList.clear()
        frenchList.addAll(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AyahViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_quran_ayah, parent, false)
        return AyahViewHolder(view)
    }

    override fun onBindViewHolder(holder: AyahViewHolder, position: Int) {
        listData[position].run { holder.bind(this) }
        holder.itemView.setOnClickListener(View.OnClickListener {
            listData[position].let { it -> onClickAyah.onClickAyah(it) }
        })
    }

    override fun getItemCount(): Int {
        return listData.size
    }


    inner class AyahViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        fun bind(ayah: AyahDb) {
            if(Locale.getDefault().language.contentEquals("fr")){
                itemView.apply {
                    tv_item_ayah_verse.text = ayah.verse_number.toString()
                    tv_item_ayah_arab.text = ayah.originalText
                }
                if(ayah.frenchTranslation == null || ayah.frenchTranslation.equals("empty")){
                    for (item in frenchList){
                        Log.d("Adapter","item number:" + item.numInSurah)
                        Log.d("Adapter","ayah number:" + ayah.verse_number)
                        if (item.numInSurah == ayah.verse_number){
                            itemView.tv_item_ayah_translate.text = item.trans
                            viewmodel.updateAyah(item.trans,ayah.id)
                            break
                        }
                    }
                }

                else{
                    itemView.tv_item_ayah_translate.text = ayah.frenchTranslation

                }

            } else{
                itemView.apply {
                    tv_item_ayah_verse.text = ayah.verse_number.toString()
                    tv_item_ayah_arab.text = ayah.originalText
                    tv_item_ayah_translate.text = ayah.translation
                }
            }
             /*else{
                itemView.apply {
                    tv_item_ayah_verse.text = ayah.verse_number.toString()
                    tv_item_ayah_arab.text = ayah.originalText
                    tv_item_ayah_translate.text = ayah.translation

                }
            }*/

        }

    }


}