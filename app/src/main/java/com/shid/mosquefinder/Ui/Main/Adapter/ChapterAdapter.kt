package com.shid.mosquefinder.Ui.Main.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shid.mosquefinder.R
import dev.kosrat.muslimdata.models.AzkarChapter
import kotlinx.android.synthetic.main.item_chapter.view.*

class ChapterAdapter(private val goToItemActivity : (id: Int) -> Unit) :RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder>(){

    var listData = ArrayList<AzkarChapter>()
    lateinit var itemAction:ItemAction


     interface ItemAction{
        fun clickItemAction(chapterId:Int)
    }

    fun setItemClickAction(mItemAction: ItemAction){
        itemAction = mItemAction
    }

     fun setData(data:List<AzkarChapter>){
        if (data == null) return
        listData.clear()
        listData.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chapter,parent,false)
        return ChapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChapterViewHolder, position: Int) {
        val item = listData[position]
        holder.bind(item)
        holder.itemView.cardChapter.setOnClickListener(View.OnClickListener {
            itemAction.clickItemAction(item.chapterId)
        })
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    inner class ChapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
       fun bind(item:AzkarChapter){
          itemView.txt_chapter.text = item.chapterName

           /*itemView.rootView.setOnClickListener(View.OnClickListener {
               goToItemActivity(item.chapterId)
           })*/
      }
    }

}