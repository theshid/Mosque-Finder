package com.shid.mosquefinder.Ui.Main.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Ui.Main.ViewModel.AzkharViewModel
import dev.kosrat.muslimdata.models.AzkarItem
import kotlinx.android.synthetic.main.item_azkhar.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class ItemAdapter(val viewModel: AzkharViewModel, val mContext:Context) :
    RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {
    private var listData = ArrayList<AzkarItem>()

    fun setData(data: List<AzkarItem>) {
        if (data == null) return
        listData.clear()
        listData.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_azkhar, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = listData[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(azkarItem: AzkarItem) {
            if(Locale.getDefault().language.contentEquals("fr")){
                itemView.btn_translate.visibility = View.VISIBLE
            }
            itemView.tv_item_id.text = azkarItem.itemId.toString()
            itemView.tv_item_arab.text = azkarItem.item
            itemView.tv_item_translate.text = azkarItem.translation
            itemView.tv_item_source.text = azkarItem.reference
            itemView.btn_translate.setOnClickListener(View.OnClickListener {
                translateTest(azkarItem.translation)
            })
        }

        private fun translateTest(translation: String) {
            GlobalScope.launch(context = Dispatchers.Main) {
                viewModel.setTranslation(translation)
                delay(3000)
                itemView.tv_item_translate_french.text = viewModel.output.value?.textTranslation
                Log.d("Adapter","value of output"+viewModel.output.value?.textTranslation)
            }



            /*viewModel.output.observe(mContext as LifecycleOwner, Observer {
                itemView.tv_item_translate_french.text = it.textTranslation
            })*/

        }
    }

}