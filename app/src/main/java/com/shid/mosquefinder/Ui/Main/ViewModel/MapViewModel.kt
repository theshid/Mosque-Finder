package com.shid.mosquefinder.Ui.Main.ViewModel

import android.app.Application
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
import com.shid.mosquefinder.Ui.Main.View.MapsActivity2
import com.shid.mosquefinder.Utils.Resource
import java.util.HashMap

class MapViewModel( mapRepository: MapRepository, application: Application) :ViewModel() {
    private var mMosqueList: MutableList<Mosque> = ArrayList()
    private var mGoogleMosqueList: MutableList<GoogleMosque> = ArrayList()
    private val mRepository = mapRepository
    val position:LatLng = LatLng(5.6363262,-0.2349102)
    private  var getPlace: MutableLiveData<Place>? = null


    init {

        mMosqueList = mRepository.getTotalMosquesFromFirebase()
        mGoogleMosqueList = mRepository.getGoogleMosqueFromFirebase()
        retrieveStatusMsg()


        /*if (MapsActivity2.userPosition != null){
            getPlace = mRepository.googlePlaceNearbyMosques("mosque", MapsActivity2.userPosition!!)
        }*/

    }

     fun retrieveStatusMsg():LiveData<Resource<String>> {
        return mRepository.returnStatusMsg()
    }

    fun inputMosqueInDatabase(userInput: HashMap<String, Comparable<*>>) {
        mRepository.inputMosqueInDatabase(userInput)
    }

    fun getGoogleMosqueFromRepository():MutableList<GoogleMosque>{
        return mGoogleMosqueList
    }

    fun getUsersMosqueFromRepository(): MutableList<Mosque> {

        return mMosqueList
    }

    fun getGoogleMapMosqueFromRepository(): MutableLiveData<Place>? {

        return getPlace

    }

    fun confirmMosqueLocation(marker: Marker,user:User) {
        mRepository.confirmMosqueLocation(marker,user)
    }

    fun reportFalseMosqueLocation(marker: Marker,user:User) {
        mRepository.reportFalseLocation(marker,user)
    }

}