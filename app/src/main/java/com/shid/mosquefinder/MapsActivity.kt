package com.shid.mosquefinder

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
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
import com.google.maps.internal.PolylineEncoding
import com.google.maps.model.DirectionsResult
import com.shid.mosquefinder.Model.ClusterMarker
import com.shid.mosquefinder.Model.Mosque
import com.shid.mosquefinder.Model.PolylineData
import com.shid.mosquefinder.Utils.MyClusterManagerRenderer
import com.shid.mosquefinder.Utils.PermissionUtils
import kotlinx.coroutines.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
    OnInfoWindowClickListener, OnPolylineClickListener {
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
    private  var mClusterManagerRenderer: MyClusterManagerRenderer? = null
    private var mClusterMarkers: MutableList<ClusterMarker> = ArrayList()
    private var mPolylinesData: MutableList<PolylineData> = ArrayList()
    private val mTripMarkers: MutableList<Marker> = ArrayList()
    private var mSelectedMarker: Marker? = null
    private lateinit var userPosition: LatLng
    private var mapJob = Job()
    private var mGeoApiContext: GeoApiContext? = null
    private val uiScope = CoroutineScope(Dispatchers.Main + mapJob)
    private var markerManager: MarkerManager? = null



    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        if (mGeoApiContext == null) {
            mGeoApiContext = GeoApiContext.Builder()
                .apiKey(getString(R.string.google_maps_key))
                .build()
        }



        database = FirebaseFirestore.getInstance()


    }

    override fun onDestroy() {
        super.onDestroy()
        mapJob.cancel()
    }

    private fun getTotalMosques() {
        val mosqueRef: CollectionReference = database.collection("mosques")
        mMosqueListEventListener =
            mosqueRef.addSnapshotListener(EventListener<QuerySnapshot> { querySnapshot: QuerySnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                if (firebaseFirestoreException != null) {
                    Log.e(TAG, "onEvent: Listen failed.", firebaseFirestoreException)
                    return@EventListener
                }

                if (querySnapshot != null) {
                    mMosqueList.clear()
                    for (doc in querySnapshot) {
                        val mosque = doc.toObject(Mosque::class.java)
                       // mMosqueList.add(mosque)
                        var mosqueName: String = doc.get("name") as String
                        var locationMos: GeoPoint = doc.get("position") as GeoPoint
                        var mosqueElem:Mosque = Mosque(mosqueName,locationMos)
                        mMosqueList.add(mosqueElem)
                       /* var lieu: LatLng = LatLng(locationMos.latitude,locationMos.longitude)
                        var marker : Marker = mMap.addMarker(MarkerOptions().position(lieu).title(mosqueName))*/
                        Log.d(TAG,"mosque position"+ mosque.position.latitude)
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

            getTotalMosques()


        Handler().postDelayed(Runnable {
            //anything you want to start after 3s
            addMapMarkers()
           // addUserMarker()

        }, 3000)
        mMap.setOnInfoWindowClickListener(this)

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

   /* private fun addUserMarker(){
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



        mClusterManager!!.cluster()


    }*/

    private fun addMapMarkers() {
        resetMap()
        //getTotalMosques()
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
        for (mosqueLocation in mMosqueList) {
            Log.d(
                "Map",
                "addMapMarkers: location: " + mosqueLocation.position.toString()
            )
            try {
                val snippet = getString(R.string.determine_route) + " " + mosqueLocation.name + "?"
                val title =  mosqueLocation.name

                /*val avatar: String = mosqueLocation
                Log.d("Avatar", "avatar link $avatar")*/
                // int avatar = R.mipmap.icon; // set the default avatar
                val newClusterMarker = ClusterMarker(

                        mosqueLocation.position.latitude ,
                        mosqueLocation.position.longitude
                    ,
                    title,
                    snippet,
                    "default"
                )
                mClusterManager!!.addItem(newClusterMarker)
                var markerCollection: MarkerManager.Collection= mClusterManager!!.markerCollection
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


        try {

            val snippet2 = getString(R.string.you)
            val newClusterMarker2 = ClusterMarker(
                userPosition.latitude                    ,
                userPosition.longitude
                ,
                "You",
                snippet2,
                "Me"
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
        var markerCollection: MarkerManager.Collection= mClusterManager!!.markerCollection
        markerCollection.setOnMarkerClickListener(object: OnMarkerClickListener{
            override fun onMarkerClick(marker: Marker?): Boolean {
                Log.d(TAG,"you clicked")
                openDialog(marker!!)
                return true
            }

        })
        mClusterManager!!.cluster()
        setCameraView()
    }

    private fun openDialog(marker: Marker){
        // Initialize a new instance of
        val builder = AlertDialog.Builder(this@MapsActivity)

        // Set the alert dialog title
        builder.setTitle("App background color")

        // Display a message on alert dialog
        builder.setMessage("Are you want to set the app background color to RED?")

        // Set a positive button and its click listener on alert dialog
        builder.setPositiveButton("YES"){dialog, which ->
            // Do something when user press the positive button
            resetSelectedMarker()
            mSelectedMarker = marker
            calculateDirections(marker)
            dialog.dismiss()


        }


        // Display a negative button on alert dialog
        builder.setNegativeButton("No"){dialog,which ->
            Toast.makeText(applicationContext,"You are not agree.",Toast.LENGTH_SHORT).show()
        }


        // Display a neutral button on alert dialog
        builder.setNeutralButton("Cancel"){_,_ ->
            Toast.makeText(applicationContext,"You cancelled the dialog.",Toast.LENGTH_SHORT).show()
        }

        // Finally, make the alert dialog using builder
        val dialog: AlertDialog = builder.create()

        // Display the alert dialog on app interface
        dialog.show()

    }

    private fun setCameraView() {
        // Set a boundary to start


        // Set a boundary to start
        val bottomBoundary: Double = userPosition.latitude - .1
        val leftBoundary: Double = userPosition.longitude - .1
        val topBoundary: Double = userPosition.latitude + .1
        val rightBoundary: Double = userPosition.longitude + .1

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
                    polyline.color = ContextCompat.getColor(applicationContext, R.color.grey)
                    polyline.isClickable = true
                    mPolylinesData.add(PolylineData(polyline, route.legs[0]))
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
                        Log.d(TAG,"position="+location.latitude +""+location.longitude)
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



    override fun onInfoWindowClick(marker: Marker) {
        Log.d(TAG,"you clicked")
        if (marker.title.contains("Trip #") || marker.title.contains("Parcours #")) {
            val builder =
                AlertDialog.Builder(this)
            builder.setMessage(getString(R.string.open_google_map))
                .setCancelable(true)
                .setPositiveButton(
                    "Yes"
                ) { dialog, id ->
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
                .setNegativeButton(
                    "No"
                ) { dialog, id -> dialog.cancel() }
            val alert = builder.create()
            alert.show()
        } else {
            //We check if the markers is our and we hide menu to not calculate our own direction
            if (marker.snippet == "This is you" || marker.snippet == "Vous êtes ici") {
                marker.hideInfoWindow()
            } else {
                val builder =
                    AlertDialog.Builder(this)
                builder.setMessage(marker.snippet)
                    .setCancelable(true)
                    .setPositiveButton(
                        getString(R.string.yes),
                        DialogInterface.OnClickListener { dialog, id ->
                            resetSelectedMarker()
                            mSelectedMarker = marker
                            calculateDirections(marker)
                            dialog.dismiss()
                        })
                    .setNegativeButton(
                        getString(R.string.no),
                        DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
                val alert = builder.create()
                alert.show()
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
    }

    override fun onPolylineClick(polyline: Polyline) {
        val index = 0
        //Were're loopping through to see if the polyline selected is the same as the one in the
        // arrayList if there is a match the polyline will become blue

        for (polylineData in mPolylinesData) {
            Log.d(TAG, "onPolylineClick: toString: $polylineData")
            if (polyline.id == polylineData.polyline.id) {
                polylineData.polyline.color =
                    ContextCompat.getColor(applicationContext, R.color.unicef)
                polylineData.polyline.zIndex =
                    1F // gives a elevation in orfer to easily differenciate from the others
                val endpoint =
                    LatLng(
                        polylineData.leg.endLocation.lat,
                        polylineData.leg.endLocation.lng
                    )
                val marker: Marker = mMap.addMarker(
                    MarkerOptions()
                        .position(endpoint)
                        .title(getString(R.string.trip) + index)
                        .snippet(getString(R.string.duree) + polylineData.leg.duration)
                )
                marker.showInfoWindow()
                mTripMarkers.add(marker)
            } else {
                polylineData.polyline.color =
                    ContextCompat.getColor(applicationContext, R.color.grey)
                polylineData.polyline.zIndex = 0F
            }
        }
    }


}