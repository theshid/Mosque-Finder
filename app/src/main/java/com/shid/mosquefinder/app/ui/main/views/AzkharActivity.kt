package com.shid.mosquefinder.app.ui.main.views

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.ui.base.BaseActivity
import com.shid.mosquefinder.app.ui.main.adapters.AzkharAdapter
import com.shid.mosquefinder.app.ui.main.states.AzkharViewState
import com.shid.mosquefinder.app.ui.main.view_models.AzkharViewModel
import com.shid.mosquefinder.app.utils.extensions.showToast
import com.shid.mosquefinder.app.utils.helper_class.Constants.EXTRA_CHAPTER
import com.shid.mosquefinder.app.utils.helper_class.singleton.NetworkUtil
import com.shid.mosquefinder.app.utils.extensions.showSnackbar
import com.shid.mosquefinder.data.model.AzkarII
import dagger.hilt.android.AndroidEntryPoint
import dev.kosrat.muslimdata.models.Language
import dev.kosrat.muslimdata.repository.MuslimRepository
import kotlinx.android.synthetic.main.activity_item.*
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class AzkharActivity : BaseActivity() {
    private lateinit var adapter: AzkharAdapter
    private var chapterId: Int? = null
    private val viewModel by viewModels<AzkharViewModel>()
    private var list: ArrayList<AzkarII> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item)

        chapterId = intent.getIntExtra(EXTRA_CHAPTER, 1)
        adapter = AzkharAdapter() { azkarItem ->
            getFrenchTranslation(azkarItem)
        }
        setUI()
        observeAzkharViewState()
        setOnClick()
    }

    private fun getFrenchTranslation(item: AzkarII) {
        if (NetworkUtil.isOnline(this)) {
            viewModel.getTranslation(item.translation)
        } else {
            showToast(getString(R.string.network_error_message))
        }
    }

    override fun onPause() {
        super.onPause()
        AzkharAdapter.itemToSend = null
    }

    private fun observeAzkharViewState() {
        viewModel.azkharViewState.observe(this) { viewState ->
            if (AzkharAdapter.itemToSend != null) {
                val element = list.first { AzkharAdapter.itemToSend!!.itemId == it.itemId }
                Timber.d("ele:$element")
                val translationFr = viewState.translation?.translation?.get(0)?.textTranslation
                Timber.d("trans:$translationFr")
                val index = list.indexOf(element)
                Timber.d("index:$index")
                if (translationFr != null) {
                    list[index] = AzkarII(
                        AzkharAdapter.itemToSend!!.itemId,
                        AzkharAdapter.itemToSend!!.chapterId,
                        AzkharAdapter.itemToSend!!.item,
                        translationFr,
                        AzkharAdapter.itemToSend!!.reference
                    )
                    Timber.d("new ele:${list[index]}")

                }
                adapter.notifyDataSetChanged()
            }
            handleListError(viewState)
        }
    }

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
                for (item in azkarItems) {
                    list.add(
                        AzkarII(
                            item.itemId,
                            item.chapterId,
                            item.item,
                            item.translation,
                            item.reference
                        )
                    )
                }
                adapter.submitList(list)
            }
        }
    }
}