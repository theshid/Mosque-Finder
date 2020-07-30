package com.shid.mosquefinder

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.SphericalUtil

import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.collections.MarkerManager
import com.google.maps.model.PlacesSearchResult
import com.irozon.sneaker.Sneaker
import com.shid.mosquefinder.Data.Model.ClusterMarker
import com.shid.mosquefinder.Data.Model.Mosque
import com.shid.mosquefinder.Data.Model.User
import com.shid.mosquefinder.Ui.Base.MapViewModelFactory
import com.shid.mosquefinder.Ui.Main.View.AuthActivity
import com.shid.mosquefinder.Ui.Main.View.SplashActivity
import com.shid.mosquefinder.Ui.Main.ViewModel.MapViewModel
import com.shid.mosquefinder.Utils.*
import com.shid.mosquefinder.Utils.Network.Event
import com.shid.mosquefinder.Utils.Network.NetworkEvents
import kotlinx.android.synthetic.main.activity_maps.btn_reset_map
import kotlinx.android.synthetic.main.activity_maps.startActivityButton
import kotlinx.android.synthetic.main.dialog_layout.*
import java.math.BigDecimal
import java.math.RoundingMode

class MapsActivity2 : AppCompatActivity(), OnMapReadyCallback, FirebaseAuth.AuthStateListener {

    private lateinit var mMap: GoogleMap

