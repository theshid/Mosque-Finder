package com.shid.mosquefinder.app.ui.main.views


import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.ui.base.BaseActivity
import com.shid.mosquefinder.app.ui.main.adapters.MosqueAdapter
import com.shid.mosquefinder.app.ui.main.states.BeautyMosqueViewState
import com.shid.mosquefinder.app.ui.main.view_models.BeautifulMosquesViewModel
import com.shid.mosquefinder.app.utils.hide
import com.shid.mosquefinder.app.utils.remove
import com.shid.mosquefinder.app.utils.show
import com.shid.mosquefinder.app.utils.showSnackbar
import com.skydoves.transformationlayout.onTransformationStartContainer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_beautiful_mosques.*

@AndroidEntryPoint
class BeautifulMosquesActivity : BaseActivity() {
    private lateinit var mosqueAdapter: MosqueAdapter
    private val mosqueViewModel: BeautifulMosquesViewModel by viewModels()
    private lateinit var layoutManager: GridLayoutManager
    override fun onCreate(savedInstanceState: Bundle?) {
        onTransformationStartContainer()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beautiful_mosques)
        setOnClick()
        setRecycler()
        Handler(Looper.getMainLooper()).postDelayed(kotlinx.coroutines.Runnable {
            mosqueViewModel.getBeautifulMosques()
            progressBar2.remove()
        }, 2000)
        setObservers()
    }

    private fun setOnClick() {
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setRecycler() {
        layoutManager = GridLayoutManager(this, 2)
        mosque_recycler.layoutManager = layoutManager
        mosqueAdapter = MosqueAdapter()
        mosque_recycler.adapter = mosqueAdapter
    }

    private fun setObservers() {

        mosqueViewModel.beautyMosqueViewState.observe(this) { state ->
            handleBeautyMosquesLoading(state)
            state.mosques?.let { list ->
                if (list.isNotEmpty()) {
                    mosqueAdapter.submitList(list.shuffled())
                }
                handleBeautyMosquesError(state)
            }

        }


    }

    private fun handleBeautyMosquesError(state: BeautyMosqueViewState) {
        state.error?.run {
            showSnackbar(mosque_recycler, getString(this.message), isError = true)
        }
    }

    private fun handleBeautyMosquesLoading(viewState: BeautyMosqueViewState) {
        if (viewState.isLoading) {
            progressBar2.show()
        } else {
            progressBar2.hide()
        }
    }

}