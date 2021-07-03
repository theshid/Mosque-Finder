package com.shid.mosquefinder.Ui.Main.View

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Ui.Base.NameViewModelFactory
import com.shid.mosquefinder.Ui.Main.Adapter.NameAdapter
import com.shid.mosquefinder.Ui.Main.ViewModel.NameViewModel
import dev.kosrat.muslimdata.models.Language
import dev.kosrat.muslimdata.repository.MuslimRepository
import kotlinx.android.synthetic.main.activity_beautiful_mosques.*
import kotlinx.android.synthetic.main.activity_names.*
import kotlinx.coroutines.launch

class NamesActivity : AppCompatActivity() {
    private lateinit var viewModel: NameViewModel
    private lateinit var nameAdapter: NameAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_names)

        setViewModel()
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


    private fun setViewModel() {
        viewModel = ViewModelProvider(
            this,
            NameViewModelFactory(application)
        ).get(NameViewModel::class.java)
    }
}