package com.shid.mosquefinder.Ui.Main.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.azan.AzanTimes
import com.azan.Time
import com.shid.mosquefinder.R
import kotlinx.android.synthetic.main.item_prayer_time.view.*


class PrayerAdapter : RecyclerView.Adapter<PrayerAdapter.PrayerViewHolder>() {
    private var listData: AzanTimes? = null


    fun setData(data: AzanTimes) {
        listData = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrayerViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_prayer_time, parent, false)
        return PrayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PrayerViewHolder, position: Int) {
        if (listData != null) {
            holder.bind(listData!!.times)
        }

    }

    override fun getItemCount(): Int {
        return 1
    }

    inner class PrayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        fun bind(time: Array<Time>) {
            itemView.tv_pray_time_fajr.text = time[0].toString()
        }
    }
}