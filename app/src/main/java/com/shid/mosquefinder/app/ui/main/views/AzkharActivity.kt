package com.shid.mosquefinder.app.ui.main.views

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.ui.main.adapters.AzkharAdapter
import com.shid.mosquefinder.app.ui.main.states.AzkharViewState
import com.shid.mosquefinder.app.ui.main.view_models.AzkharViewModel
import com.shid.mosquefinder.app.utils.showSnackbar
import dagger.hilt.android.AndroidEntryPoint
import dev.kosrat.muslimdata.models.Language
import dev.kosrat.muslimdata.repository.MuslimRepository
import kotlinx.android.synthetic.main.activity_item.*
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AzkharActivity : AppCompatActivity() {
    private lateinit var adapter: AzkharAdapter
    private var chapterId: Int? = null
    private val viewModel by viewModels<AzkharViewModel>()

    companion object {
        var translation: MutableLiveData<String>? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item)
        //setViewModel()
        translation?.value = ""
        chapterId = intent.getIntExtra("chapter", 1)
        adapter = AzkharAdapter() { azkarItem ->
            viewModel.getTranslation(azkarItem.translation)
        }
        setUI()
        observeAzkharViewState()
        setOnClick()

    }

    private fun observeAzkharViewState() {
        viewModel.azkharViewState.observe(this) {
            translation?.value = it.translation?.translation?.get(0)?.textTranslation
            handleListError(it)
        }
    }

    /*private fun setViewModel() {
        viewModel = ViewModelProvider(this,AzkharViewModelFactory(application))
            .get(AzkharViewModel::class.java)
    }*/

    private fun handleListError(communityInfoListViewState: AzkharViewState) {
        communityInfoListViewState.error?.run {
            showSnackbar(
                rv_azkhar,
                getString(this.message),
                isError = true
            )
        }
    }

    private fun setUI() {
        rv_azkhar.adapter = adapter
        chapterId?.let { getItem(it) }
    }

    private fun setOnClick() {
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun getItem(chapterNum: Int) {
        lifecycleScope.launch {
            val repository = MuslimRepository(this@AzkharActivity)
            val azkarItems = repository.getAzkarItems(chapterNum, Language.EN)
            if (azkarItems != null) {
                adapter.submitList(azkarItems)
                // adapter.setData(azkarItems)
            }
            /*Log.i("azkarItems", "$azkarItems")
            Log.i("azkarItems", "${azkarItems?.size}")*/
        }
    }
}