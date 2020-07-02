package com.shid.mosquefinder

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
import com.google.maps.internal.PolylineEncoding
import com.google.maps.model.DirectionsResult
import com.shid.mosquefinder.Model.ClusterMarker
import com.shid.mosquefinder.Model.Mosque
import com.shid.mosquefinder.Model.PolylineData
import com.shid.mosquefinder.Utils.MyClusterManagerRenderer
import com.shid.mosquefinder.Utils.PermissionUtils
import kotlinx.coroutines.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
    View.OnClickListener,
    OnInfoWindowClickListener, OnPolylineClickListener {
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 999
        private const val TAG = "MapsActivity"
    }

    private lateinit var mMap: GoogleMap
    private lateinit var database: FirebaseFirestore
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mMosqueListEventListener: ListenerRegistration
    private val mMosqueList: MutableList<Mosque> = ArrayList()

    private lateinit var mMapView: MapView
    private lateinit var mGeoApiContext: GeoApiContext
    private lateinit var mMapBoundary: LatLngBounds
    private lateinit var mClusterManager: ClusterManager<ClusterMarker>
    private lateinit var mClusterManagerRenderer: MyClusterManagerRenderer
    private var mClusterMarkers: MutableList<ClusterMarker> = ArrayList()
    private var mPolylinesData: MutableList<PolylineData> = ArrayList()
    private val mTripMarkers: MutableList<Marker> = ArrayList()
    private var mSelectedMarker: Marker? = null
    private lateinit var userPosition: LatLng
    private var mapJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + mapJob)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        database = FirebaseFirestore.getInstance()
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        getTotalMosques()
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
                        mMosqueList.add(mosque)
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
        addMapMarkers()
        mMap.setOnInfoWindowClickListener(this)
        mMap.setOnPolylineClickListener(this)
    }

    private fun addMapMarkers() {
        resetMap()
        if (mClusterManager == null) {
            mClusterManager =
                ClusterManager(this.applicationContext, mMap)
        }
        if (mClusterManagerRenderer == null) {
            mClusterManagerRenderer = MyClusterManagerRenderer(
                this,
                mClusterManager,
                mMap
            )
            mClusterManager.setRenderer(mClusterManagerRenderer)
        }
        for (mosqueLocation in mMosqueList) {
            Log.d(
                "Map",
                "addMapMarkers: location: " + mosqueLocation.position.toString()
            )
            try {
                val snippet = getString(R.string.determine_route) + " " + mosqueLocation.name + "?"

                /*val avatar: String = mosqueLocation
                Log.d("Avatar", "avatar link $avatar")*/
                // int avatar = R.mipmap.icon; // set the default avatar
                val newClusterMarker = ClusterMarker(
                    LatLng(
                        mosqueLocation.position.latitude,
                        mosqueLocation.position.longitude
                    ),
                    mosqueLocation.name,
                    snippet,
                    "default"
                )
                mClusterManager.addItem(newClusterMarker)
                mClusterMarkers.add(newClusterMarker)
            } catch (e: NullPointerException) {
                Log.e(
                    "Map",
                    "addMapMarkers: NullPointerException: " + e.message
                )
            }
        }
        mClusterManager.cluster()
        setCameraView()
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
        if (mMap != null) {
            mMap.clear()
            if (mClusterManager != null) {
                mClusterManager.clearItems()
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
                    mPolylinesData = java.util.ArrayList()
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
        if (mMap == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return

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

    override fun onClick(v: View?) {
        TODO("Not yet implemented")
    }

    override fun onInfoWindowClick(marker: Marker) {
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
                polylineData.polyline.color = ContextCompat.getColor(applicationContext, R.color.unicef)
                polylineData.polyline.zIndex = 1F // gives a elevation in orfer to easily differenciate from the others
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
                polylineData.polyline.color = ContextCompat.getColor(applicationContext, R.color.grey)
                polylineData.polyline.zIndex = 0F
            }
        }
    }
}