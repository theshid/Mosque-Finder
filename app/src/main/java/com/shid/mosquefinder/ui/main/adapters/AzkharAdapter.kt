package com.shid.mosquefinder.ui.main.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shid.mosquefinder.R
import com.shid.mosquefinder.ui.main.view_models.AzkharViewModel
import com.shid.mosquefinder.ui.main.views.AzkharActivity
import dev.kosrat.muslimdata.models.AzkarItem
import kotlinx.android.synthetic.main.item_azkhar.view.*
import java.util.*

internal class AzkharAdapter(val listener: (AzkarItem) -> Unit) :
    ListAdapter<AzkarItem, AzkharAdapter.ItemViewHolder>(DIFF_CALLBACK) {


    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<AzkarItem>() {
            override fun areItemsTheSame(oldItem: AzkarItem, newItem: AzkarItem): Boolean =
                oldItem.itemId == newItem.itemId

            override fun areContentsTheSame(oldItem: AzkarItem, newItem: AzkarItem): Boolean =
                oldItem == newItem
        }
    }
    //private var listData = ArrayList<AzkarItem>()

    /*fun setData(data: List<AzkarItem>) {
        if (data == null) return
        listData.clear()
        listData.addAll(data)
        notifyDataSetChanged()
    }*/

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_azkhar, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        //val item = listData[position]
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.btn_translate.setOnClickListener { listener(item) }
    }


    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(azkarItem: AzkarItem) {
            if (Locale.getDefault().language.contentEquals("fr")) {
                itemView.btn_translate.visibility = View.VISIBLE
            }
            itemView.tv_item_id.text = azkarItem.itemId.toString()
            itemView.tv_item_arab.text = azkarItem.item
            itemView.tv_item_translate.text = azkarItem.translation
            itemView.tv_item_source.text = azkarItem.reference
            if (AzkharActivity.translation?.value?.isNotEmpty() == true) {
                itemView.tv_item_translate_french.text = AzkharActivity.translation!!.value
                AzkharActivity.translation!!.setValue("")
            }
            /*itemView.btn_translate.setOnClickListener(View.OnClickListener {
                viewModel.getTranslation(azkarItem.translation)
                translateTest(azkarItem.translation)
            })*/
        }

        /*private fun translateTest(translation: String) {
            GlobalScope.launch(context = Dispatchers.Main) {
                viewModel.setTranslation(translation)

                delay(3000)
                itemView.tv_item_translate_french.text = viewModel.output.value?.textTranslation
                Log.d("Adapter","value of output"+viewModel.output.value?.textTranslation)
            }



            *//*viewModel.output.observe(mContext as LifecycleOwner, Observer {
                itemView.tv_item_translate_french.text = it.textTranslation
            })*//*

        }*/
    }

}