package com.shid.mosquefinder.Ui.Main.View

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Ui.Main.Adapter.ItemAdapter
import dev.kosrat.muslimdata.models.Language
import dev.kosrat.muslimdata.repository.MuslimRepository
import kotlinx.android.synthetic.main.activity_item.*
import kotlinx.coroutines.launch

class ItemActivity : AppCompatActivity() {
    private lateinit var adapter:ItemAdapter
    private var chapterId:Int ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item)
        chapterId = intent.getIntExtra("chapter",1)
        adapter = ItemAdapter()
        setUI()

    }

    private fun setUI() {
        rv_azkhar.adapter = adapter
        chapterId?.let { getItem(it) }
    }

    private fun getItem(chapterNum:Int) {
        lifecycleScope.launch {
            val repository = MuslimRepository(this@ItemActivity)
            val azkarItems = repository.getAzkarItems(chapterNum, Language.EN)
            if (azkarItems != null){
                adapter.setData(azkarItems)
            }
            Log.i("azkarItems", "$azkarItems")
            Log.i("azkarItems", "${azkarItems?.size}")
        }
    }
}