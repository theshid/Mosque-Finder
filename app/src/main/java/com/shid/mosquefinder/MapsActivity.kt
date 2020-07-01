package com.shid.mosquefinder

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener
import com.google.android.gms.maps.GoogleMap.OnPolylineClickListener
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import com.google.firebase.firestore.*
import com.google.maps.GeoApiContext
import com.google.maps.android.clustering.ClusterManager
import com.shid.mosquefinder.Model.ClusterMarker
import com.shid.mosquefinder.Model.Mosque
import com.shid.mosquefinder.Model.PolylineData
import com.shid.mosquefinder.Utils.MyClusterManagerRenderer
import com.shid.mosquefinder.Utils.PermissionUtils

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
    View.OnClickListener,
    OnInfoWindowClickListener, OnPolylineClickListener{
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 999
        private val TAG = "MapsActivity"
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
    private val mClusterMarkers: MutableList<ClusterMarker> = ArrayList()
    private val mPolylinesData: MutableList<PolylineData> = ArrayList()
    private val mTripMarkers: MutableList<Marker> = ArrayList()
    private lateinit var mSelectedMarker: Marker


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
                com.shid.swissaid.UI.GeolocationActivity.TAG,
                "addMapMarkers: location: " + userLocation.getGeo_point().toString()
            )
            try {
                var snippet = ""
                snippet = if (userLocation.getUser().getUser_id()
                        .equals(FirebaseAuth.getInstance().getUid())
                ) {
                    getString(R.string.you)
                } else {
                    getString(R.string.determine_route) + " " + userLocation.getUser()
                        .getUsername() + "?"
                }
                val avatar: String = userLocation.getUser().getImageUrl()
                Log.d("Avatar", "avatar link $avatar")
                // int avatar = R.mipmap.icon; // set the default avatar
                val newClusterMarker = ClusterMarker(
                    LatLng(
                        userLocation.getGeo_point().getLatitude(),
                        userLocation.getGeo_point().getLongitude()
                    ),
                    userLocation.getUser().getUsername(),
                    snippet,
                    avatar,
                    userLocation.getUser()
                )
                mClusterManager.addItem(newClusterMarker)
                mClusterMarkers.add(newClusterMarker)
            } catch (e: NullPointerException) {
                Log.e(
                    com.shid.swissaid.UI.GeolocationActivity.TAG,
                    "addMapMarkers: NullPointerException: " + e.message
                )
            }
        }
        mClusterManager.cluster()
        setCameraView()
    }

    private fun setCameraView() {
        TODO("Not yet implemented")
    }

    private fun resetMap() {
        TODO("Not yet implemented")
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

    override fun onInfoWindowClick(p0: Marker?) {
        TODO("Not yet implemented")
    }

    override fun onPolylineClick(p0: Polyline?) {
        TODO("Not yet implemented")
    }
}