package com.shid.mosquefinder.Data.Repository


import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import com.shid.mosquefinder.Data.Model.Api.ApiInterface
import com.shid.mosquefinder.Data.Model.ClusterMarker
import com.shid.mosquefinder.Data.Model.Mosque
import com.shid.mosquefinder.Data.Model.Pojo.GoogleMosque
import com.shid.mosquefinder.Data.Model.Pojo.Place
import com.shid.mosquefinder.Data.Model.User
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Utils.Common
import com.shid.mosquefinder.Utils.Resource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.StringBuilder
import java.util.HashMap

class MapRepository constructor(mService: ApiInterface, application: Application) {
    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val firebaseMosqueRef: CollectionReference = database.collection("mosques")
    private lateinit var mMosqueListEventListener: ListenerRegistration
    private val realDatabase: DatabaseReference = Firebase.database.reference

    private val firebaseGoogleMosqueRef: CollectionReference = database.collection("test")
    private lateinit var mGoogleMosqueListEventListener: ListenerRegistration
    var mGoogleMosqueList: MutableList<GoogleMosque> = ArrayList()

    private val TAG: String = "Map Repository"
    private val mApp: Application = application
    private val service = Common.googleApiService

    private var mClusterMarkers: MutableList<ClusterMarker> = ArrayList()
    var mMosqueList: MutableList<Mosque> = ArrayList()
    val apiService: ApiInterface = mService
    val placeData: MutableLiveData<Place> =
        MutableLiveData<com.shid.mosquefinder.Data.Model.Pojo.Place>()

    private val statusMsg: MutableLiveData<Resource<String>> = MutableLiveData()

    init {


    }

    fun getGoogleMosqueFromFirebase(): MutableList<GoogleMosque> {
        mGoogleMosqueListEventListener =
            firebaseGoogleMosqueRef.addSnapshotListener(EventListener<QuerySnapshot>
            { querySnapshot: QuerySnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                if (firebaseFirestoreException != null) {
                    Log.e(TAG, "onEvent: Listen failed.", firebaseFirestoreException)
                    statusMsg.postValue(Resource.error(firebaseFirestoreException.toString(),"could not load data, check internet"))
                    return@EventListener
                }

                if (querySnapshot != null) {

                    mGoogleMosqueList.clear()
                    for (doc in querySnapshot) {
                        val mosque = doc.toObject(GoogleMosque::class.java)

                        //mosque.documentId = doc.id

                        var mosqueLat: Double = doc.get("Latitude") as Double
                        var mosqueLg = doc.get("Longitude") as Double
                        var mosqueId: String = doc.get("Place ID") as String
                        var mosqueName: String = doc.get("Place Name") as String


                        var mosqueElem: GoogleMosque =
                            GoogleMosque(
                                mosqueLat,
                                mosqueLg,
                                mosqueId,
                                mosqueName
                            )
                        //Log.d(TAG, "the id  is" + mosqueElem.documentId)
                        mGoogleMosqueList.add(mosqueElem)
                        /* var lieu: LatLng = LatLng(locationMos.latitude,locationMos.longitude)
                         var marker : Marker = mMap.addMarker(MarkerOptions().position(lieu).title(mosqueName))*/
                        //Log.d(TAG, "mosque position" + mosque.position.latitude)
                    }
                }
            })
        Log.d(TAG, "Mosque firebase" + mMosqueList.isEmpty().toString())
        return mGoogleMosqueList
    }

    fun getTotalMosquesFromFirebase(): MutableList<Mosque> {
        mMosqueListEventListener =
            firebaseMosqueRef.addSnapshotListener(EventListener<QuerySnapshot> { querySnapshot: QuerySnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                if (firebaseFirestoreException != null) {
                    Log.e(TAG, "onEvent: Listen failed.", firebaseFirestoreException)
                    statusMsg.postValue(Resource.error(firebaseFirestoreException.toString(),"could not load data, check internet"))
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
        Log.d(TAG, "Mosque firebase" + mMosqueList.isEmpty().toString())
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
                    Log.d(TAG, "failed")
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
        Log.d(TAG, "Api Mosque" + mClusterMarkers.isEmpty().toString())
        return placeData
    }

    fun inputMosqueInDatabase(userInput: HashMap<String, Comparable<*>>) {
        database.collection("mosques").document()
            .set(userInput)
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully written!")
                statusMsg.postValue(Resource.success("Thank you for confirming location"))
                //addMapMarkers()

            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error writing document", e)
                statusMsg.postValue(
                    Resource.error(
                        e.localizedMessage,
                        "Mosque has not been added check internet"
                    )
                )

            }
    }

    fun returnStatusMsg(): LiveData<Resource<String>> {
        return statusMsg
    }

    fun confirmMosqueLocation(marker: Marker, user: User) {
        for (mosque in mMosqueList) {
            if (marker.title == mosque.name) {
                val reportIndex = mosque.report + 1


                realDatabase.child("votes").child(mosque.documentId)
                    .child(user.uid).setValue(1L)
                    .addOnSuccessListener {
                        statusMsg.postValue(Resource.success("Thank you for confirming location"))
                        database.collection("mosques").document(mosque.documentId)
                            .update("report", FieldValue.increment(1))
                            .addOnSuccessListener {
                                //Toast.makeText(this@MapsActivity, "Thanks", Toast.LENGTH_LONG).show()
                            }
                            .addOnFailureListener {

                            }


                    }
                    .addOnFailureListener {
                        statusMsg.postValue(
                            Resource.error(
                                it.localizedMessage,
                                "You already made a choice "
                            )
                        )
                    }


            }

        }
    }


    fun reportFalseLocation(marker: Marker, user: User) {
        for (mosque in mMosqueList) {
            if (marker.title == mosque.name) {

                realDatabase.child("votes").child(mosque.documentId)
                    .child(user.uid).setValue(-1L)
                    .addOnSuccessListener {
                        statusMsg.postValue(Resource.success("Thank you for reporting location"))
                        database.collection("mosques").document(mosque.documentId)
                            .update("report", FieldValue.increment(-1))
                            .addOnSuccessListener {
                                //Toast.makeText(this@MapsActivity, "Thanks", Toast.LENGTH_LONG).show()
                            }
                            .addOnFailureListener {

                                Log.d(
                                    "Error",
                                    it.message.toString() + it.localizedMessage.toString()
                                )
                            }
                    }
                    .addOnFailureListener {
                        statusMsg.postValue(
                            Resource.error(
                                it.localizedMessage,
                                "You already reported this location "
                            )
                        )
                    }


            }
        }
    }
}