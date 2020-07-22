package com.shid.mosquefinder.Data.Repository

import android.annotation.SuppressLint
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.*
import com.google.maps.android.clustering.ClusterManager
import com.shid.mosquefinder.App
import com.shid.mosquefinder.Data.Model.Api.ApiInterface
import com.shid.mosquefinder.Data.Model.ClusterMarker
import com.shid.mosquefinder.Data.Model.Mosque
import com.shid.mosquefinder.Data.Model.Pojo.Place
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Ui.Main.View.MapsActivity
import com.shid.mosquefinder.Utils.Common
import com.shid.mosquefinder.Utils.MyClusterManagerRenderer
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.StringBuilder

class MapRepository constructor(var mService:ApiInterface) {
    private val database:FirebaseFirestore = FirebaseFirestore.getInstance()
    private  val firebaseMosqueRef: CollectionReference = database.collection("mosques")
    private lateinit var mMosqueListEventListener: ListenerRegistration
    private val TAG:String = "Map Repository"
    private val mApp:App = App()
    private lateinit var userPosition: LatLng
    init {
        mService = Common.googleApiService
    }

    private fun getTotalMosquesFromFirebase():MutableList<Mosque>{
        val mMosqueList:MutableList<Mosque> = ArrayList()
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
                        // mMosqueList.add(mosque)
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
        return  mMosqueList
    }

    @SuppressLint("MissingPermission")
    private fun setUpLocationListener() {
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
        googlePlaceUrl.append("&key=" + mApp.applicationContext.getString(R.string.browser_key))
        googlePlaceUrl.append("&pagetoken=$token")
        Log.d("Url_debug", googlePlaceUrl.toString())
        return googlePlaceUrl.toString()
    }

    private fun googlePlaceNearbyMosques(keyword: String, nextToken: String = "") {
        //mMap.clear()

        //Build url request base on location
        val requestUrl = getRequestUrl(userPosition.latitude, userPosition.longitude, keyword, nextToken)

        mService.getNearbyPlaces(requestUrl)
            .enqueue(object : Callback<Place> {
                override fun onFailure(call: Call<Place>, t: Throwable) {
                    Toast.makeText(baseContext, "" + t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(call: Call<Place>, response: Response<Place>) {
                    mosqueInArea = response.body()
                    if (mClusterManager == null) {
                        mClusterManager = ClusterManager(applicationContext, mMap)
                    }
                    if (mClusterManagerRenderer == null) {
                        mClusterManagerRenderer = MyClusterManagerRenderer(
                            applicationContext,
                            mClusterManager!!,
                            mMap
                        )
                        mClusterManager!!.renderer = mClusterManagerRenderer
                    }
                    if (response.isSuccessful) { //for(i in 0 until response.body()!!.results!!.size)
                        for (i in mosqueInArea!!.results!!.indices) {

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
                                    getString(R.string.determine_route) + " " + placeName + "?"
                                val title = placeName

                                /*val avatar: String = mosqueLocation
                                Log.d("Avatar", "avatar link $avatar")*/
                                // int avatar = R.mipmap.icon; // set the default avatar
                                val newClusterMarker =
                                    ClusterMarker(

                                        lat,
                                        lng
                                        ,
                                        title,
                                        snippet,
                                        "default",
                                        true
                                    )
                                mClusterManager!!.addItem(newClusterMarker)
                                markerCollectionForClusters = mClusterManager!!.markerCollection

                                mClusterMarkers.add(newClusterMarker)


                            } catch (e: NullPointerException) {
                                Log.e(
                                    "Map",
                                    "addMapMarkers: NullPointerException: " + e.message
                                )
                            }


                        }
                        markerCollectionForClusters?.setOnMarkerClickListener { marker ->
                            Log.d(MapsActivity.TAG, "you clicked")
                            marker.showInfoWindow()

                            true
                        }

                        markerCollectionForClusters?.setOnInfoWindowClickListener(object :
                            GoogleMap.OnInfoWindowClickListener {
                            override fun onInfoWindowClick(marker: Marker) {
                                for (i in mClusterMarkers) {
                                    if (i.isMarkerFromGooglePlace && i.title == marker.title) {
                                        showDirectionInGoogleMapDialog(marker)

                                    } else if (i.title == marker.title && !i.isMarkerFromGooglePlace) {
                                        showOptionsDialog(marker)
                                    }
                                }
                            }

                        })
                        mClusterManager!!.cluster()
                    }
                    //Code to loop to get at most 60 mosque
                    /*  if (response.body()!!.nextPageToken != ""){

                          Handler().postDelayed(Runnable {

                              nearByPlace("mosque", response.body()!!.nextPageToken.toString())


                          }, 3000)
                      } else{
                          Toast.makeText(applicationContext,"No more results",Toast.LENGTH_LONG).show()
                      }*/
                }

            })
    }
}