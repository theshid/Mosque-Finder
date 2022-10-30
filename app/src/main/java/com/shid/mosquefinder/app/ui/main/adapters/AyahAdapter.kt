package com.shid.mosquefinder.app.ui.main.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.ui.models.AyahPresentation
import com.shid.mosquefinder.app.utils.helper_class.Constants
import kotlinx.android.synthetic.main.item_quran_ayah.view.*
import java.util.*

class AyahAdapter :
    ListAdapter<AyahPresentation, AyahAdapter.AyahViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<AyahPresentation>() {
            override fun areItemsTheSame(
                oldItem: AyahPresentation,
                newItem: AyahPresentation
            ): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: AyahPresentation,
                newItem: AyahPresentation
            ): Boolean =
                oldItem == newItem
        }
    }

    lateinit var onClickAyah: OnClickAyah

    interface OnClickAyah {
        fun onClickAyah(ayah: AyahPresentation)
    }

    fun setOnItemClick(mOnClickAyah: OnClickAyah) {
        onClickAyah = mOnClickAyah
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AyahViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_quran_ayah, parent, false)
        return AyahViewHolder(view)
    }

    override fun onBindViewHolder(holder: AyahViewHolder, position: Int) {
        holder.bind(getItem(position))
        holder.itemView.setOnClickListener {
            getItem(position).let { onClickAyah.onClickAyah(it) }
        }
    }


    inner class AyahViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        fun bind(ayah: AyahPresentation) {
            itemView.apply {
                tv_item_ayah_verse.text = ayah.verse_number.toString()
                tv_item_ayah_arab.text = ayah.originalText
            }
            if (Locale.getDefault().language.contentEquals(Constants.FRENCH_VERSION)) {

                itemView.tv_item_ayah_translate.text = ayah.frenchTranslation

            } else {
                itemView.apply {
                    tv_item_ayah_translate.text = ayah.translation
                }
            }


        }

    }


}