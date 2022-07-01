package com.shid.mosquefinder.ui.Main.View

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.irozon.sneaker.Sneaker
import com.shid.mosquefinder.ConnectivityStateHolder
import com.shid.mosquefinder.data.model.ClusterMarker
import com.shid.mosquefinder.R
import com.shid.mosquefinder.ui.Base.SearchViewModelFactory
import com.shid.mosquefinder.ui.Main.Adapter.SearchAdapter
import com.shid.mosquefinder.ui.Main.ViewModel.SearchViewModel
import com.shid.mosquefinder.utils.Common
import com.shid.mosquefinder.utils.Network.Event
import com.shid.mosquefinder.utils.Network.NetworkEvents
import com.shid.mosquefinder.utils.Status
import kotlinx.android.synthetic.main.activity_search.*
import java.math.BigDecimal
import java.math.RoundingMode

class SearchActivity : AppCompatActivity(), SearchAdapter.OnClickSearch {

    companion object {
        const val SEARCH_RESULT = "search_result"
    }
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var searchViewModel: SearchViewModel
    private var previousSate = true
    private var sortedMosqueList: List<ClusterMarker> = ArrayList()
    private var listFromApi = arrayListOf<ClusterMarker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        savedInstanceState?.let {
            previousSate = it.getBoolean("LOST_CONNECTION")
        }
        val bundle = intent.extras
        if (bundle != null) {
            listFromApi =
                bundle.getParcelableArrayList<ClusterMarker>("test") as ArrayList<ClusterMarker>
        }
        setViewModel()
        setRecycler()

        setObservers()

        setNetworkMonitor()
        setOnClick()
        Handler().postDelayed(kotlinx.coroutines.Runnable {

            progressBar.visibility = View.GONE
            sortClusterMarkerList()

            searchAdapter.list = sortedMosqueList as MutableList<ClusterMarker>
            searchAdapter.mosqueList = sortedMosqueList as MutableList<ClusterMarker>
            searchAdapter.notifyDataSetChanged()
            setSearch()

        }, 1000)
    }

    override fun onResume() {
        super.onResume()
        handleConnectivityChange()
    }

    private fun setSearch() {
        searchEdit.doOnTextChanged { text, _, _, _ ->
            val search = text.toString()
            searchAdapter.filter.filter(search)
        }
    }

    private fun setObservers() {

        searchViewModel.retrieveStatusMsg().observe(this, androidx.lifecycle.Observer {
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

    private fun sortClusterMarkerList() {
        sortedMosqueList = listFromApi.sortedWith(compareBy { it.distanceFromUser })
    }

    private fun calculateDistanceBetweenUserAndMosque(
        position: LatLng,
        userLocation: LatLng
    ): Double {
        val distanceInKm: Double =
            SphericalUtil.computeDistanceBetween(userLocation, position) * 0.001
        return BigDecimal(distanceInKm).setScale(2, RoundingMode.HALF_EVEN).toDouble()
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
        searchViewModel = ViewModelProvider(
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
            Sneaker.with(this) // Activity, Fragment or ViewGroup
                .setTitle(getString(R.string.sneaker_connected))
                .setMessage(getString(R.string.sneaker_msg_network))
                .sneakSuccess()
        }

        if (!ConnectivityStateHolder.isConnected && previousSate) {
            Sneaker.with(this) // Activity, Fragment or ViewGroup
                .setTitle(getString(R.string.sneaker_disconnected))
                .setMessage(getString(R.string.sneaker_msg_network_lost))
                .sneakError()
        }

        previousSate = ConnectivityStateHolder.isConnected
    }

    override fun onClickSearch(clusterMarker: ClusterMarker) {

    }


}