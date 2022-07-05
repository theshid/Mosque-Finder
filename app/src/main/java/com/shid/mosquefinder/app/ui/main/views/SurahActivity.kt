package com.shid.mosquefinder.app.ui.main.views

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.irozon.sneaker.Sneaker
import com.shid.mosquefinder.ConnectivityStateHolder
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.ui.main.adapters.SurahAdapter
import com.shid.mosquefinder.app.ui.main.states.SurahViewState
import com.shid.mosquefinder.app.ui.main.view_models.SurahViewModel
import com.shid.mosquefinder.app.ui.models.SurahPresentation
import com.shid.mosquefinder.app.utils.Network.Event
import com.shid.mosquefinder.app.utils.Network.NetworkEvents
import com.shid.mosquefinder.app.utils.hide
import com.shid.mosquefinder.app.utils.show
import com.shid.mosquefinder.app.utils.showSnackbar
import com.shid.mosquefinder.app.utils.startActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_surah.*

@AndroidEntryPoint
class SurahActivity : AppCompatActivity(), SurahAdapter.OnClickSurah {

    private val viewModel: SurahViewModel by viewModels()
    private var previousSate = true
    private lateinit var surahAdapter: SurahAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_surah)
        //setViewModel()
        hideSoftKeyboard()
        setUI()
        setOnClick()
        setSearch()
        observeSurahViewState()
        //setTransparentStatusBar()
        setNetworkMonitor()
    }

    private fun observeSurahViewState() {
        viewModel.surahViewState.observe(this) { state ->
            handleSurahLoading(state)
            state.surahs?.let { list ->
                if (list.isNotEmpty()) {
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

    /* private fun setViewModel() {
         viewModel = ViewModelProvider(
             this,
             SurahViewModelFactory(application)
         ).get(SurahViewModel::class.java)
     }*/

    private fun hideSoftKeyboard() {
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    private fun setUI() {
        surahAdapter = SurahAdapter()
        surahAdapter.setItemClick(this@SurahActivity)
        //setRecycler()
    }

    /*private fun setRecycler() {
        viewModel.getSurahs()
        viewModel.surahDbList.observe(this, Observer {
            if (it.isNotEmpty()) {

                surahAdapter.setData(it)
                surahAdapter.setItemClick(this@SurahActivity)
                surahRecycler.adapter = surahAdapter
                surahAdapter.list = it as MutableList<SurahDb>
                surahAdapter.notifyDataSetChanged()
            }

        })


    }*/

    private fun setOnClick() {
        backButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setNetworkMonitor() {
        NetworkEvents.observe(this, androidx.lifecycle.Observer {

            if (it is Event.ConnectivityEvent)
                handleConnectivityChange()
        })
    }

    private fun handleConnectivityChange() {
        if (ConnectivityStateHolder.isConnected && !previousSate) {
            // showSnackBar(textView, "The network is back !")
            Sneaker.with(this) // Activity, Fragment or ViewGroup
                .setTitle(getString(R.string.sneaker_connected))
                .setMessage(getString(R.string.sneaker_msg_network))
                .sneakSuccess()
        }

        if (!ConnectivityStateHolder.isConnected && previousSate) {
            // showSnackBar(textView, "No Network !")
            Sneaker.with(this) // Activity, Fragment or ViewGroup
                .setTitle(getString(R.string.sneaker_disconnected))
                .setMessage(getString(R.string.sneaker_msg_network_lost))
                .sneakError()
        }

        previousSate = ConnectivityStateHolder.isConnected
    }

    override fun onClickSurah(surahPresentation: SurahPresentation) {
        startActivity<AyahActivity> {
            putExtra("surah_number", surahPresentation.number)
        }

    }
}