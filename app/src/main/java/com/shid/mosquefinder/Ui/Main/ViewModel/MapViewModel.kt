package com.shid.mosquefinder.Ui.Main.ViewModel

import android.annotation.SuppressLint
import android.app.Application
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterManager
import com.shid.mosquefinder.App
import com.shid.mosquefinder.Data.Model.ClusterMarker
import com.shid.mosquefinder.Data.Model.Mosque
import com.shid.mosquefinder.Data.Repository.MapRepository
import com.shid.mosquefinder.Utils.Common.TAG

class MapViewModel(var mapRepository: MapRepository) : ViewModel() {
    private var mMosqueList :MutableList<Mosque> = ArrayList()
    private lateinit var userPosition: LatLng
    private val mApp: App = App()


    init {
        setUpLocationListener()
    }

     fun getUsersMosqueFromRepository() {
     mMosqueList = mapRepository.getTotalMosquesFromFirebase()
    }

    fun getMosquePositionFromApi(map:GoogleMap):Pair<MutableList<ClusterMarker>,ClusterManager<ClusterMarker>?>{
        return mapRepository.googlePlaceNearbyMosques("mosque",userPosition,map)

    }

    @SuppressLint("MissingPermission")
    fun setUpLocationListener() {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mApp.applicationContext)
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