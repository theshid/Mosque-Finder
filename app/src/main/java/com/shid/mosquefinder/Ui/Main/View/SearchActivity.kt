package com.shid.mosquefinder.Ui.Main.View

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.irozon.sneaker.Sneaker
import com.shid.mosquefinder.ConnectivityStateHolder
import com.shid.mosquefinder.Data.Model.ClusterMarker
import com.shid.mosquefinder.Data.Model.Mosque
import com.shid.mosquefinder.Data.Model.Pojo.GoogleMosque
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Ui.Base.SearchViewModelFactory
import com.shid.mosquefinder.Ui.Main.Adapter.SearchAdapter
import com.shid.mosquefinder.Ui.Main.ViewModel.MapViewModel
import com.shid.mosquefinder.Ui.Main.ViewModel.SearchViewModel
import com.shid.mosquefinder.Utils.Common
import com.shid.mosquefinder.Utils.Network.Event
import com.shid.mosquefinder.Utils.Network.NetworkEvents
import com.shid.mosquefinder.Utils.Status
import com.shid.mosquefinder.Utils.setTransparentStatusBar
import kotlinx.android.synthetic.main.activity_search.*
import java.util.Observer

class SearchActivity : AppCompatActivity(),SearchAdapter.OnClickSearch {

    companion object {
        const val SEARCH_RESULT = "search_result"
        private const val TAG = "SearchActivity"
    }

    private lateinit var searchAdapter: SearchAdapter
    private lateinit var searchViewmodel: SearchViewModel
    private var previousSate = true
    private var mMosqueList: MutableList<Mosque> = ArrayList()
    private var mGoogleMosqueList: MutableList<GoogleMosque> = ArrayList()
    private var mClusterMarkerList: MutableList<ClusterMarker> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        savedInstanceState?.let {
            previousSate = it.getBoolean("LOST_CONNECTION")
        }
        setViewModel()
        setRecycler()

        setObservers()
        setTransparentStatusBar()
        setNetworkMonitor()
        setOnClick()
        Handler().postDelayed(kotlinx.coroutines.Runnable {
            //anything you want to start after 3s

            mGoogleMosqueList = searchViewmodel.getGoogleMosqueFromRepository()
            mMosqueList = searchViewmodel.getUsersMosqueFromRepository()
            getClusterMarkers()
            searchAdapter.list = mClusterMarkerList
            searchAdapter.notifyDataSetChanged()
            Log.d(TAG,mMosqueList.size.toString())

            // addUserMarker()

        }, 5000)
    }

    fun getClusterMarkers():MutableList<ClusterMarker>{
        var newClusterMarker: ClusterMarker? = null
        var newClusterMarker2: ClusterMarker? = null
        for (mosqueLocation in mMosqueList) {

            val title = mosqueLocation.name
            val snippet = ""
            val distanceFromUser = 0.0
            newClusterMarker =
                ClusterMarker(

                    mosqueLocation.position.latitude,
                    mosqueLocation.position.longitude
                    ,
                    title,
                    snippet,
                    "verified",
                    false,
                    distanceFromUser
                )

            mClusterMarkerList.add(newClusterMarker)
        }


        for (mosqueLocation in mGoogleMosqueList) {
            val mosqueLat: Double = mosqueLocation.latitude.toDouble()
            val mosqueLg: Double = mosqueLocation.longitude.toDouble()

            try {
                val snippet =""
                val title = mosqueLocation.placeName
                val distanceFromUser = 0.0

                newClusterMarker2 =
                    ClusterMarker(

                        mosqueLat,
                        mosqueLg,
                        title,
                        snippet,
                        "default",
                        true,
                        distanceFromUser
                    )
                mClusterMarkerList.add(newClusterMarker2)

            } catch (e: NullPointerException) {
                Log.e(
                    "Map",
                    "addMapMarkers: NullPointerException: " + e.message
                )
            }
        }
        Log.d("model",mClusterMarkerList.size.toString())
        Log.d("model",mMosqueList.size.toString())
        Log.d("model",mGoogleMosqueList.size.toString())

        return mClusterMarkerList
    }

    private fun setObservers() {

        searchViewmodel.retrieveStatusMsg().observe(this, androidx.lifecycle.Observer{
            when (it.status) {
                Status.SUCCESS -> {
                    Toast.makeText(this, it.data, Toast.LENGTH_LONG).show()
                }
                Status.LOADING -> {

                }
                Status.ERROR -> {
                    //Handle Error

                    Toast.makeText(this, it.data, Toast.LENGTH_LONG).show()
                    Log.d("Search", it.message)
                }
            }
        })


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