package com.shid.mosquefinder.app.ui.main.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.shid.mosquefinder.app.utils.helper_class.Resource
import com.shid.mosquefinder.data.model.ClusterMarker
import com.shid.mosquefinder.data.model.Mosque
import com.shid.mosquefinder.data.model.pojo.GoogleMosque
import com.shid.mosquefinder.data.repository.MapRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(mapRepository: MapRepository) : ViewModel() {

    private var mMosqueList: MutableList<Mosque> = ArrayList()
    private var mGoogleMosqueList: MutableList<GoogleMosque> = ArrayList()
    private var mNigerGoogleMosqueList: MutableList<GoogleMosque> = ArrayList()
    private var mClusterMarkerList: MutableList<ClusterMarker> = ArrayList()
    private val mRepository = mapRepository

    init {

        mMosqueList = mRepository.getTotalMosquesFromFirebase()
        mGoogleMosqueList = mRepository.getGoogleMosqueFromFirebase()
        mNigerGoogleMosqueList = mRepository.getNigerGoogleMosqueFromFirebase()
        //getClusterMarkers()

        retrieveStatusMsg()


    }

    fun retrieveStatusMsg(): LiveData<Resource<String>> {
        return mRepository.returnStatusMsg()
    }

    fun getGoogleMosqueFromRepository(): MutableList<GoogleMosque> {
        return mGoogleMosqueList
    }

    fun getNigerGoogleMosqueFromRepository(): MutableList<GoogleMosque> {
        return mNigerGoogleMosqueList
    }

    fun getUsersMosqueFromRepository(): MutableList<Mosque> {

        return mMosqueList
    }

    fun getClusterMarkers(): MutableList<ClusterMarker> {
        var newClusterMarker: ClusterMarker? = null
        var newClusterMarker2: ClusterMarker? = null
        for (mosqueLocation in mMosqueList) {

            val title = mosqueLocation.name
            val snippet = ""
            val distanceFromUser = 0.0
            newClusterMarker =
                ClusterMarker(

                    mosqueLocation.position.latitude,
                    mosqueLocation.position.longitude,
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
                val snippet = ""
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
        Log.d("model", mClusterMarkerList.size.toString())
        Log.d("model", mMosqueList.size.toString())
        Log.d("model", mGoogleMosqueList.size.toString())

        return mClusterMarkerList
    }
}