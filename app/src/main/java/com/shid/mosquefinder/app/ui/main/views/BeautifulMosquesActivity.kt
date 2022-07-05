package com.shid.mosquefinder.app.ui.main.views


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.irozon.sneaker.Sneaker
import com.shid.mosquefinder.ConnectivityStateHolder
import com.shid.mosquefinder.data.model.BeautifulMosques
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.ui.base.BeautifulMosquesViewModelFactory
import com.shid.mosquefinder.app.ui.main.adapters.MosqueAdapter
import com.shid.mosquefinder.app.ui.main.view_models.BeautifulMosquesViewModel
import com.shid.mosquefinder.app.utils.Network.Event
import com.shid.mosquefinder.app.utils.Network.NetworkEvents
import com.shid.mosquefinder.app.utils.Status
import com.skydoves.transformationlayout.onTransformationStartContainer
import kotlinx.android.synthetic.main.activity_beautiful_mosques.*
import kotlinx.android.synthetic.main.activity_beautiful_mosques.progressBar2
import kotlinx.android.synthetic.main.activity_beautiful_mosques.toolbar


class BeautifulMosquesActivity : AppCompatActivity() {
    private lateinit var mosqueAdapter: MosqueAdapter
    private lateinit var mosqueViewModel: BeautifulMosquesViewModel
    private var previousSate = true
    private lateinit var layoutManager: GridLayoutManager
    private var mMosqueList: MutableList<BeautifulMosques> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        onTransformationStartContainer()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beautiful_mosques)

        savedInstanceState?.let {
            previousSate = it.getBoolean("LOST_CONNECTION")
        }
        setViewModel()

        setOnClick()

        setNetworkMonitor()
        setObservers()
        Handler().postDelayed(kotlinx.coroutines.Runnable {
            //anything you want to start after 3s
            mMosqueList = mosqueViewModel.getMosquesFromRepository()
            progressBar2.visibility = View.GONE
            setRecycler()
            mosqueAdapter.notifyDataSetChanged()
            // addUserMarker()

        }, 2000)
    }

    private fun setOnClick() {
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setRecycler() {
        layoutManager = GridLayoutManager(this,2)
        mosque_recycler.layoutManager = layoutManager
        mMosqueList.shuffle()
        mosqueAdapter = MosqueAdapter(mMosqueList, this)
        mosque_recycler.adapter = mosqueAdapter
    }

    private fun setViewModel() {
        mosqueViewModel = ViewModelProvider(
            this,
            BeautifulMosquesViewModelFactory()
        ).get(BeautifulMosquesViewModel::class.java)


    }

    private fun setObservers() {

        mosqueViewModel.retrieveStatusMsg().observe(this, androidx.lifecycle.Observer{
            when (it.status) {
                Status.SUCCESS -> {
                    Toast.makeText(this, it.data, Toast.LENGTH_LONG).show()
                }
                Status.LOADING -> {

                }
                Status.ERROR -> {
                    //Handle Error

                    Toast.makeText(this, it.data, Toast.LENGTH_LONG).show()
                    Log.d("Search", it.message.toString())
                }
            }
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

    override fun onResume() {
        super.onResume()
        handleConnectivityChange()
    }
}