    private lateinit var mapViewModel: MapViewModel

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 999
        private const val TAG = "MapsActivity"
        var userPosition: LatLng? = null
        lateinit var position: LatLng
    }

    private var mClusterManager: ClusterManager<ClusterMarker>? = null
    private var mClusterMarkers: MutableList<ClusterMarker> = ArrayList()
    private var mClusterManagerRenderer: MyClusterManagerRenderer? = null
    private var markerCollection: MarkerManager.Collection? = null
    private var markerCollectionForClusters: MarkerManager.Collection? = null
    private var markerCollectionForClusters2: MarkerManager.Collection? = null
    private var mMosqueList: MutableList<Mosque> = ArrayList()

    private lateinit var mMapBoundary: LatLngBounds

    private var previousSate = true

    private val firebaseAuth = FirebaseAuth.getInstance()
    private var googleSignInClient: GoogleSignInClient? = null
    private var user: User? = null

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps2)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        getUserPositionFromOtherActivities()
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        user = getUserFromIntent()

        savedInstanceState?.let {
            previousSate = it.getBoolean("LOST_CONNECTION")
        }



        NetworkEvents.observe(this, Observer {
            if (it is Event.ConnectivityEvent)
                handleConnectivityChange()
        })
        setUpLocationListener()
        setupViewModel()
        initGoogleSignInClient()
        setMessageForToast(user!!)
        btnClickListeners()
        testLocation()


    }

    private fun testLocation(){
        AppLocationProvider().getLocation(this, object : AppLocationProvider.LocationCallBack {
            override fun locationResult(location: Location?) {
                if (location != null) {
                    position = LatLng(location.latitude,location.longitude)
                    Log.d(TAG, "new position:$position")
                    Log.d(TAG,"accuracy:"+location.accuracy)
                } // use location, this might get called in a different thread if a location is a last known location. In that case, you can post location on main thread
            }
        })
    }

    override fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            goToAuthInActivity()
        }
    }

    private fun signOut() {
        singOutFirebase()
        signOutGoogle()
    }

    private fun singOutFirebase() {
        firebaseAuth.signOut()
    }

    private fun signOutGoogle() {
        googleSignInClient!!.signOut()
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(this)
    }

    override fun onStop() {
        super.onStop()
        firebaseAuth.removeAuthStateListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId === R.id.sign_out_button) {
            signOut()
            return true
        }
        return super.onOptionsItemSelected(menuItem)
    }


    private fun setMessageForToast(user: User) {
        val message = "You are logged in as: " + user.name
        // messageTextView!!.text = message
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun goToAuthInActivity() {
        val intent = Intent(this@MapsActivity2, AuthActivity::class.java)
        startActivity(intent)
    }

    private fun getUserFromIntent(): User? {
        return intent.getSerializableExtra(Common.USER) as com.shid.mosquefinder.Data.Model.User
    }

    private fun initGoogleSignInClient() {
        val googleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
    }

    private fun handleConnectivityChange() {
        if (ConnectivityStateHolder.isConnected && !previousSate) {
            // showSnackBar(textView, "The network is back !")
            Sneaker.with(this) // Activity, Fragment or ViewGroup
                .setTitle("Connected!!")
                .setMessage("The network is back !")
                .sneakSuccess()
        }

        if (!ConnectivityStateHolder.isConnected && previousSate) {
            // showSnackBar(textView, "No Network !")
            Sneaker.with(this) // Activity, Fragment or ViewGroup
                .setTitle("Connection lost")
                .setMessage("No Network!")
                .sneakError()
        }

        previousSate = ConnectivityStateHolder.isConnected
    }

    override fun onResume() {
        super.onResume()
        handleConnectivityChange()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("LOST_CONNECTION", previousSate)
        super.onSaveInstanceState(outState)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        Handler().postDelayed(kotlinx.coroutines.Runnable {
            //anything you want to start after 3s

            addMapMarkers()


            // addUserMarker()

        }, 2000)


    }


    private fun getUserPositionFromOtherActivities() {
        if (AuthActivity.userPosition != null) {
            userPosition = AuthActivity.userPosition!!
        } else if (SplashActivity.userPosition != null) {
            userPosition = SplashActivity.userPosition!!
        }
    }

    @SuppressLint("MissingPermission")
    fun setUpLocationListener() {
        val fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(application)
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
                        Log.d(Common.TAG, "position=" + location.latitude + "" + location.longitude + location.provider)
                    }
                    // Few more things we can do here:
                    // For example: Update the location of user on server
                }
            },
            Looper.myLooper()
        )
    }

    private fun btnClickListeners() {
        btn_reset_map.setOnClickListener {
            addMapMarkers()

        }
        startActivityButton.setOnClickListener {
            mosquePromptDialog()
        }

    }

    private fun mosquePromptDialog() {
        MaterialDialog(this).show {
            title(text = "Mosque Finder")
            message(text = "Are you sure you want to add a mosque on this location?")
            positiveButton(text = "Yes")
            negativeButton(text = "Cancel")
            positiveButton(text = "Yes") { dialog ->
                dialog.cancel()
                userPosition?.let { mosqueInputDialog(it) }

            }
            negativeButton(text = "Cancel") { dialog ->
                dialog.cancel()
            }
            icon(R.drawable.logo)
        }
    }

    private fun mosqueInputDialog(userPosition: LatLng) {

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

        mapViewModel.inputMosqueInDatabase(userMosqueInput)
    }


    private fun setupViewModel() {
        mapViewModel = ViewModelProvider(
            this,
            MapViewModelFactory(Common.googleApiService, application)
        ).get(MapViewModel::class.java)
    }


    private fun addMapMarkers() {
        resetMap()

        if (mClusterManager == null) {
            mClusterManager = ClusterManager(this, mMap)
        }
        if (mClusterManagerRenderer == null) {
            mClusterManagerRenderer = MyClusterManagerRenderer(
                this,
                mClusterManager!!,
                mMap
            )
            mClusterManager!!.renderer = mClusterManagerRenderer
        }
        getMosquesFromGoogleMap()
        getMosqueFromFirebase()
        addFirebaseMarkersToClusterManager()
        addUserMarker()
        markersClickListeners()
        for (i in mClusterMarkers){
            Log.d("Map",i.title)
        }
        mClusterManager!!.cluster()
        setCameraView()
    }

    private fun setCameraView() {
        // Set a boundary to start
        val bottomBoundary: Double = userPosition!!.latitude - .009
        val leftBoundary: Double = userPosition!!.longitude - .009
        val topBoundary: Double = userPosition!!.latitude + .009
        val rightBoundary: Double = userPosition!!.longitude + .009

        mMapBoundary = LatLngBounds(
            LatLng(bottomBoundary, leftBoundary),
            LatLng(topBoundary, rightBoundary)
        )
        try {
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0))
        } catch (ise: IllegalStateException) {
            mMap.setOnMapLoadedCallback(GoogleMap.OnMapLoadedCallback {
                mMap.moveCamera(
                    CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0)
                )
            })
        }
    }

    private fun calculateDistanceBetweenUserAndMosque(position: LatLng): Double {
        val distanceInKm:Double = SphericalUtil.computeDistanceBetween(userPosition, position) * 0.001
        return BigDecimal(distanceInKm).setScale(2, RoundingMode.HALF_EVEN).toDouble()
    }

    private fun addFirebaseMarkersToClusterManager() {
        if (mMosqueList != null) {
            for (mosqueLocation in mMosqueList) {
                Log.d(
                    "Map",
                    "addMapMarkers: location: " + mosqueLocation.position.toString()
                )
                try {
                    val snippet =
                        calculateDistanceBetweenUserAndMosque(
                            LatLng(
                                mosqueLocation.position.latitude,
                                mosqueLocation.position.longitude
                            )).toString() +
                                getString(R.string.km) + "from your position "
                    val title = mosqueLocation.name

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
                    mClusterMarkers.add(newClusterMarker)
                    mClusterManager!!.addItem(newClusterMarker)
                    markerCollectionForClusters2 = mClusterManager!!.markerCollection


                } catch (e: NullPointerException) {
                    Log.e(
                        "Map",
                        "addMapMarkers: NullPointerException: " + e.message
                    )
                }
            }
        }
    }

    private fun addUserMarker() {
        //userPosition = mapViewModel.getUserPosition()!!
        try {

            val snippet2 = getString(R.string.you)
            val newClusterMarker2 =
                ClusterMarker(
                    userPosition!!.latitude,
                    userPosition!!.longitude
                    ,
                    "You",
                    snippet2,
                    "Me",
                    false
                )
            mClusterManager!!.addItem(newClusterMarker2)
            mClusterMarkers.add(newClusterMarker2)


        } catch (e: NullPointerException) {
            Log.e(
                "Map",
                "addMapMarkers: NullPointerException: " + e.message
            )
        }
    }

    private fun markersClickListeners() {
        markerCollectionForClusters = mClusterManager!!.markerCollection

        markerCollectionForClusters?.setOnMarkerClickListener { marker ->
            Log.d(TAG, "you clicked")
            //openDialog(marker!!)
            marker.showInfoWindow()
            true
        }

        markerCollectionForClusters?.setOnInfoWindowClickListener { marker ->
            for (i in mClusterMarkers) {
                //We check if the marker comes from Google Place or FireStore
                if (i.isMarkerFromGooglePlace && i.title == marker.title) {
                    showDirectionInGoogleMapDialog(marker)

                } else if (i.title == marker.title && !i.isMarkerFromGooglePlace) {
                    showOptionsDialog(marker)
                }
            }
        }
    }

    private fun showOptionsDialog(marker: Marker) {
        when {
            marker.title.contains("You") -> {
                marker.showInfoWindow()
            }
            marker.title.contains("Trip") -> {
                showDirectionInGoogleMapDialog(marker)
            }
            else -> {
                showOptionsForUserInputMosquesDialog(marker)
                //dialogOpenGoogleMap(marker)
                //dialogForRoute(marker)
            }
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

    private fun reportMosquePosition(marker: Marker) {
        mapViewModel.reportFalseMosqueLocation(marker)
    }

    private fun confirmMosquePosition(marker: Marker) {
        mapViewModel.confirmMosqueLocation(marker)
    }

    private fun showDirectionInGoogleMapDialog(marker: Marker) {
        MaterialDialog(this).show {
            title(text = "Mosque Finder")
            message(text = "Show Directions in Google Map?")
            icon(R.drawable.logo)


            positiveButton(text = "Yes") { dialog ->
                dialog.cancel()
                intentToGoogleMap(marker)
            }
            negativeButton(text = "No") { dialog ->
                dialog.cancel()
                Toast.makeText(applicationContext, "Window closed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun intentToGoogleMap(marker: Marker) {
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

    private fun resetMap() {
        mMap.clear()
        if (mClusterManager != null) {
            mClusterManager!!.clearItems()
        }
        if (mClusterMarkers.size > 0) {
            mClusterMarkers.clear()
            mClusterMarkers = java.util.ArrayList()
        }
    }

    private fun getMosqueFromFirebase() {
        mMosqueList = mapViewModel.getUsersMosqueFromRepository()
    }

    private fun getMosquesFromGoogleMap() {

        mapViewModel.getGoogleMapMosqueFromRepository()?.observe(this, Observer {
            val places: List<PlacesSearchResult>? = it?.results
            if (places != null) {
                for (i in places.indices) {
                    val googlePlace = it!!.results!![i]
                    val lat = googlePlace.geometry!!.location!!.lat
                    val lng = googlePlace.geometry!!.location!!.lng
                    val placeName = googlePlace.name

                    Log.d(
                        "Map",
                        "addMapMarkers: location: " + googlePlace.geometry.location.toString()
                    )
                    Log.d(
                        "Map",
                        "addMapMarkers: name: " + googlePlace.name
                    )
                    try {
                        val snippet =
                            calculateDistanceBetweenUserAndMosque(LatLng(lat,lng)).toString() +
                                    getString(R.string.km) + "from your position "
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
                        for (i in mClusterMarkers) {
                            Log.d(TAG, i.title)
                        }


                    } catch (e: NullPointerException) {
                        Log.e(
                            "Map",
                            "addMapMarkers: NullPointerException: " + e.message
                        )
                    }

                }
            }
           /* markerCollectionForClusters?.setOnMarkerClickListener { marker ->
                Log.d(TAG, "you clicked")
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

            })*/
            // mClusterManager!!.cluster()
        })

        //mClusterManager?.cluster()
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
                            //getUserPosition()
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
}