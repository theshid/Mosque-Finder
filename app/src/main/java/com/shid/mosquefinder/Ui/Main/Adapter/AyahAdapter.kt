package com.shid.mosquefinder.Ui.Main.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shid.mosquefinder.Data.database.entities.Ayah
import com.shid.mosquefinder.Data.database.entities.Surah
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Ui.Main.ViewModel.AyahViewModel
import kotlinx.android.synthetic.main.item_azkhar.view.*
import kotlinx.android.synthetic.main.item_quran_ayah.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class AyahAdapter(val viewmodel:AyahViewModel) :
    RecyclerView.Adapter<AyahAdapter.AyahViewHolder>() {

    private var listData = ArrayList<Ayah>()
    lateinit var onClickAyah: OnClickAyah

    interface OnClickAyah {
        fun onClickAyah(ayah: Ayah)
    }

    fun setOnItemClick(mOnClickAyah: OnClickAyah) {
        onClickAyah = mOnClickAyah
    }

    fun setData(newListData: List<Ayah>) {
        listData.clear()
        listData.addAll(newListData)
        notifyDataSetChanged()
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
        fun bind(ayah: Ayah) {
            if(Locale.getDefault().language.contentEquals("fr")){
                if (ayah.frenchTranslation.equals("empty")){
                    GlobalScope.launch(Dispatchers.Main){
                        viewmodel.fetchFrenchAyah(ayah.id)
                        itemView.apply {
                            tv_item_ayah_verse.text = ayah.verse_number.toString()
                            tv_item_ayah_arab.text = ayah.originalText


                        }
                        delay(2000)

                        itemView.tv_item_ayah_translate.text = viewmodel.getFrenchTrans()

                    }

                }
            } else{
                itemView.apply {
                    tv_item_ayah_verse.text = ayah.verse_number.toString()
                    tv_item_ayah_arab.text = ayah.originalText
                    tv_item_ayah_translate.text = ayah.translation

                }
            }

        }

    }


}