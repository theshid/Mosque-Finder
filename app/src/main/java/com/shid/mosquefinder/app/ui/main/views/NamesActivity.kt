package com.shid.mosquefinder.app.ui.main.views

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.ui.main.adapters.NameAdapter
import com.shid.mosquefinder.app.ui.main.view_models.NameViewModel
import dagger.hilt.android.AndroidEntryPoint
import dev.kosrat.muslimdata.models.Language
import dev.kosrat.muslimdata.repository.MuslimRepository
import kotlinx.android.synthetic.main.activity_names.*
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NamesActivity : AppCompatActivity() {
    private val viewModel: NameViewModel by viewModels()
    private lateinit var nameAdapter: NameAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_names)
        setUI()
        //setTransparentStatusBar()
    }

    private fun setUI() {
        nameAdapter = NameAdapter()
        lifecycleScope.launch {
            val repository = MuslimRepository(this@NamesActivity)
            val names = repository.getNamesOfAllah(Language.EN)
            nameAdapter.setData(names)
            nameRecycler.adapter = nameAdapter
        }
        val view: View = toolbar_detail.getChildAt(0)
        view.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })

    }

}