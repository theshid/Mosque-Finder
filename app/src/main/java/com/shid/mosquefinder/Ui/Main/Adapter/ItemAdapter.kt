package com.shid.mosquefinder.Ui.Main.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shid.mosquefinder.R
import dev.kosrat.muslimdata.models.AzkarItem
import kotlinx.android.synthetic.main.item_azkhar.view.*

class ItemAdapter :RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {
    private var listData = ArrayList<AzkarItem>()

    fun setData(data:List<AzkarItem>){
        if (data == null) return
        listData.clear()
        listData.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_azkhar,parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = listData[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    inner class ItemViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){

        fun bind(azkarItem: AzkarItem){
            itemView.tv_item_id.text = azkarItem.itemId.toString()
            itemView.tv_item_arab.text = azkarItem.item
            itemView.tv_item_translate.text = azkarItem.translation
            itemView.tv_item_source.text = azkarItem.reference
        }
    }

}