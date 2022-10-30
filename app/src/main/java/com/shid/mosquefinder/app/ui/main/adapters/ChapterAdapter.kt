package com.shid.mosquefinder.app.ui.main.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shid.mosquefinder.R
import com.shid.mosquefinder.data.local.database.entities.Chapter
import dev.kosrat.muslimdata.models.AzkarChapter
import kotlinx.android.synthetic.main.item_chapter.view.*

class ChapterAdapter(private val goToItemActivity: (id: Int) -> Unit) :
    ListAdapter<AzkarChapter, ChapterAdapter.ChapterViewHolder>(DIFF_CALLBACK) {


    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<AzkarChapter>() {
            override fun areItemsTheSame(oldItem: AzkarChapter, newItem: AzkarChapter): Boolean =
                oldItem.chapterId == newItem.chapterId

            override fun areContentsTheSame(oldItem: AzkarChapter, newItem: AzkarChapter): Boolean =
                oldItem == newItem
        }
    }

    var listData = ArrayList<AzkarChapter>()
    lateinit var itemAction: ItemAction
    var frenchChapters = ArrayList<Chapter>()


    interface ItemAction {
        fun clickItemAction(chapterId: Int)
    }

    fun setItemClickAction(mItemAction: ItemAction) {
        itemAction = mItemAction
    }

    /*fun setData(data: List<AzkarChapter>) {
        if (data == null) return
        listData.clear()
        listData.addAll(data)
        notifyDataSetChanged()
    }*/

    /*fun setFrenchData(data: List<Chapter>) {
        if (data == null) return
        frenchChapters.clear()
        frenchChapters.addAll(data)
        notifyDataSetChanged()
    }*/

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chapter, parent, false)
        return ChapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChapterViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.cardChapter.setOnClickListener(View.OnClickListener {
            itemAction.clickItemAction(item.chapterId)
        })
        /* if (Locale.getDefault().language.contentEquals("fr")) {
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
         }*/
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