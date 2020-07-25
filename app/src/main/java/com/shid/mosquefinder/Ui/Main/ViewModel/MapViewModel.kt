package com.shid.mosquefinder.Ui.Main.ViewModel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.clustering.ClusterManager
import com.shid.mosquefinder.App
import com.shid.mosquefinder.Data.Model.Api.ApiInterface
import com.shid.mosquefinder.Data.Model.ClusterMarker
import com.shid.mosquefinder.Data.Model.Mosque
import com.shid.mosquefinder.Data.Model.Pojo.Place
import com.shid.mosquefinder.Data.Repository.MapRepository
import com.shid.mosquefinder.MapsActivity2
import com.shid.mosquefinder.Ui.Base.MapViewModelFactory
import com.shid.mosquefinder.Utils.Common.TAG
import java.util.HashMap

class MapViewModel( mapRepository: MapRepository, application: Application) :ViewModel() {
    private var mMosqueList: MutableList<Mosque> = ArrayList()
    private lateinit var userPosition: LatLng
    private val mApplication = application
    private val mRepository = mapRepository
    val position:LatLng = LatLng(5.6363262,-0.2349102)
    private  var getPlace: MutableLiveData<Place>? = null


    init {
        setUpLocationListener()
        mMosqueList = mRepository.getTotalMosquesFromFirebase()


       // getPlace = mRepository.googlePlaceNearbyMosques("mosque", MapsActivity2.position)



    }

    fun getUserPosition(): LatLng? {
        return userPosition
    }

    fun inputMosqueInDatabase(userInput: HashMap<String, Comparable<*>>) {
        mRepository.inputMosqueInDatabase(userInput)
    }

    fun getUsersMosqueFromRepository(): MutableList<Mosque> {

        return mMosqueList
    }

    fun getGoogleMapMosqueFromRepository(): MutableLiveData<Place>? {
        getPlace = mRepository.googlePlaceNearbyMosques("mosque", MapsActivity2.userPosition)
        return getPlace

    }

    fun confirmMosqueLocation(marker: Marker) {
        mRepository.confirmMosqueLocation(marker)
    }

    fun reportFalseMosqueLocation(marker: Marker) {
        mRepository.reportFalseLocation(marker)
    }

    @SuppressLint("MissingPermission")
    fun setUpLocationListener() {
        val fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(mApplication)
        // for getting the current location update after every 2 seconds with high accuracy
        val locationRequest = LocationRequest().setInterval(2000).setFastestInterval(2000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    for (location in locationResult.locations) {
                        /* latTextView.text = location.latitude.toString()
                         lngTextView.text = location.longitude.toString()*/
                        userPosition = LatLng(location.latitude, location.longitude)
                        Log.d(TAG, "position=" + location.latitude + "" + location.longitude)
                    }
                    // Few more things we can do here:
                    // For example: Update the location of user on server
                }
            },
            Looper.myLooper()
        )
    }
}