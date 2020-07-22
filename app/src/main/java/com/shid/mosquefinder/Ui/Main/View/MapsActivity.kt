package com.shid.mosquefinder.Ui.Main.View

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.GoogleMap.*
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.*
import com.google.maps.DirectionsApiRequest
import com.google.maps.GeoApiContext
import com.google.maps.PendingResult
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.collections.MarkerManager
import com.google.maps.android.ktx.utils.collection.addMarker
import com.google.maps.internal.PolylineEncoding
import com.google.maps.model.DirectionsResult
import com.shid.mosquefinder.Data.Model.Api.ApiInterface
import com.shid.mosquefinder.Data.Model.ClusterMarker
import com.shid.mosquefinder.Data.Model.Mosque
import com.shid.mosquefinder.Data.Model.Pojo.Place
import com.shid.mosquefinder.Data.Model.PolylineData
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Utils.Common
import com.shid.mosquefinder.Utils.MyClusterManagerRenderer
import com.shid.mosquefinder.Utils.PermissionUtils
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.dialog_layout.*
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.StringBuilder

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
     OnPolylineClickListener {
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 999
        private const val TAG = "MapsActivity"

    }

    private lateinit var mMap: GoogleMap
    private lateinit var database: FirebaseFirestore
    private lateinit var mMosqueListEventListener: ListenerRegistration
    private var mMosqueList: MutableList<Mosque> = ArrayList()


    private lateinit var mMapBoundary: LatLngBounds
    private var mClusterManager: ClusterManager<ClusterMarker>? = null
    private var mClusterManagerRenderer: MyClusterManagerRenderer? = null
    private var mClusterMarkers: MutableList<ClusterMarker> = ArrayList()
    private var mPolylinesData: MutableList<PolylineData> = ArrayList()
    private val mTripMarkers: MutableList<Marker> = ArrayList()
    private var mSelectedMarker: Marker? = null
    private lateinit var userPosition: LatLng
    private var mapJob = Job()
    private var mGeoApiContext: GeoApiContext? = null
    private val uiScope = CoroutineScope(Dispatchers.Main + mapJob)
    private var markerManager: MarkerManager? = null
    private var markerCollection: MarkerManager.Collection? = null
    private var markerCollectionForClusters: MarkerManager.Collection? = null
    private var markerCollectionForClusters2: MarkerManager.Collection? = null

    lateinit var mService: ApiInterface
    internal var mosqueInArea: Place? = null


    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mService = Common.googleApiService
        if (mGeoApiContext == null) {
            mGeoApiContext = GeoApiContext.Builder()
                .apiKey(getString(R.string.google_maps_key))
                .build()
        }



        database = FirebaseFirestore.getInstance()
        btn_reset_map.setOnClickListener {
            addMapMarkers()

        }
        startActivityButton.setOnClickListener {
            MaterialDialog(this).show {
                title(text = "Mosque Finder")
                message(text = "Are you sure you want to add a mosque on this location?")
                positiveButton(text = "Yes")
                negativeButton(text = "Cancel")
                positiveButton(text = "Yes") { dialog ->
                    dialog.cancel()
                    mosqueInputDialog(userPosition)

                }
                negativeButton(text = "Cancel") { dialog ->
                    dialog.cancel()
                }
                icon(R.drawable.logo)
            }
        }

    }

    private fun mosqueInputDialog(userPosition: LatLng) {
        //val type = InputType.TYPE_CLASS_TEXT

        MaterialDialog(this).show {
            input(hint = "Enter the name of the mosque")
            // input(inputType = type)
            title(text = "Mosque Finder")
            message(text = "Are you sure you want to add a mosque on this location?")
            positiveButton(text = "Save Mosque") { dialog ->
                val inputField = dialog.getInputField().text.toString()

                dialog.cancel()
                saveMosqueInputInDatabase(userPosition, inputField)
            }
            negativeButton(text = "Cancel") { dialog ->
                dialog.cancel()
            }
            icon(R.drawable.logo)
        }
    }

    private fun saveMosqueInputInDatabase(userPosition: LatLng, inputField: String) {
        val mosqueLocation: GeoPoint = GeoPoint(userPosition.latitude, userPosition.longitude)
        val userMosqueInput = hashMapOf(
            "name" to inputField,
            "position" to mosqueLocation,
            "documentId" to "0",
            "report" to 0

        )

        database.collection("mosques").document()
            .set(userMosqueInput)
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully written!")
                addMapMarkers()


                Toast.makeText(this, "Mosque added", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error writing document", e)
                Toast.makeText(this, "Error try again", Toast.LENGTH_LONG).show()
            }

    }

    override fun onDestroy() {
        super.onDestroy()
        mapJob.cancel()

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
                            Log.d(TAG, "you clicked")
                            marker.showInfoWindow()

                            true
                        }

                        markerCollectionForClusters?.setOnInfoWindowClickListener(object :
                            OnInfoWindowClickListener {
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
        googlePlaceUrl.append("&key=" + getString(R.string.browser_key))
        googlePlaceUrl.append("&pagetoken=$token")
        Log.d("Url_debug", googlePlaceUrl.toString())
        return googlePlaceUrl.toString()
    }

    private fun getTotalMosqueFromFirebase() {
        val firebaseMosqueRef: CollectionReference = database.collection("mosques")
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
                        Log.d("Map", "the id  is" + mosqueElem.documentId)
                        mMosqueList.add(mosqueElem)
                        /* var lieu: LatLng = LatLng(locationMos.latitude,locationMos.longitude)
                         var marker : Marker = mMap.addMarker(MarkerOptions().position(lieu).title(mosqueName))*/
                        Log.d(TAG, "mosque position" + mosque.position.latitude)
                    }
                }
            })

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
//        mMap.addMarker(MarkerOptions().position(userPosition).title("Marker in Sydney"))

        getTotalMosqueFromFirebase()


        Handler().postDelayed(Runnable {
            //anything you want to start after 3s
            addMapMarkers()

            // addUserMarker()

        }, 3000)


        mMap.setOnPolylineClickListener(this)
        /*mMap.setOnInfoWindowClickListener(object : GoogleMap.OnInfoWindowClickListener {
            override fun onInfoWindowClick(marker: Marker) {
                *//*var lat = marker.position.latitude
                var long = marker.position.longitude
                var addPositions: LatLng
                addPositions = LatLng(lat, long)*//*

                Toast.makeText(applicationContext,"Youpiiiiii",Toast.LENGTH_LONG).show()
            }
        })*/

    }


    private fun addMapMarkers() {
        resetMap()
        googlePlaceNearbyMosques("mosque")
        getTotalMosqueFromFirebase()
        if (mClusterManager == null) {
            mClusterManager = ClusterManager(this.applicationContext, mMap)
        }
        if (mClusterManagerRenderer == null) {
            mClusterManagerRenderer = MyClusterManagerRenderer(
                this,
                mClusterManager!!,
                mMap
            )
            mClusterManager!!.renderer = mClusterManagerRenderer
        }
        if (mMosqueList != null) {
            for (mosqueLocation in mMosqueList) {
                Log.d(
                    "Map",
                    "addMapMarkers: location: " + mosqueLocation.position.toString()
                )
                try {
                    val snippet =
                        getString(R.string.determine_route) + " " + mosqueLocation.name + "?"
                    val title = mosqueLocation.name

                    /*val avatar: String = mosqueLocation
                    Log.d("Avatar", "avatar link $avatar")*/
                    // int avatar = R.mipmap.icon; // set the default avatar
                    val newClusterMarker =
                        ClusterMarker(

                            mosqueLocation.position.latitude,
                            mosqueLocation.position.longitude
                            ,
                            title,
                            snippet,
                            "verified",
                            false
                        )
                    mClusterManager!!.addItem(newClusterMarker)
                    markerCollectionForClusters2 = mClusterManager!!.markerCollection

                    //markerCollection.setOnInfoWindowClickListener(this)
                    /*   markerCollection.setOnInfoWindowClickListener(OnInfoWindowClickListener { marker: Marker? ->
                           Toast.makeText(applicationContext,"Yesssss",Toast.LENGTH_LONG).show()
                       })
                     markerCollection.setOnInfoWindowClickListener(object: GoogleMap.OnInfoWindowClickListener{
                         override fun onInfoWindowClick(p0: Marker?) {
                             Toast.makeText(applicationContext,"Yesssss",Toast.LENGTH_LONG).show()
                         }

                     })*/
                    mClusterMarkers.add(newClusterMarker)


                } catch (e: NullPointerException) {
                    Log.e(
                        "Map",
                        "addMapMarkers: NullPointerException: " + e.message
                    )
                }
            }
        }



        try {

            val snippet2 = getString(R.string.you)
            val newClusterMarker2 =
                ClusterMarker(
                    userPosition.latitude,
                    userPosition.longitude
                    ,
                    "You",
                    snippet2,
                    "Me",
                    false
                )
            mClusterManager!!.addItem(newClusterMarker2)
            mClusterMarkers.add(newClusterMarker2)
            /*val avatar: String = mosqueLocation
            Log.d("Avatar", "avatar link $avatar")*/
            // int avatar = R.mipmap.icon; // set the default avatar

            /*
            markerCollection.setOnInfoWindowClickListener(this)*/

        } catch (e: NullPointerException) {
            Log.e(
                "Map",
                "addMapMarkers: NullPointerException: " + e.message
            )
        }
        //val markerCollection: MarkerManager.Collection = mClusterManager!!.markerCollection
        markerCollectionForClusters2?.setOnMarkerClickListener { marker ->
            Log.d(TAG, "you clicked")
            //openDialog(marker!!)
            marker.showInfoWindow()
            true
        }

        markerCollectionForClusters2?.setOnInfoWindowClickListener(object :
            OnInfoWindowClickListener {
            override fun onInfoWindowClick(marker: Marker) {
                for (i in mClusterMarkers) {
                    //We check if the marker comes from Google Place or FireStore
                    if (i.isMarkerFromGooglePlace && i.title == marker.title) {
                        showDirectionInGoogleMapDialog(marker)

                    } else if (i.title == marker.title && !i.isMarkerFromGooglePlace) {
                        showOptionsDialog(marker)
                    }
                }

            }

        })
        mClusterManager!!.cluster()
        setCameraView()
    }

    private fun showOptionsDialog(marker: Marker) {

        if (marker.title.contains("You")) {
            marker.showInfoWindow()
        } else if (marker.title.contains("Trip")) {
            showDirectionInGoogleMapDialog(marker)
        } else {
            showOptionsForUserInputMosquesDialog(marker)
            //dialogOpenGoogleMap(marker)
            //dialogForRoute(marker)
        }

    }

    private fun showOptionsForUserInputMosquesDialog(marker: Marker) {
        MaterialDialog(this).show {
            customView(R.layout.dialog_layout, scrollable = true)
            title(text = "Mosque Finder")

            icon(R.drawable.logo)
            btn_open_map.setOnClickListener {
                this.cancel()
                showDirectionInGoogleMapDialog(marker)
            }

            btn_report.setOnClickListener {
                this.cancel()
                showReportDialog(marker)


            }
        }
    }

    private fun showReportDialog(marker: Marker) {
        MaterialDialog(this).show {
            title(text = "Mosque Finder")
            message(text = "Confirm or Report false location")
            icon(R.drawable.logo)


            positiveButton(text = "Confirm Mosque") { dialog ->
                dialog.cancel()
                confirmMosquePosition(marker)
            }
            negativeButton(text = "Report Mosque") { dialog ->
                dialog.cancel()
                reportMosquePosition(marker)
            }
        }
    }

    private fun confirmMosquePosition(marker:Marker){
        for (mosque in mMosqueList) {
            if (marker.position.latitude == mosque.position.latitude) {
                val reportIndex = mosque.report + 1
                database.collection("mosques").document(mosque.documentId)
                    .update("report", FieldValue.increment(1))
                    .addOnSuccessListener {
                        Toast.makeText(this@MapsActivity, "Thanks", Toast.LENGTH_LONG)
                            .show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this@MapsActivity, "Error", Toast.LENGTH_LONG).show()
                    }
            }
        }
    }

    private fun reportMosquePosition(marker: Marker){
        for (mosque in mMosqueList) {
            if (marker.title == mosque.name) {

                database.collection("mosques").document(mosque.documentId)
                    .update("report",  FieldValue.increment(-1))
                    .addOnSuccessListener {
                        Toast.makeText(this@MapsActivity, "Thanks", Toast.LENGTH_LONG)
                            .show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this@MapsActivity, "Error" , Toast.LENGTH_LONG).show()
                        Log.d("Error", it.message.toString() + it.localizedMessage.toString())
                    }
            }
        }
    }

    private fun showDirectionInGoogleMapDialog(marker: Marker) {

        MaterialDialog(this).show {
            title(text = "Mosque Finder")
            message(text = "Show Directions in Google Map?")
            icon(R.drawable.logo)


            positiveButton(text = "Confirm Mosque") { dialog ->
                dialog.cancel()
                intentToGoogleMap(marker)
            }
            negativeButton(text = "Report Mosque") { dialog ->
                dialog.cancel()
                Toast.makeText(applicationContext, "Window closed", Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun intentToGoogleMap(marker:Marker){

        val latitude: String = marker.position.latitude.toString()
        val longitude: String = marker.position.longitude.toString()
        val gmmIntentUri = Uri.parse("google.navigation:q=$latitude,$longitude")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        try {
            if (mapIntent.resolveActivity(applicationContext.packageManager) != null) {
                startActivity(mapIntent)
            }
        } catch (e: java.lang.NullPointerException) {
            Log.e(TAG, "onClick: NullPointerException: Couldn't open map." + e.message)
            Toast.makeText(
                applicationContext,
                getString(R.string.map_error),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun dialogForRoute(marker: Marker) {
        // Initialize a new instance of
        val builder = AlertDialog.Builder(this@MapsActivity)

        // Set the alert dialog title
        builder.setTitle("Mosque Finder")

        // Display a message on alert dialog
        builder.setMessage(getString(R.string.determine_route) + " " + marker.title + "?")

        // Set a positive button and its click listener on alert dialog
        builder.setPositiveButton("YES") { dialog, which ->
            // Do something when user press the positive button
            resetSelectedMarker()
            mSelectedMarker = marker
            calculateDirections(marker)
            dialog.dismiss()


        }


        // Display a negative button on alert dialog
        builder.setNegativeButton("No") { dialog, which ->
            Toast.makeText(applicationContext, "Cancelled", Toast.LENGTH_SHORT).show()
        }


        // Display a neutral button on alert dialog
        builder.setNeutralButton("Cancel") { _, _ ->
            Toast.makeText(applicationContext, "Cancelled", Toast.LENGTH_SHORT).show()
        }

        // Finally, make the alert dialog using builder
        val dialog: AlertDialog = builder.create()

        // Display the alert dialog on app interface
        dialog.show()

    }

    private fun setCameraView() {
        // Set a boundary to start


        // Set a boundary to start
        val bottomBoundary: Double = userPosition.latitude - .009
        val leftBoundary: Double = userPosition.longitude - .009
        val topBoundary: Double = userPosition.latitude + .009
        val rightBoundary: Double = userPosition.longitude + .009

        mMapBoundary = LatLngBounds(
            LatLng(bottomBoundary, leftBoundary),
            LatLng(topBoundary, rightBoundary)
        )
        try {
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0))
        } catch (ise: IllegalStateException) {
            mMap.setOnMapLoadedCallback(OnMapLoadedCallback {
                mMap.moveCamera(
                    CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0)
                )
            })
        }
    }

    private fun resetMap() {
        mMap.clear()
        if (mClusterManager != null) {
            mClusterManager!!.clearItems()
        }
        if (mClusterMarkers.size > 0) {
            mClusterMarkers.clear()
            mClusterMarkers = java.util.ArrayList()
        }
        if (mPolylinesData.size > 0) {
            mPolylinesData.clear()
            mPolylinesData = ArrayList()
        }
    }

    private fun calculateDirections(marker: Marker) {
        Log.d(TAG, "calculateDirections: calculating directions.")
        //Determine the position of the marker that we are clicking on the map
        val destination = com.google.maps.model.LatLng(
            marker.position.latitude,
            marker.position.longitude
        )
        val directions = DirectionsApiRequest(mGeoApiContext)
        directions.alternatives(true)
        directions.origin(
            com.google.maps.model.LatLng(
                userPosition.latitude,
                userPosition.longitude
            )
        )
        Log.d(TAG, "calculateDirections: destination: $destination")
        directions.destination(destination)
            .setCallback(object : PendingResult.Callback<DirectionsResult> {
                override fun onResult(result: DirectionsResult) {
                    Log.d(TAG, "calculateDirections: routes: " + result.routes[0].toString())
                    Log.d(
                        TAG,
                        "calculateDirections: duration: " + result.routes[0].legs[0].duration
                    )
                    Log.d(
                        TAG,
                        "calculateDirections: distance: " + result.routes[0].legs[0].distance
                    )
                    Log.d(
                        TAG,
                        "calculateDirections: geocodedWayPoints: " + result.geocodedWaypoints[0].toString()
                    )
                    addPolylinesToMap(result)
                }

                override fun onFailure(e: Throwable) {
                    Log.e(TAG, "calculateDirections: Failed to get directions: " + e.message)
                }
            })
    }

    private fun addPolylinesToMap(directionsResult: DirectionsResult) {
        uiScope.launch {
            withContext(Dispatchers.Main) {
                Log.d(TAG, "run: result routes: " + directionsResult.routes.size)
                //make sure that when we click a direction the old polyline gets removed
                //make sure that when we click a direction the old polyline gets removed
                if (mPolylinesData.size > 0) {
                    for (polylineData in mPolylinesData) {
                        polylineData.polyline.remove()
                    }
                    mPolylinesData.clear()
                    mPolylinesData = ArrayList()
                }
                var duration = 99999999.0
                //Gives you a list of check points
                //Gives you a list of check points
                for (route in directionsResult.routes) {
                    Log.d(TAG, "run: leg: " + route.legs[0].toString())
                    //Decoding the check points
                    val decodedPath =
                        PolylineEncoding.decode(route.overviewPolyline.encodedPath)
                    val newDecodedPath: MutableList<LatLng> = ArrayList()

                    // This loops through all the LatLng coordinates of ONE polyline.
                    for (latLng in decodedPath) {

//                        Log.d(TAG, "run: latlng: " + latLng.toString());
                        newDecodedPath.add(
                            LatLng(
                                latLng.lat,
                                latLng.lng
                            )
                        )
                    }
                    val polyline: Polyline =
                        mMap.addPolyline(PolylineOptions().addAll(newDecodedPath))
                    polyline.color = ContextCompat.getColor(applicationContext,
                        R.color.grey
                    )
                    polyline.isClickable = true
                    mPolylinesData.add(
                        PolylineData(
                            polyline,
                            route.legs[0]
                        )
                    )
                    mSelectedMarker!!.isVisible = false
                    val tempDuration = route.legs[0].duration.inSeconds.toDouble()
                    if (tempDuration < duration) {
                        duration = tempDuration
                        onPolylineClick(polyline)
                        zoomRoute(polyline.points)
                    }
                }
            }
        }
    }

    private fun zoomRoute(lstLatLngRoute: List<LatLng>) {
        if (lstLatLngRoute.isEmpty()) return

        val boundsBuilder = LatLngBounds.Builder()
        for (latLngPoint in lstLatLngRoute) boundsBuilder.include(
            latLngPoint
        )

        val routePadding = 120
        val latLngBounds = boundsBuilder.build()

        mMap.animateCamera(
            CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding),
            600,
            null
        )
    }


    @SuppressLint("MissingPermission")
    private fun setUpLocationListener() {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
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

    override fun onStart() {
        super.onStart()
        when {
            PermissionUtils.isAccessFineLocationGranted(this) -> {
                when {
                    PermissionUtils.isLocationEnabled(this) -> {
                        setUpLocationListener()
                    }
                    else -> {
                        PermissionUtils.showGPSNotEnabledDialog(this)
                    }
                }
            }
            else -> {
                PermissionUtils.requestAccessFineLocationPermission(
                    this,
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    when {
                        PermissionUtils.isLocationEnabled(this) -> {
                            setUpLocationListener()
                        }
                        else -> {
                            PermissionUtils.showGPSNotEnabledDialog(this)
                        }
                    }
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.location_permission_not_granted),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }


    private fun resetSelectedMarker() {
        if (mSelectedMarker != null) {
            mSelectedMarker!!.isVisible = true
            mSelectedMarker = null
            removeTripMarkers()
        }
    }

    private fun removeTripMarkers() {
        for (marker in mTripMarkers) {
            marker.remove()
        }
        markerCollection!!.clear()

    }

    override fun onPolylineClick(polyline: Polyline) {
        val index = 0
        //Were're loopping through to see if the polyline selected is the same as the one in the
        // arrayList if there is a match the polyline will become blue

        for (polylineData in mPolylinesData) {
            Log.d(TAG, "onPolylineClick: toString: $polylineData")
            if (polyline.id == polylineData.polyline.id) {
                polylineData.polyline.color =
                    ContextCompat.getColor(applicationContext,
                        R.color.unicef
                    )
                polylineData.polyline.zIndex =
                    1F // gives a elevation in orfer to easily differenciate from the others
                val endpoint =
                    LatLng(
                        polylineData.leg.endLocation.lat,
                        polylineData.leg.endLocation.lng
                    )
                markerManager = MarkerManager(mMap)
                markerCollection = markerManager!!.newCollection()

                val marker: Marker = markerCollection!!.addMarker {
                    position(endpoint)
                    title(getString(R.string.trip) + index)
                    snippet(getString(R.string.duree) + polylineData.leg.duration)
                }
                /* val marker: Marker = mMap.addMarker(
                     MarkerOptions()
                         .position(endpoint)
                         .title(getString(R.string.trip) + index)
                         .snippet(getString(R.string.duree) + polylineData.leg.duration)
                 )*/
                marker.showInfoWindow()
                mTripMarkers.add(marker)
                markerCollection!!.setOnMarkerClickListener(object : OnMarkerClickListener {
                    override fun onMarkerClick(marker: Marker): Boolean {
                        showDirectionInGoogleMapDialog(marker)
                        return true
                    }
                })

                markerCollection!!.setOnInfoWindowClickListener(object : OnInfoWindowClickListener {
                    override fun onInfoWindowClick(marker: Marker) {
                        showDirectionInGoogleMapDialog(marker)
                    }

                })
            } else {
                polylineData.polyline.color =
                    ContextCompat.getColor(applicationContext,
                        R.color.grey
                    )
                polylineData.polyline.zIndex = 0F
            }
        }
    }


}