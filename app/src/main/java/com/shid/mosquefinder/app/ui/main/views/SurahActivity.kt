package com.shid.mosquefinder.app.ui.main.views

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.core.widget.doOnTextChanged
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.ui.base.BaseActivity
import com.shid.mosquefinder.app.ui.main.adapters.SurahAdapter
import com.shid.mosquefinder.app.ui.main.states.SurahViewState
import com.shid.mosquefinder.app.ui.main.view_models.SurahViewModel
import com.shid.mosquefinder.app.ui.models.SurahPresentation
import com.shid.mosquefinder.app.utils.extensions.startActivity
import com.shid.mosquefinder.app.utils.helper_class.Constants
import com.shid.mosquefinder.app.utils.extensions.hide
import com.shid.mosquefinder.app.utils.extensions.show
import com.shid.mosquefinder.app.utils.extensions.showSnackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_surah.*
import timber.log.Timber

@AndroidEntryPoint
class SurahActivity : BaseActivity(), SurahAdapter.OnClickSurah {

    private val viewModel: SurahViewModel by viewModels()
    private lateinit var surahAdapter: SurahAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_surah)
        hideSoftKeyboard()
        setUI()
        setOnClick()
        setSearch()
        observeSurahViewState()
        //setTransparentStatusBar()
    }

    private fun observeSurahViewState() {
        viewModel.getSurahs()
        viewModel.surahViewState.observe(this) { state ->
            handleSurahLoading(state)
            state.surahs?.let { list ->
                if (list.isNotEmpty()) {
                    Timber.d("list:$list")
                    surahAdapter.submitList(list)
                }
            }
            handleSurahError(state)
        }
    }

    private fun handleSurahError(state: SurahViewState) {
        state.error?.run {
            showSnackbar(
                surahRecycler, getString(this.message), isError = true
            )
        }
    }

    private fun handleSurahLoading(surahViewState: SurahViewState) {
        if (surahViewState.isLoading) {
            progressBar3.show()
        } else {
            progressBar3.hide()
        }
    }

    private fun setSearch() {
        searchEdit.doOnTextChanged { text, _, _, _ ->
            val search = text.toString()
            surahAdapter.filter.filter(search)
        }
    }

    private fun hideSoftKeyboard() {
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    private fun setUI() {
        surahAdapter = SurahAdapter()
        surahRecycler.adapter = surahAdapter
        surahAdapter.setItemClick(this@SurahActivity)
    }

    private fun setOnClick() {
        backButton.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onClickSurah(surahPresentation: SurahPresentation) {
        startActivity<AyahActivity> {
            putExtra(Constants.EXTRA_SURAH_NUMBER, surahPresentation.number)
        }
    }
}