package com.shid.mosquefinder.Ui.Main.View

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.irozon.sneaker.Sneaker
import com.shid.mosquefinder.ConnectivityStateHolder
import com.shid.mosquefinder.Data.Model.BeautifulMosques
import com.shid.mosquefinder.Data.Model.ClusterMarker
import com.shid.mosquefinder.Data.Model.Mosque
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Ui.Base.BeautifulMosquesViewModelFactory
import com.shid.mosquefinder.Ui.Base.SearchViewModelFactory
import com.shid.mosquefinder.Ui.Main.Adapter.MosqueAdapter
import com.shid.mosquefinder.Ui.Main.Adapter.SearchAdapter
import com.shid.mosquefinder.Ui.Main.ViewModel.BeautifulMosquesViewModel
import com.shid.mosquefinder.Ui.Main.ViewModel.SearchViewModel
import com.shid.mosquefinder.Utils.Common
import com.shid.mosquefinder.Utils.Network.Event
import com.shid.mosquefinder.Utils.Network.NetworkEvents
import com.shid.mosquefinder.Utils.getCountryCode
import com.shid.mosquefinder.Utils.setTransparentStatusBar
import com.skydoves.transformationlayout.TransformationCompat.startActivity
import kotlinx.android.synthetic.main.activity_beautiful_mosques.*
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.item_mosque.*

class BeautifulMosquesActivity : AppCompatActivity() {
    private lateinit var mosqueAdapter: MosqueAdapter
    private lateinit var mosqueViewModel: BeautifulMosquesViewModel
    private var previousSate = true
    private var onClickedTime = System.currentTimeMillis()
    private var mMosqueList: MutableList<BeautifulMosques> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beautiful_mosques)

        savedInstanceState?.let {
            previousSate = it.getBoolean("LOST_CONNECTION")
        }
        setViewModel()
        setRecycler()
        setTransparentStatusBar()
        setNetworkMonitor()
        Handler().postDelayed(kotlinx.coroutines.Runnable {
            //anything you want to start after 3s
            mMosqueList = mosqueViewModel.getMosquesFromRepository()
            mosqueAdapter.notifyDataSetChanged()
            // addUserMarker()

        }, 2000)
    }

    private fun setOnClick() {
        backButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setRecycler() {
        mosqueAdapter = MosqueAdapter(mMosqueList, this)
        mosqueAdapter.setOnClickMosque {
            val currentTime = System.currentTimeMillis()
            if (currentTime - onClickedTime > transformationLayout.duration) {
                onClickedTime = currentTime
                DetailActivity.startActivity(transformationLayout, item)
            }
            val returnIntent = Intent()
            returnIntent.putExtra(SearchActivity.SEARCH_RESULT, it)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }
        mosque_recycler.adapter = mosqueAdapter
    }

    private fun setViewModel() {
        mosqueViewModel = ViewModelProvider(
            this,
            BeautifulMosquesViewModelFactory()
        ).get(BeautifulMosquesViewModel::class.java)


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

    override fun onResume() {
        super.onResume()
        handleConnectivityChange()
    }
}