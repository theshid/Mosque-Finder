package com.shid.mosquefinder.Ui.Main.ViewModel

import android.app.Application
import android.location.Location
import android.os.Handler
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.shid.mosquefinder.Data.Model.Mosque
import com.shid.mosquefinder.Data.Model.Pojo.GoogleMosque
import com.shid.mosquefinder.Data.Model.Pojo.Place
import com.shid.mosquefinder.Data.Model.User
import com.shid.mosquefinder.Data.Repository.MapRepository
import com.shid.mosquefinder.MainActivity
import com.shid.mosquefinder.Ui.Main.View.MapsActivity2
import com.shid.mosquefinder.Ui.Main.View.SplashActivity
import com.shid.mosquefinder.Utils.Resource
import fr.quentinklein.slt.LocationTracker
import fr.quentinklein.slt.ProviderError
import java.util.HashMap

class MapViewModel(mapRepository: MapRepository, var application: Application) : ViewModel() {
    private var mMosqueList: MutableList<Mosque> = ArrayList()
    private var mGoogleMosqueList: MutableList<GoogleMosque> = ArrayList()
    private var mNigerGoogleMosqueList: MutableList<GoogleMosque> = ArrayList()
    private val mRepository = mapRepository
    val position: LatLng = LatLng(5.6363262, -0.2349102)
    private var getPlace: MutableLiveData<Place>? = null
    val locationTracker = LocationTracker()
    val newUserPosition: LatLng? = null
    val testPlace: MutableLiveData<Place>? = null


    init {
        //setUpNewLocationListener()
        mMosqueList = mRepository.getTotalMosquesFromFirebase()
        Log.d("MapModel", mMosqueList.size.toString())
        mGoogleMosqueList = mRepository.getGoogleMosqueFromFirebase()
        mNigerGoogleMosqueList = mRepository.getNigerGoogleMosqueFromFirebase()
        retrieveStatusMsg()


        /*when {
            SplashActivity.userPosition != null -> {
                getPlace = mRepository.googlePlaceNearbyMosques("mosque", SplashActivity.userPosition!!)
            }
            MapsActivity2.userPosition != null -> {
                getPlace = mRepository.googlePlaceNearbyMosques("mosque", MapsActivity2.userPosition!!)
            }
            MapsActivity2.newUserPosition != null -> {
                getPlace = mRepository.googlePlaceNearbyMosques("mosque", MapsActivity2.newUserPosition!!)
            }
        }*/

    }

    fun retrieveStatusMsg(): LiveData<Resource<String>> {
        return mRepository.returnStatusMsg()
    }

    fun inputMosqueInDatabase(userInput: HashMap<String, Comparable<*>>) {
        mRepository.inputMosqueInDatabase(userInput)
    }

    fun getGoogleMosqueFromRepository(): MutableList<GoogleMosque> {
        return mGoogleMosqueList
    }

    fun getNigerGoogleMosqueFromRepository(): MutableList<GoogleMosque> {
        return mNigerGoogleMosqueList
    }

    fun getUsersMosqueFromRepository(): MutableList<Mosque> {
        Log.d("MapModel", mMosqueList.size.toString())
        return mMosqueList
    }

    fun getGoogleMapMosqueFromRepository(userLocation: LatLng): MutableLiveData<Place>? {
        getPlace =  mRepository.googlePlaceNearbyMosques("mosque", userLocation)

        return getPlace

    }

    fun confirmMosqueLocation(marker: Marker, user: User) {
        mRepository.confirmMosqueLocation(marker, user)
    }

    fun reportFalseMosqueLocation(marker: Marker, user: User) {
        mRepository.reportFalseLocation(marker, user)
    }

    fun setUpNewLocationListener(): MutableLiveData<LatLng>? {
        var position: MutableLiveData<LatLng> = MutableLiveData()
        locationTracker.addListener(object : LocationTracker.Listener {

            override fun onLocationFound(location: Location) {
                position = MutableLiveData(LatLng(location.latitude, location.latitude))
                Log.d("MapsActivity2.TAG", "new position:" + MapsActivity2.newUserPosition)
                Log.d("MapsActivity2.TAG", "accuracy" + location.accuracy)
            }

            override fun onProviderError(providerError: ProviderError) {
            }

        });
        return position
    }

}