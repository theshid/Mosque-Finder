package com.shid.mosquefinder.Ui.Main.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.shid.mosquefinder.Data.Model.ClusterMarker
import com.shid.mosquefinder.Data.database.entities.Surah
import com.shid.mosquefinder.R
import kotlinx.android.synthetic.main.item_quran_surah.view.*
import java.util.*
import kotlin.collections.ArrayList

class SurahAdapter(private var surahList: List<Surah>) :
    RecyclerView.Adapter<SurahAdapter.SurahViewHolder>(),Filterable {
    lateinit var onClickSurah: OnClickSurah
     var list:MutableList<Surah>?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SurahViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_quran_surah, parent, false)
        return SurahViewHolder(view)
    }

    override fun onBindViewHolder(holder: SurahViewHolder, position: Int) {
        surahList[position].run { holder.bind(this) }
        holder.itemView.setOnClickListener(View.OnClickListener {
            surahList[position].let { it -> onClickSurah.onClickSurah(it) }
        })
    }

    override fun getItemCount(): Int {
        return surahList.size
    }

    interface OnClickSurah {
        fun onClickSurah(surah: Surah)
    }

    fun setItemClick(mOnClickSurah: OnClickSurah){
       onClickSurah = mOnClickSurah
    }



    inline fun setOnClickSurah(crossinline onClickSurah: (surah: Surah) -> Unit) {
        this.onClickSurah = object : OnClickSurah {
            override fun onClickSurah(surah: Surah) {
                onClickSurah(surah)
            }
        }
    }


    inner class SurahViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        fun bind(surah: Surah) {
            itemView.apply {
                tv_item_surah_number.text = surah.id.toString()
                tv_item_surah_surah.text = surah.transliteration
                tv_item_surah_arab.text = surah.name
                tv_item_surah_revelation_type.text = surah.revelationType
                tv_item_surah_total_ayah.text = surah.totalVerses.toString() + " " +"Ayah"

            }
        }

    }

    override fun getFilter(): Filter {
        return object:Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                    if (list?.isNotEmpty()!!){
                        surahList = list!!
                    }

                } else {
                    val resultList = ArrayList<Surah>()
                    for (row in list!!) {
                        if (row.transliteration!!.toLowerCase(Locale.ROOT).contains(charSearch.toLowerCase(
                                Locale.ROOT))) {
                            resultList.add(row)
                        }
                    }
                    surahList = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = surahList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                surahList = results?.values as List<Surah>
                notifyDataSetChanged()
            }

        }
    }


}