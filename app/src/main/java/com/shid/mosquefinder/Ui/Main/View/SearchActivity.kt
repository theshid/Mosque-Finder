package com.shid.mosquefinder.Ui.Main.View

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
import com.shid.mosquefinder.Data.Model.ClusterMarker
import com.shid.mosquefinder.Data.Model.Mosque
import com.shid.mosquefinder.Data.Model.Pojo.GoogleMosque
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Ui.Base.SearchViewModelFactory
import com.shid.mosquefinder.Ui.Main.Adapter.SearchAdapter
import com.shid.mosquefinder.Ui.Main.ViewModel.SearchViewModel
import com.shid.mosquefinder.Utils.Common
import com.shid.mosquefinder.Utils.Network.Event
import com.shid.mosquefinder.Utils.Network.NetworkEvents
import com.shid.mosquefinder.Utils.Status
import com.shid.mosquefinder.Utils.getCountryCode
import com.shid.mosquefinder.Utils.setTransparentStatusBar
import kotlinx.android.synthetic.main.activity_search.*
import java.math.BigDecimal
import java.math.RoundingMode

class SearchActivity : AppCompatActivity(),SearchAdapter.OnClickSearch {

    companion object {
        const val SEARCH_RESULT = "search_result"
        private const val TAG = "SearchActivity"
    }

    private lateinit var searchAdapter: SearchAdapter
    private lateinit var searchViewModel: SearchViewModel
    private var previousSate = true
    private var mMosqueList: MutableList<Mosque> = ArrayList()
    private var mGoogleMosqueList: MutableList<GoogleMosque> = ArrayList()
    private var mNigerGoogleMosqueList: MutableList<GoogleMosque> = ArrayList()
    private var mClusterMarkerList: MutableList<ClusterMarker> = ArrayList()
    private var userPosition = SplashActivity.userPosition
    private var sortedMosqueList :List<ClusterMarker> = ArrayList()
    private var listFromApi =  arrayListOf<ClusterMarker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        savedInstanceState?.let {
            previousSate = it.getBoolean("LOST_CONNECTION")
        }
        val bundle = intent.extras
        if (bundle != null) {
            listFromApi = bundle.getParcelableArrayList<ClusterMarker>("test") as ArrayList<ClusterMarker>
        }
        setViewModel()
        setRecycler()

        setObservers()
        setTransparentStatusBar()
        setNetworkMonitor()
        setOnClick()
        Handler().postDelayed(kotlinx.coroutines.Runnable {
            //anything you want to start after 3s
            progressBar.visibility = View.GONE
            //mMosqueList = searchViewModel.getUsersMosqueFromRepository()
            /*if (getCountryCode(applicationContext) == "gh"){
                mGoogleMosqueList = searchViewModel.getGoogleMosqueFromRepository()
                getClusterMarkers(mGoogleMosqueList)
                sortClusterMarkerList()
            } else if (getCountryCode(applicationContext) == "ne"){
                mNigerGoogleMosqueList = searchViewModel.getNigerGoogleMosqueFromRepository()
                getClusterMarkers(mNigerGoogleMosqueList)
                sortClusterMarkerList()
            }*/
            sortClusterMarkerList()

            searchAdapter.list = sortedMosqueList as MutableList<ClusterMarker>
            searchAdapter.mosqueList = sortedMosqueList as MutableList<ClusterMarker>
            searchAdapter.notifyDataSetChanged()
            setSearch()
            Log.d(TAG,mMosqueList.size.toString())

            // addUserMarker()

        }, 2000)
    }

    override fun onResume() {
        super.onResume()
        handleConnectivityChange()
    }

    private fun setSearch() {
        searchEdit.doOnTextChanged { text, _, _, _ ->
            val search = text.toString()
            searchAdapter.filter.filter(search)
           /* if (search.isBlank()) {
                //viewModel.getContries()
            } else {
               // viewModel.search(search)

            }*/
        }
    }

    private fun setObservers() {

        searchViewModel.retrieveStatusMsg().observe(this, androidx.lifecycle.Observer{
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

    private fun sortClusterMarkerList() {
       // sortedMosqueList = mClusterMarkerList.sortedWith(compareBy { it.distanceFromUser })
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

    override fun onClickSearch(clusterMarker: ClusterMarker) {

    }

    private fun getClusterMarkers(googleList:MutableList<GoogleMosque>):MutableList<ClusterMarker>{
        var newClusterMarker: ClusterMarker? = null
        var newClusterMarker2: ClusterMarker? = null
        for (mosqueLocation in mMosqueList) {

            val title = mosqueLocation.name
            val snippet = ""
            var distanceFromUser = 0.0
            if (userPosition!=null) {
                distanceFromUser = calculateDistanceBetweenUserAndMosque(
                    LatLng(mosqueLocation.position.latitude,mosqueLocation.position.longitude), userPosition!!
                )
            }
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


        for (mosqueLocation in googleList) {
            val mosqueLat: Double = mosqueLocation.latitude.toDouble()
            val mosqueLg: Double = mosqueLocation.longitude.toDouble()

            try {
                val snippet =""
                val title = mosqueLocation.placeName
                var distanceFromUser = 0.0
                if (userPosition !=null) {
                    distanceFromUser = calculateDistanceBetweenUserAndMosque(
                        LatLng(mosqueLat, mosqueLg),
                        userPosition!!
                    )
                }



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
}