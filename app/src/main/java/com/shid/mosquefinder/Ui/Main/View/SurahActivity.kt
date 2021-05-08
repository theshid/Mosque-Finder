package com.shid.mosquefinder.Ui.Main.View

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.irozon.sneaker.Sneaker
import com.shid.mosquefinder.ConnectivityStateHolder
import com.shid.mosquefinder.Data.database.entities.Surah
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Ui.Base.SearchViewModelFactory
import com.shid.mosquefinder.Ui.Base.SurahViewModelFactory
import com.shid.mosquefinder.Ui.Main.Adapter.SurahAdapter
import com.shid.mosquefinder.Ui.Main.ViewModel.SearchViewModel
import com.shid.mosquefinder.Ui.Main.ViewModel.SurahViewModel
import com.shid.mosquefinder.Utils.Common
import com.shid.mosquefinder.Utils.Network.Event
import com.shid.mosquefinder.Utils.Network.NetworkEvents
import com.shid.mosquefinder.Utils.setTransparentStatusBar
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.activity_surah.*
import kotlinx.android.synthetic.main.activity_surah.backButton
import kotlinx.android.synthetic.main.activity_surah.searchEdit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SurahActivity : AppCompatActivity(), SurahAdapter.OnClickSurah {

    private lateinit var viewModel: SurahViewModel
    private var previousSate = true
    private lateinit var surahAdapter: SurahAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_surah)
        setViewModel()
        setUI()
        setOnClick()
        setSearch()
        setTransparentStatusBar()
        setNetworkMonitor()
    }

    private fun setSearch() {
        searchEdit.doOnTextChanged { text, _, _, _ ->
            val search = text.toString()
            surahAdapter.filter.filter(search)
        }
    }

    private fun setViewModel() {
        viewModel = ViewModelProvider(
            this,
            SurahViewModelFactory(application)
        ).get(SurahViewModel::class.java)
    }

    private fun setUI() {
        surahAdapter = SurahAdapter()
        setRecycler()
    }

    private fun setRecycler() {
        viewModel.getSurahs()
        viewModel.surahList.observe(this, Observer {
            if (it.isNotEmpty()) {

                surahAdapter.setData(it)
                surahAdapter.setItemClick(this@SurahActivity)
                surahRecycler.adapter = surahAdapter
                surahAdapter.list = it as MutableList<Surah>
                surahAdapter.notifyDataSetChanged()
            }

        })


    }

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

    override fun onClickSurah(surah: Surah) {
        var intent = Intent(this, AyahActivity::class.java)
        intent.putExtra("surah_number", surah.id)
        startActivity(intent)
    }
}