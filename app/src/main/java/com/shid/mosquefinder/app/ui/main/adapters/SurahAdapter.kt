package com.shid.mosquefinder.app.ui.main.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.shid.mosquefinder.data.local.database.entities.SurahDb
import com.shid.mosquefinder.R
import kotlinx.android.synthetic.main.item_quran_surah.view.*
import java.util.*
import kotlin.collections.ArrayList

class SurahAdapter() :
    RecyclerView.Adapter<SurahAdapter.SurahViewHolder>(),Filterable {
    lateinit var onClickSurah: OnClickSurah
     var list:MutableList<SurahDb>?=null
    private var listData  = ArrayList<SurahDb>()

    fun setData(newListData: List<SurahDb>) {
        listData.clear()
        listData.addAll(newListData)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SurahViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_quran_surah, parent, false)
        return SurahViewHolder(view)
    }

    override fun onBindViewHolder(holder: SurahViewHolder, position: Int) {
        listData[position].run { holder.bind(this) }
        holder.itemView.setOnClickListener(View.OnClickListener {
            listData[position].let { it -> onClickSurah.onClickSurah(it) }
        })
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    interface OnClickSurah {
        fun onClickSurah(surahDb: SurahDb)
    }

    fun setItemClick(mOnClickSurah: OnClickSurah){
       onClickSurah = mOnClickSurah
    }



    inner class SurahViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        fun bind(surahDb: SurahDb) {
            itemView.apply {
                tv_item_surah_number.text = surahDb.id.toString()
                tv_item_surah_surah.text = surahDb.transliteration
                tv_item_surah_arab.text = surahDb.name
                tv_item_surah_revelation_type.text = surahDb.revelationType
                tv_item_surah_total_ayah.text = surahDb.totalVerses.toString() + " " +"Ayah"

            }
        }

    }

    override fun getFilter(): Filter {
        return object:Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                    if (list?.isNotEmpty() == true){
                        listData = list as ArrayList<SurahDb>
                    }

                } else {
                    val resultList = ArrayList<SurahDb>()
                    if (list != null){
                        for (row in list!!) {
                            if (row.transliteration.toLowerCase(Locale.ROOT).contains(charSearch.toLowerCase(
                                    Locale.ROOT))) {
                                resultList.add(row)
                            }
                        }
                    }

                    listData = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = listData
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                listData = results?.values as ArrayList<SurahDb>
                notifyDataSetChanged()
            }

        }
    }


}