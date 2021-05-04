package com.shid.mosquefinder.Ui.Main.View

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.irozon.sneaker.Sneaker
import com.shid.mosquefinder.ConnectivityStateHolder
import com.shid.mosquefinder.Data.database.entities.Ayah
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Ui.Base.AyahViewModelFactory
import com.shid.mosquefinder.Ui.Base.SurahViewModelFactory
import com.shid.mosquefinder.Ui.Main.Adapter.AyahAdapter
import com.shid.mosquefinder.Ui.Main.ViewModel.AyahViewModel
import com.shid.mosquefinder.Ui.Main.ViewModel.SurahViewModel
import com.shid.mosquefinder.Utils.Network.Event
import com.shid.mosquefinder.Utils.Network.NetworkEvents
import com.shid.mosquefinder.Utils.setTransparentStatusBar
import kotlinx.android.synthetic.main.activity_ayah.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AyahActivity : AppCompatActivity() {
    private lateinit var viewModel: AyahViewModel
    private var ayahList: List<Ayah>? = null
    private var previousSate = true
    private lateinit var ayahAdapter: AyahAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ayah)
        savedInstanceState?.let {
            previousSate = it.getBoolean("LOST_CONNECTION")
        }
        val surahNumber = intent.getIntExtra("surah_number",1)
        setViewModel()
        setUI(surahNumber)

        setTransparentStatusBar()
        setNetworkMonitor()
    }

    private fun setViewModel() {
        viewModel = ViewModelProvider(
            this,
            AyahViewModelFactory(application)
        ).get(AyahViewModel::class.java)
    }

    private fun setUI(number_surah:Int) {
        viewModel.getAllAyah(number_surah)
        viewModel.getSurahInfo(number_surah)
        viewModel.ayah.observe(this, Observer {
            if (it.isNotEmpty()){
                ayahAdapter = AyahAdapter(it)
                ayahRecycler.adapter = ayahAdapter
                ayahAdapter.notifyDataSetChanged()
            }

        })
        viewModel.surah.observe(this, Observer {
            surah_title.text = it.transliteration
            verse_number.text = it.totalVerses.toString() + " " + "Ayah"
        })


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
}