package com.shid.mosquefinder.app.ui.main.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shid.mosquefinder.data.local.database.entities.SurahDb
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.ui.models.SurahPresentation
import dev.kosrat.muslimdata.models.AzkarItem
import kotlinx.android.synthetic.main.item_quran_surah.view.*
import java.util.*
import kotlin.collections.ArrayList

class SurahAdapter() :
    ListAdapter<SurahPresentation,SurahAdapter.SurahViewHolder>(DIFF_CALLBACK),Filterable {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SurahPresentation>() {
            override fun areItemsTheSame(oldItem: SurahPresentation, newItem: SurahPresentation): Boolean =
                oldItem.number == newItem.number

            override fun areContentsTheSame(oldItem: SurahPresentation, newItem: SurahPresentation): Boolean =
                oldItem == newItem
        }
    }
    lateinit var onClickSurah: OnClickSurah
     var list:MutableList<SurahPresentation>?=null
    private var listData  = ArrayList<SurahPresentation>()

    /*fun setData(newListData: List<SurahDb>) {
        listData.clear()
        listData.addAll(newListData)
        notifyDataSetChanged()
    }*/

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SurahViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_quran_surah, parent, false)
        listData.clear()
        listData.addAll(currentList)
        return SurahViewHolder(view)
    }

    override fun onBindViewHolder(holder: SurahViewHolder, position: Int) {
        //listData[position].run { holder.bind(this) }
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener(View.OnClickListener {
            listData[position].let { it -> onClickSurah.onClickSurah(it) }
        })

    }

    /*override fun getItemCount(): Int {
        return listData.size
    }*/

    interface OnClickSurah {
        fun onClickSurah(surahPresentation: SurahPresentation)
    }

    fun setItemClick(mOnClickSurah: OnClickSurah){
       onClickSurah = mOnClickSurah
    }


    inner class SurahViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        fun bind(surahDb: SurahPresentation) {
            itemView.apply {
                tv_item_surah_number.text = surahDb.number.toString()
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
                        listData = list as ArrayList<SurahPresentation>
                    }

                } else {
                    val resultList = ArrayList<SurahPresentation>()
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
                listData = results?.values as ArrayList<SurahPresentation>
                submitList(listData)
                //notify
            }

        }
    }


}