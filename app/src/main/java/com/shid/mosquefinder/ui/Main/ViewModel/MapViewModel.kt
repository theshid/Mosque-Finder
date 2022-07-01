package com.shid.mosquefinder.ui.Main.ViewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.shid.mosquefinder.data.model.Mosque
import com.shid.mosquefinder.data.model.Pojo.GoogleMosque
import com.shid.mosquefinder.data.model.Pojo.Place
import com.shid.mosquefinder.data.model.User
import com.shid.mosquefinder.data.repository.MapRepository
import com.shid.mosquefinder.utils.Resource
import java.util.HashMap

class MapViewModel(mapRepository: MapRepository, var application: Application) : ViewModel() {
    private var mMosqueList: MutableList<Mosque> = ArrayList()
    private var mGoogleMosqueList: MutableList<GoogleMosque> = ArrayList()
    private var mNigerGoogleMosqueList: MutableList<GoogleMosque> = ArrayList()
    private val mRepository = mapRepository
    val position: LatLng = LatLng(5.6363262, -0.2349102)
    private var getPlace: MutableLiveData<Place>? = null

    init {
        mMosqueList = mRepository.getTotalMosquesFromFirebase()
        Log.d("MapModel", mMosqueList.size.toString())
        mGoogleMosqueList = mRepository.getGoogleMosqueFromFirebase()
        mNigerGoogleMosqueList = mRepository.getNigerGoogleMosqueFromFirebase()
        retrieveStatusMsg()
    }

    fun retrieveStatusMsg(): LiveData<Resource<String>> {
        return mRepository.returnStatusMsg()
    }

    fun inputMosqueInDatabase(userInput: HashMap<String, Comparable<*>>) {
        mRepository.inputMosqueInDatabase(userInput)
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

}