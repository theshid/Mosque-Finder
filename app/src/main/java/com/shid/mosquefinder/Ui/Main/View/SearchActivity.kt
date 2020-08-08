package com.shid.mosquefinder.Ui.Main.View

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.irozon.sneaker.Sneaker
import com.shid.mosquefinder.ConnectivityStateHolder
import com.shid.mosquefinder.Data.Model.ClusterMarker
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Ui.Base.SearchViewModelFactory
import com.shid.mosquefinder.Ui.Main.Adapter.SearchAdapter
import com.shid.mosquefinder.Ui.Main.ViewModel.MapViewModel
import com.shid.mosquefinder.Ui.Main.ViewModel.SearchViewModel
import com.shid.mosquefinder.Utils.Common
import com.shid.mosquefinder.Utils.Network.Event
import com.shid.mosquefinder.Utils.Network.NetworkEvents
import com.shid.mosquefinder.Utils.setTransparentStatusBar
import kotlinx.android.synthetic.main.activity_search.*
import java.util.Observer

class SearchActivity : AppCompatActivity(),SearchAdapter.OnClickSearch {

    companion object {
        const val SEARCH_RESULT = "search_result"
    }
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var searchViewmodel: SearchViewModel
    private var previousSate = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        savedInstanceState?.let {
            previousSate = it.getBoolean("LOST_CONNECTION")
        }

        setRecycler()
        setViewModel()
        setObservers()
        setTransparentStatusBar()
        setNetworkMonitor()
        setOnClick()

    }

    private fun setObservers() {
        searchAdapter.list = searchViewmodel.getClusterMarkers()
        searchAdapter.notifyDataSetChanged()
    }

    

    private fun setOnClick() {
        backButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setRecycler() {
        searchAdapter = SearchAdapter(this, this)
        searchAdapter.setOnClickSearch {
            val returnIntent = Intent()
            returnIntent.putExtra(SEARCH_RESULT, it)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }
        searchRecycler.adapter = searchAdapter
    }

    private fun setViewModel() {
        searchViewmodel = ViewModelProvider(
            this,
            SearchViewModelFactory(Common.googleApiService, application)
        ).get(SearchViewModel::class.java)
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
                .setTitle("Connected!!")
                .setMessage("The network is back !")
                .sneakSuccess()
        }

        if (!ConnectivityStateHolder.isConnected && previousSate) {
            // showSnackBar(textView, "No Network !")
            Sneaker.with(this) // Activity, Fragment or ViewGroup
                .setTitle("Connection lost")
                .setMessage("No Network!")
                .sneakError()
        }

        previousSate = ConnectivityStateHolder.isConnected
    }

    override fun onClickSearch(clusterMarker: ClusterMarker) {

    }
}