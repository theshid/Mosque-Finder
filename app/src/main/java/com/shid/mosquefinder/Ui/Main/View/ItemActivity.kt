package com.shid.mosquefinder.Ui.Main.View

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Ui.Base.AzkharViewModelFactory
import com.shid.mosquefinder.Ui.Main.Adapter.ItemAdapter
import com.shid.mosquefinder.Ui.Main.ViewModel.AzkharViewModel
import dev.kosrat.muslimdata.models.Language
import dev.kosrat.muslimdata.repository.MuslimRepository
import kotlinx.android.synthetic.main.activity_item.*
import kotlinx.coroutines.launch

class ItemActivity : AppCompatActivity() {
    private lateinit var adapter:ItemAdapter
    private var chapterId:Int ?= null
    private lateinit var viewModel:AzkharViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item)
        setViewModel()
        chapterId = intent.getIntExtra("chapter",1)
        adapter = ItemAdapter(viewModel,this)
        setUI()

    }

    private fun setViewModel() {
        viewModel = ViewModelProvider(this,AzkharViewModelFactory(application))
            .get(AzkharViewModel::class.java)
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