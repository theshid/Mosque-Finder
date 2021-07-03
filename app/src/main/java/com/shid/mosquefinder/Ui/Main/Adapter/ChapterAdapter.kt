package com.shid.mosquefinder.Ui.Main.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shid.mosquefinder.Data.database.entities.Chapter
import com.shid.mosquefinder.R
import dev.kosrat.muslimdata.models.AzkarChapter
import kotlinx.android.synthetic.main.item_chapter.view.*
import java.util.*
import kotlin.collections.ArrayList

class ChapterAdapter(private val goToItemActivity: (id: Int) -> Unit) :
    RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder>() {

    var listData = ArrayList<AzkarChapter>()
    lateinit var itemAction: ItemAction
    var frenchChapters = ArrayList<Chapter>()


    interface ItemAction {
        fun clickItemAction(chapterId: Int)
    }

    fun setItemClickAction(mItemAction: ItemAction) {
        itemAction = mItemAction
    }

    fun setData(data: List<AzkarChapter>) {
        if (data == null) return
        listData.clear()
        listData.addAll(data)
        notifyDataSetChanged()
    }

    fun setFrenchData(data: List<Chapter>) {
        if (data == null) return
        frenchChapters.clear()
        frenchChapters.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chapter, parent, false)
        return ChapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChapterViewHolder, position: Int) {

        if (Locale.getDefault().language.contentEquals("fr")) {
            val frenchItem = frenchChapters[position]
            holder.frenchBind( frenchItem)
            holder.itemView.cardChapter.setOnClickListener(View.OnClickListener {
                itemAction.clickItemAction(frenchItem.id)
            })
        } else {
            val item = listData[position]
            holder.bind(item)
            holder.itemView.cardChapter.setOnClickListener(View.OnClickListener {
                itemAction.clickItemAction(item.chapterId)
            })
        }




    }

    override fun getItemCount(): Int {

        if (Locale.getDefault().language.contentEquals("fr")) {
            return frenchChapters.size
        } else {
            return listData.size
        }

    }

    inner class ChapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: AzkarChapter) {
            itemView.txt_chapter.text = item.chapterName
        }

        fun frenchBind(frenchItem: Chapter) {
            itemView.txt_chapter.text = frenchItem.chapterName
        }


    }

}