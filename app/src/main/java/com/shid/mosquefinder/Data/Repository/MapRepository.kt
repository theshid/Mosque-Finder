package com.shid.mosquefinder.Data.Repository


import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.*
import com.shid.mosquefinder.Data.Model.Api.ApiInterface
import com.shid.mosquefinder.Data.Model.ClusterMarker
import com.shid.mosquefinder.Data.Model.Mosque
import com.shid.mosquefinder.Data.Model.Pojo.Place
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Utils.Common
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.StringBuilder
import java.util.HashMap

class MapRepository constructor( mService: ApiInterface, application: Application) {
    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val firebaseMosqueRef: CollectionReference = database.collection("mosques")
    private lateinit var mMosqueListEventListener: ListenerRegistration
    private val TAG: String = "Map Repository"
    private val mApp: Application = application
    private val service = Common.googleApiService

    private var mClusterMarkers: MutableList<ClusterMarker> = ArrayList()
     var mMosqueList: MutableList<Mosque> = ArrayList()
    val apiService:ApiInterface = mService
    val placeData:MutableLiveData<Place> = MutableLiveData<com.shid.mosquefinder.Data.Model.Pojo.Place>()

    init {


    }

    fun getTotalMosquesFromFirebase(): MutableList<Mosque> {
        mMosqueListEventListener =
            firebaseMosqueRef.addSnapshotListener(EventListener<QuerySnapshot> { querySnapshot: QuerySnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                if (firebaseFirestoreException != null) {
                    Log.e(TAG, "onEvent: Listen failed.", firebaseFirestoreException)
                    return@EventListener
                }

                if (querySnapshot != null) {

                    mMosqueList.clear()
                    for (doc in querySnapshot) {
                        val mosque = doc.toObject(Mosque::class.java)

                        mosque.documentId = doc.id

                        var mosqueName: String = doc.get("name") as String
                        var locationMos: GeoPoint = doc.get("position") as GeoPoint
                        var mosqueId: String = mosque.documentId
                        var reportIndex: Long = doc.get("report") as Long

                        var mosqueElem: Mosque =
                            Mosque(
                                mosqueName,
                                locationMos,
                                mosqueId,
                                reportIndex
                            )
                        Log.d(TAG, "the id  is" + mosqueElem.documentId)
                        mMosqueList.add(mosqueElem)
                        /* var lieu: LatLng = LatLng(locationMos.latitude,locationMos.longitude)
                         var marker : Marker = mMap.addMarker(MarkerOptions().position(lieu).title(mosqueName))*/
                        Log.d(TAG, "mosque position" + mosque.position.latitude)
                    }
                }
            })
        Log.d(TAG,"Mosque firebase" +mMosqueList.isEmpty().toString())
        return mMosqueList
    }


    private fun getRequestUrl(
        latitude: Double,
        longitude: Double,
        place: String,
        token: String = ""
    ): String {
        val googlePlaceUrl =
            StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
        googlePlaceUrl.append("?location=$latitude,$longitude")
        googlePlaceUrl.append("&radius=3000")
        googlePlaceUrl.append("&type=$place")
        googlePlaceUrl.append("&key=" + mApp.getString(R.string.browser_key))
        googlePlaceUrl.append("&pagetoken=$token")
        Log.d("Url_debug", googlePlaceUrl.toString())
        return googlePlaceUrl.toString()
    }

    fun googlePlaceNearbyMosques(
        keyword: String,
        userPosition: LatLng,
        nextToken: String = ""
    )
            : MutableLiveData<Place>? {

        //mMap.clear()

        //Build url request base on location
        val requestUrl =
            getRequestUrl(userPosition.latitude, userPosition.longitude, keyword, nextToken)

        service.getNearbyPlaces(requestUrl)
            .enqueue(object : Callback<Place> {
                override fun onFailure(call: Call<Place>, t: Throwable) {
                    /*Toast.makeText(mApp.applicationContext, "" + t.message, Toast.LENGTH_LONG)
                        .show()*/
                    Log.d(TAG,"failed")
                    placeData?.value = null
                }

                override fun onResponse(call: Call<Place>, response: Response<Place>) {
                    val mosqueInArea = response.body()

                    if (response.isSuccessful) { //for(i in 0 until response.body()!!.results!!.size)
                        placeData?.value = response.body()
                        for (i in mosqueInArea!!.results.indices) {

                            val markerOptions = MarkerOptions()
                            val googlePlace = mosqueInArea!!.results!![i]
                            val lat = googlePlace.geometry!!.location!!.lat
                            val lng = googlePlace.geometry!!.location!!.lng
                            val placeName = googlePlace.name
                            val latLng = LatLng(lat, lng)

                            Log.d(
                                "Map",
                                "addMapMarkers: location: " + googlePlace.geometry.location.toString()
                            )
                            try {
                                val snippet =
                                    mApp.getString(R.string.determine_route) + " " + placeName + "?"
                                val title = placeName

                                /*val avatar: String = mosqueLocation
                                Log.d("Avatar", "avatar link $avatar")*/
                                // int avatar = R.mipmap.icon; // set the default avatar
                                val newClusterMarker =
                                    ClusterMarker(

                                        lat,
                                        lng,
                                        title,
                                        snippet,
                                        "default",
                                        true
                                    )

                                //markerCollectionForClusters = mClusterManager!!.markerCollection


                                //mClusterMarkers.add(newClusterMarker)


                            } catch (e: NullPointerException) {
                                Log.e(
                                    "Map",
                                    "addMapMarkers: NullPointerException: " + e.message
                                )
                            }


                        }

                }

                }

            })
        Log.d(TAG,"Api Mosque"+ mClusterMarkers.isEmpty().toString())
        return placeData
    }

    fun inputMosqueInDatabase(userInput: HashMap<String, Comparable<*>>) {
        database.collection("mosques").document()
            .set(userInput)
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully written!")
                //addMapMarkers()

            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error writing document", e)

            }
    }

    fun confirmMosqueLocation(marker: Marker) {
        for (mosque in mMosqueList) {
            if (marker.title == mosque.name) {
                val reportIndex = mosque.report + 1
                database.collection("mosques").document(mosque.documentId)
                    .update("report", FieldValue.increment(1))
                    .addOnSuccessListener {
                        //Toast.makeText(this@MapsActivity, "Thanks", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener {
                        //Toast.makeText(this@MapsActivity, "Error", Toast.LENGTH_LONG).show()
                    }
            }
        }
    }

    fun reportFalseLocation(marker: Marker) {
        for (mosque in mMosqueList) {
            if (marker.title == mosque.name) {

                database.collection("mosques").document(mosque.documentId)
                    .update("report", FieldValue.increment(-1))
                    .addOnSuccessListener {
                        //Toast.makeText(this@MapsActivity, "Thanks", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener {
                        // Toast.makeText(this@MapsActivity, "Error" , Toast.LENGTH_LONG).show()
                        Log.d("Error", it.message.toString() + it.localizedMessage.toString())
                    }
            }
        }
    }
}