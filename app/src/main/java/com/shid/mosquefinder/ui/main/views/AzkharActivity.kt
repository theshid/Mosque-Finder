package com.shid.mosquefinder.ui.main.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.shid.mosquefinder.R
import com.shid.mosquefinder.ui.Base.AzkharViewModelFactory
import com.shid.mosquefinder.ui.main.adapters.ItemAdapter
import com.shid.mosquefinder.ui.main.view_models.AzkharViewModel
import dagger.hilt.android.AndroidEntryPoint
import dev.kosrat.muslimdata.models.Language
import dev.kosrat.muslimdata.repository.MuslimRepository
import kotlinx.android.synthetic.main.activity_item.*
import kotlinx.android.synthetic.main.activity_item.toolbar
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AzkharActivity : AppCompatActivity() {
    private lateinit var adapter:ItemAdapter
    private var chapterId:Int ?= null
    private val viewModel by viewModels<AzkharViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item)
        //setViewModel()

        chapterId = intent.getIntExtra("chapter",1)
        adapter = ItemAdapter(viewModel,this)
        setUI()
        setOnClick()

    }

    /*private fun setViewModel() {
        viewModel = ViewModelProvider(this,AzkharViewModelFactory(application))
            .get(AzkharViewModel::class.java)
    }*/

    private fun setUI() {
        rv_azkhar.adapter = adapter
        chapterId?.let { getItem(it) }
    }

    private fun setOnClick() {
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun getItem(chapterNum:Int) {
        lifecycleScope.launch {
            val repository = MuslimRepository(this@AzkharActivity)
            val azkarItems = repository.getAzkarItems(chapterNum, Language.EN)
            if (azkarItems != null){
                adapter.setData(azkarItems)
            }
            /*Log.i("azkarItems", "$azkarItems")
            Log.i("azkarItems", "${azkarItems?.size}")*/
        }
    }
}