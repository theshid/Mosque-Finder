package com.shid.mosquefinder.Ui.Main.View

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.SphericalUtil
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.collections.MarkerManager
import com.google.maps.model.PlacesSearchResult
import com.irozon.sneaker.Sneaker
import com.shid.mosquefinder.ConnectivityStateHolder
import com.shid.mosquefinder.Data.Model.ClusterMarker
import com.shid.mosquefinder.Data.Model.Mosque
import com.shid.mosquefinder.Data.Model.Pojo.GoogleMosque
import com.shid.mosquefinder.Data.Model.User
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Ui.Base.MapViewModelFactory
import com.shid.mosquefinder.Ui.Main.ViewModel.MapViewModel
import com.shid.mosquefinder.Utils.*
import com.shid.mosquefinder.Utils.Network.Event
import com.shid.mosquefinder.Utils.Network.NetworkEvents
import fr.quentinklein.slt.LocationTracker
import fr.quentinklein.slt.ProviderError
import kotlinx.android.synthetic.main.activity_maps2.*
import kotlinx.android.synthetic.main.dialog_layout.*
import uk.co.markormesher.android_fab.FloatingActionButton
import uk.co.markormesher.android_fab.SpeedDialMenuAdapter
import uk.co.markormesher.android_fab.SpeedDialMenuItem
import uk.co.markormesher.android_fab.SpeedDialMenuOpenListener
import java.math.BigDecimal
import java.math.RoundingMode

class MapsActivity2 : AppCompatActivity(), OnMapReadyCallback, FirebaseAuth.AuthStateListener
{

    private lateinit var mMap: GoogleMap

    private lateinit var mapViewModel: MapViewModel

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 999
        private const val TAG = "MapsActivity"
        var userPosition: LatLng? = null
        var newUserPosition: LatLng? = null
        private const val RQ_SEARCH = 101


        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    //Markers
    private var mClusterManager: ClusterManager<ClusterMarker>? = null
    private var mClusterMarkers: MutableList<ClusterMarker> = ArrayList()
    private var mClusterManagerRenderer: MyClusterManagerRenderer? = null
    private var markerCollection: MarkerManager.Collection? = null
    private var markerManager:MarkerManager ?= null
    private var markerCollectionForClusters: MarkerManager.Collection? = null
    private var markerCollectionForClusters2: MarkerManager.Collection? = null
    private var clusterMarkerFromIntent: ClusterMarker? = null

    private var mMosqueList: MutableList<Mosque> = ArrayList()
    private var sortedMosqueList: List<ClusterMarker> = ArrayList()
    private var mGoogleMosqueList: MutableList<GoogleMosque> = ArrayList()
    private var mNigerGoogleMosqueList: MutableList<GoogleMosque> = ArrayList()


    private lateinit var mMapBoundary: LatLngBounds

    private var previousSate = true

    private val firebaseAuth = FirebaseAuth.getInstance()
    private var googleSignInClient: GoogleSignInClient? = null
    private var user: User? = null

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    val locationTracker = LocationTracker()

    private var buttonIcon = 0
    private var speedDialSize = 3
    private val speedDialSizeOptions = arrayOf(
        Pair("None", 0),
        Pair("1 item", 1),
        Pair("2 items", 2),
        Pair("3 items", 3)

    )
    private val speedDialMenuAdapter = object : SpeedDialMenuAdapter() {
        override fun getCount(): Int = speedDialSizeOptions[speedDialSize].second

        override fun getMenuItem(context: Context, position: Int): SpeedDialMenuItem =
            when (position) {
                0 -> SpeedDialMenuItem(context, R.drawable.ic_refresh, getString(R.string.options_1))
                1 -> SpeedDialMenuItem(context, R.drawable.ic_add, getString(R.string.options_2))
                2 -> SpeedDialMenuItem(context, R.drawable.ic_my_location, getString(R.string.options_3))
                else -> throw IllegalArgumentException("No menu item: $position")
            }

        override fun getBackgroundColour(position: Int): Int {

            return Color.argb(255, 255, 255, 255)
        }

        override fun onMenuItemClick(position: Int): Boolean {
            if (position == 0) {
                if (newUserPosition != null){
                    addMapMarkers(newUserPosition!!)
                }else{
                    addMapMarkers(userPosition!!)
                }

            } else if (position == 1) {

                setCameraView()
                Handler().postDelayed(kotlinx.coroutines.Runnable {
                    //anything you want to start after 3s

                    mosquePromptDialog()


                    // addUserMarker()

                }, 2000)
            } else{
                setCameraView()
            }
            return true
        }

        override fun onPrepareItemLabel(context: Context, position: Int, label: TextView) {
            // make the first item bold if there are multiple items
            // (this isn't a design pattern, it's just to demo the functionality)
            if ( speedDialSize > 0) {
                label.setTypeface(label.typeface, Typeface.BOLD)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    label.setTextColor(resources.getColor(R.color.colorWhite,context.theme))
                } else{
                    label.setTextColor(resources.getColor(R.color.colorWhite))
                }
            }
        }

        // rotate the "+" icon only
        override fun fabRotationDegrees(): Float = if (buttonIcon == 0) 135F else 0F
    }





    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps2)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        getUserPositionFromOtherActivities()
        setDrawerLayout()
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        user = getUserFromIntent()

        savedInstanceState?.let {
            previousSate = it.getBoolean("LOST_CONNECTION")
        }

        setNetworkMonitor()
        setUpLocationListener()
        setUpNewLocationListener()
        setupViewModel()
        setTransparentStatusBar()
        initGoogleSignInClient()
        setMessageForToast(user!!)
        btnClickListeners()
        setObserver()


    }

    private fun setUpNewLocationListener() {
        locationTracker.addListener(object : LocationTracker.Listener {

            override fun onLocationFound(location: Location) {
                newUserPosition = LatLng(location.latitude, location.longitude)
                Log.d(TAG, "new position:" + newUserPosition)
                Log.d(TAG, "accuracy" + location.accuracy)
            }

            override fun onProviderError(providerError: ProviderError) {
            }

        });
    }


    private fun setNetworkMonitor() {
        NetworkEvents.observe(this, Observer {
            if (it is Event.ConnectivityEvent)
                handleConnectivityChange()
        })
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawer(navigationView)
        } else {
            moveTaskToBack(true)
        }
    }


    private fun setDrawerLayout() {
        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_quotes -> {
                    //AnalyticsUtil.logEvent(this, AnalyticsUtil.Value.MENU_GLOBAL_CASES)
                    goToQuotes()
                }

                R.id.nav_share -> {
                    //AnalyticsUtil.logEvent(this, AnalyticsUtil.Value.MENU_SHARE)
                    goToShareApp()
                }
                R.id.nav_feedback -> {
                    //AnalyticsUtil.logEvent(this, AnalyticsUtil.Value.MENU_FEEDBACK)
                    goToFeedback()
                }
                R.id.nav_credits -> {
                    //AnalyticsUtil.logEvent(this, AnalyticsUtil.Value.MENU_CREDITS)
                    goToCredits()
                }
                R.id.nav_contact -> {
                    //AnalyticsUtil.logEvent(this, AnalyticsUtil.Value.MENU_CONTACT)
                    sendEmail()
                }
                R.id.nav_exit -> {
                    //AnalyticsUtil.logEvent(this, AnalyticsUtil.Value.MENU_CONTACT)
                    logOutDialog()
                }
            }
            drawerLayout.closeDrawer(navigationView)
            true
        }
    }

    private fun goToQuotes() {
        val intent = Intent(this, QuotesActivity::class.java)
        startActivity(intent)
    }

    private fun goToShareApp() {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text))
        shareIntent.type = "text/plain"
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_app)))
    }

    private fun goToFeedback() {
        startActivity(Intent(this, FeedbackActivity::class.java))
    }

    private fun goToCredits() {
        startActivity(Intent(this, CreditsActivity::class.java))
    }

    private fun sendEmail() {
        Toast.makeText(this, getString(R.string.email_message), Toast.LENGTH_LONG).show()
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.data = Uri.parse("mailto:")
        emailIntent.type = "message/rfc822"
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("mosquefinder@gmail.com"))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello!")

        try {
            startActivity(Intent.createChooser(emailIntent, getString(R.string.send_mail)))
        } catch (ex: ActivityNotFoundException) {
            // Empty
        }
    }

    private fun setObserver() {
        mapViewModel.retrieveStatusMsg().observe(this, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    Toast.makeText(this, it.data, Toast.LENGTH_LONG).show()
                }
                Status.LOADING -> {

                }
                Status.ERROR -> {
                    //Handle Error

                    Toast.makeText(this, it.data, Toast.LENGTH_LONG).show()
                    Log.d(TAG, it.message)
                }
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


    private fun setMessageForToast(user: User) {
        //val message = "You are logged in as: " + user.name
        // messageTextView!!.text = message
        Toast.makeText(this,String.format(resources.getString(R.string.toast_log_in_msg,user.name)), Toast.LENGTH_LONG).show()
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
                .setTitle(getString(R.string.sneaker_connected))
                .setMessage(getString(R.string.sneaker_msg_network))
                .sneakSuccess()
        }

        if (!ConnectivityStateHolder.isConnected && previousSate) {
            // showSnackBar(textView, "No Network !")
            Sneaker.with(this) // Activity, Fragment or ViewGroup
                .setTitle(getString(R.string.sneaker_disconnected))
                .setMessage(getString(R.string.sneaker_msg_network_lost))
                .sneakError()
        }

        previousSate = ConnectivityStateHolder.isConnected
    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        handleConnectivityChange()
        if (fusedLocationProviderClient != null){
            setUpLocationListener()
        }
        locationTracker.startListening(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("LOST_CONNECTION", previousSate)
        super.onSaveInstanceState(outState)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap


        mMap?.apply {
            try {
                val success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        this@MapsActivity2, R.raw.map_style
                    )
                )
                if (!success) {
                    Log.e(this.javaClass.simpleName, "Style parsing failed.")
                }
            } catch (e: Resources.NotFoundException) {
                Log.e(this.javaClass.simpleName, "Can't find style. Error: ", e)
            }
        }




        if (newUserPosition != null) {
            Handler().postDelayed(kotlinx.coroutines.Runnable {
                //anything you want to start after 3s


                addMapMarkers(newUserPosition!!)



                // addUserMarker()

            }, 2000)

        }else if (userPosition != null){
            Handler().postDelayed(kotlinx.coroutines.Runnable {
                //anything you want to start after 3s

                addMapMarkers(userPosition!!)
                /*markerUser = mMap.addMarker(
                    MarkerOptions()
                        .position(userPosition!!)
                        .draggable(true)
                        .title("usee this marker to display position")
                        .zIndex(1.0f)
                )
                mMap.setOnMarkerDragListener(this)*/


                // addUserMarker()

            }, 2000)
        } else{
            //Toast.makeText(this,"Please activate Internet",Toast.LENGTH_LONG).show()
            val rootView = findViewById<View>(android.R.id.content)
            Snackbar.make(rootView,getString(R.string.offline),Snackbar.LENGTH_LONG).show()


        }






    }


    private fun getUserPositionFromOtherActivities() {
        if (AuthActivity.userPosition != null) {
            userPosition = AuthActivity.userPosition!!
        } else if (SplashActivity.userPosition != null) {
            userPosition = SplashActivity.userPosition!!
        }
    }

    override fun onPause() {
        super.onPause()
        if (fusedLocationProviderClient != null){
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
        locationTracker.stopListening()
    }



    @SuppressLint("MissingPermission")
    fun setUpLocationListener() {
         fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(application)
        // for getting the current location update after every 2 seconds with high accuracy
        val locationRequest = LocationRequest().setInterval(10000).setFastestInterval(4000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)


        locationCallback =  object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                for (location in locationResult.locations) {
                    /* latTextView.text = location.latitude.toString()
                     lngTextView.text = location.longitude.toString()*/
                    SplashActivity.userPosition = LatLng(location.latitude, location.longitude)
                    Log.d("Splash", "position=" + location.latitude + "" + location.longitude)
                    Log.d("Splash","accuracy:"+location.accuracy)
                }
                // Few more things we can do here:
                // For example: Update the location of user on server
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,locationCallback,
            Looper.myLooper())
    }

    private fun btnClickListeners() {
        card_first.setOnClickListener {
            setCameraView(sortedMosqueList[1].position)
        }

        card_second.setOnClickListener {
            setCameraView(sortedMosqueList[2].position)
        }

        card_third.setOnClickListener {
            setCameraView(sortedMosqueList[3].position)
        }


        /* with(lava_fab) {
             //setLavaBackgroundResColor(R.color.fab_color)
             setParentOnClickListener { lava_fab.trigger() }
             setChildOnClickListener(Child.TOP) {
                 setCameraView()
                 Handler().postDelayed(kotlinx.coroutines.Runnable {
                     //anything you want to start after 3s

                     mosquePromptDialog()


                     // addUserMarker()

                 }, 2000)


             }
             setChildOnClickListener(Child.LEFT) { // some action }
                 addMapMarkers()
                 *//* enableShadow()
                 setParentIcon(R.drawable.ic_parent)
                 setChildIcon(Child.TOP, R.drawable.ic_child_top)
                 setChildIcon(Child.LEFT, R.drawable.ic_child_left)*//*
            }
        }*/

        menuButton.setOnClickListener {
            //AnalyticsUtil.logEvent(this, AnalyticsUtil.Value.MENU_OPEN)
            drawerLayout.openDrawer(navigationView)
        }

        searchText.setOnClickListener {
            //AnalyticsUtil.logEvent(this, AnalyticsUtil.Value.HOME_OPEN_SEARCH)
            goToSearch()
        }

        fab.speedDialMenuAdapter = speedDialMenuAdapter
        fab.contentCoverEnabled = true
        fab.setContentCoverColour(0xcc000000.toInt())

        fab.setOnSpeedDialMenuOpenListener(object : SpeedDialMenuOpenListener {

            override fun onOpen(floatingActionButton: FloatingActionButton) {

            }
        })


    }

    private fun goToSearch() {
        val intent = Intent(this, SearchActivity::class.java)
        startActivityForResult(intent, RQ_SEARCH)
    }

    private fun mosquePromptDialog() {
        MaterialDialog(this).show {
            title(text = getString(R.string.title_dialog))
            message(text = getString(R.string.dialog_prompt_location))
            positiveButton(text = getString(R.string.yes)) { dialog ->
                dialog.cancel()
                //userPosition?.let { mosqueInputDialog(it) }

                prepareMapForInput()

            }
            negativeButton(text = getString(R.string.cancel)) { dialog ->
                dialog.cancel()
            }
            icon(R.drawable.logo2)
        }
    }

    private fun prepareMapForInput(){
        resetMap()
        Toast.makeText(this,getString(R.string.toast_marker),Toast.LENGTH_LONG).show()
        addSingleMarker()

    }

    private fun addSingleMarker(){
        markerManager = MarkerManager(mMap)
        markerCollection = markerManager!!.newCollection()
        markerCollection!!.addMarker(MarkerOptions()
            .position(userPosition!!)
            .draggable(true)
            .zIndex(1.0f))
        var listener:GoogleMap.OnMarkerDragListener = object : GoogleMap.OnMarkerDragListener{
            override fun onMarkerDragEnd(marker: Marker?) {
                if (marker != null) {
                    Log.d(TAG,"onMarkerDragEnd"+ marker.position)
                }

            }

            override fun onMarkerDragStart(p0: Marker?) {
                Log.d(TAG,"onMarkerDragStart")
            }

            override fun onMarkerDrag(p0: Marker?) {
                Log.d(TAG,"onMarkerDragStart")
            }
        }

        markerCollection!!.setOnMarkerDragListener(listener)
        markerCollection!!.setOnMarkerClickListener(object : GoogleMap.OnMarkerClickListener{
            override fun onMarkerClick(marker: Marker?): Boolean {
                //userPosition?.let { mosqueInputDialog(it) }
                if (marker != null) {
                    Log.d(TAG,"onMarkerClickListener"+ marker.position)
                }
                marker?.let { mosqueInputDialog(LatLng(it.position.latitude,it.position.longitude)) }
                return true
            }

        })
    }

    private fun mosqueInputDialog(userPosition: LatLng) {

        MaterialDialog(this).show {
            input(hint = getString(R.string.dialog_input_title))
            // input(inputType = type)
            title(text = getString(R.string.title_dialog))
            message(text = getString(R.string.dialog_prompt_location))
            positiveButton(text = getString(R.string.dialog_save)) { dialog ->
                val inputField = dialog.getInputField().text.toString()

                dialog.cancel()
                saveMosqueInputInDatabase(userPosition, inputField)
                addMapMarkers(newUserPosition!!)
            }
            negativeButton(text = getString(R.string.cancel)) { dialog ->
                dialog.cancel()
                addMapMarkers(newUserPosition!!)
            }
            icon(R.drawable.logo2)
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

        mapViewModel.setUpNewLocationListener()?.observe(this, Observer {
            if (it != null) {
                newUserPosition = it

            }

        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                clusterMarkerFromIntent = data.getParcelableExtra("search_result") as ClusterMarker

                val latLngNow = clusterMarkerFromIntent?.position

                // Set a boundary to start

                val bottomBoundary: Double = latLngNow!!.latitude - .009
                val leftBoundary: Double = latLngNow.longitude - .009
                val topBoundary: Double = latLngNow.latitude + .009
                val rightBoundary: Double = latLngNow.longitude + .009

                mMapBoundary = LatLngBounds(
                    LatLng(bottomBoundary, leftBoundary),
                    LatLng(topBoundary, rightBoundary)
                )
                try {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0))
                } catch (ise: IllegalStateException) {
                    mMap.setOnMapLoadedCallback(GoogleMap.OnMapLoadedCallback {
                        mMap.moveCamera(
                            CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0)
                        )
                    })
                }


                /*val location = CameraUpdateFactory.newLatLngZoom(
                    latLngNow, ZOOM_MAP
                )

                mMap?.animateCamera(location)*/
            }
        }


    }

    private fun addMapMarkers(userLocation: LatLng) {
        resetMap()
        /*mMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener{
            override fun onMarkerDragEnd(p0: Marker?) {
                Log.d(TAG,"onMarkerDragEnd")
            }

            override fun onMarkerDragStart(p0: Marker?) {
                Log.d(TAG,"onMarkerDragStart")
            }

            override fun onMarkerDrag(p0: Marker?) {
                Log.d(TAG,"onMarkerDragStart")
            }
        })*/


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
        //getMosquesFromGoogleMap()
        getMosqueFromFirebase()
        if (getCountryCode(applicationContext) == "gh"){
            getGoogleMosqueFromFirebase()
            addGoogleFirebaseMarkersToClusterManager(userLocation)
        } else if (getCountryCode(applicationContext) == "ne"){
            getNigerGoogleMosqueFromFirebase()
            addNigerGoogleFirebaseMarkersToClusterManager(userLocation)
        }


        addFirebaseMarkersToClusterManager(userLocation)



        addUserMarker(userLocation)


        sortClusterMarkerList()
        setTextViews()
        markersClickListeners()
        for (i in mClusterMarkers) {
            Log.d("Map", i.title)
        }
        mClusterManager!!.cluster()
        setCameraView()
    }

    private fun setTextViews() {
        mosque_first_distance.text =
            String.format(getString(R.string.km_test), sortedMosqueList[1].distanceFromUser)
        mosque_second_distance.text =
            String.format(getString(R.string.km_test), sortedMosqueList[2].distanceFromUser)
        mosque_third_distance.text =
            String.format(getString(R.string.km_test), sortedMosqueList[3].distanceFromUser)

        mosque_un.text = sortedMosqueList[1].title
        mosque_deux.text = sortedMosqueList[2].title
        mosque_trois.text = sortedMosqueList[3].title
    }

    private fun sortClusterMarkerList() {
        sortedMosqueList = mClusterMarkers.sortedWith(compareBy { it.distanceFromUser })
    }

    private fun setCameraView(position: LatLng) {
        // Set a boundary to start

        val bottomBoundary: Double = position!!.latitude - .009
        val leftBoundary: Double = position!!.longitude - .009
        val topBoundary: Double = position!!.latitude + .009
        val rightBoundary: Double = position!!.longitude + .009

        mMapBoundary = LatLngBounds(
            LatLng(bottomBoundary, leftBoundary),
            LatLng(topBoundary, rightBoundary)
        )
        try {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0))
            // mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0))
        } catch (ise: IllegalStateException) {
            mMap.setOnMapLoadedCallback(GoogleMap.OnMapLoadedCallback {
                mMap.moveCamera(
                    CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0)
                )
            })
        }


    }

    private fun setCameraView() {
        // Set a boundary to start
        if (userPosition != null) {
            val bottomBoundary: Double = userPosition!!.latitude - .009
            val leftBoundary: Double = userPosition!!.longitude - .009
            val topBoundary: Double = userPosition!!.latitude + .009
            val rightBoundary: Double = userPosition!!.longitude + .009

            mMapBoundary = LatLngBounds(
                LatLng(bottomBoundary, leftBoundary),
                LatLng(topBoundary, rightBoundary)
            )
            try {
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0))
            } catch (ise: IllegalStateException) {
                mMap.setOnMapLoadedCallback(GoogleMap.OnMapLoadedCallback {
                    mMap.moveCamera(
                        CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0)
                    )
                })
            }
        }

    }

    private fun calculateDistanceBetweenUserAndMosque(
        position: LatLng,
        userLocation: LatLng
    ): Double {
        val distanceInKm: Double =
            SphericalUtil.computeDistanceBetween(userLocation, position) * 0.001
        return BigDecimal(distanceInKm).setScale(2, RoundingMode.HALF_EVEN).toDouble()
    }

    private fun addNigerGoogleFirebaseMarkersToClusterManager(userLocation: LatLng) {
        if (mNigerGoogleMosqueList != null) {
            for (mosqueLocation in mNigerGoogleMosqueList) {
                val mosqueLat: Double = mosqueLocation.latitude.toDouble()
                val mosqueLg: Double = mosqueLocation.longitude.toDouble()
                val distanceFromUser =
                    calculateDistanceBetweenUserAndMosque(LatLng(mosqueLat, mosqueLg), userLocation)
                try {
                    val snippet =
                        distanceFromUser.toString() + " " +
                                getString(R.string.km) + " "+ "from your position "
                    val title = mosqueLocation.placeName

                    val newClusterMarker =
                        ClusterMarker(

                            mosqueLat,
                            mosqueLg,
                            title,
                            snippet,
                            "default",
                            true,
                            distanceFromUser
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

    private fun addGoogleFirebaseMarkersToClusterManager(userLocation: LatLng) {
        if (mGoogleMosqueList != null) {
            for (mosqueLocation in mGoogleMosqueList) {
                val mosqueLat: Double = mosqueLocation.latitude.toDouble()
                val mosqueLg: Double = mosqueLocation.longitude.toDouble()
                val distanceFromUser =
                    calculateDistanceBetweenUserAndMosque(LatLng(mosqueLat, mosqueLg), userLocation)
                try {
                    val snippet =
                        distanceFromUser.toString() + " "+
                                getString(R.string.km) +" "+ "from your position "
                    val title = mosqueLocation.placeName

                    val newClusterMarker =
                        ClusterMarker(

                            mosqueLat,
                            mosqueLg,
                            title,
                            snippet,
                            "default",
                            true,
                            distanceFromUser
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

    private fun addFirebaseMarkersToClusterManager(userLocation: LatLng) {
        var newClusterMarker: ClusterMarker? = null
        if (mMosqueList != null) {
            for (mosqueLocation in mMosqueList) {
                val distanceFromUser = calculateDistanceBetweenUserAndMosque(
                    LatLng(
                        mosqueLocation.position.latitude, mosqueLocation.position.longitude
                    ), userLocation
                )
                Log.d(
                    "Map",
                    "addMapMarkers: location: " + mosqueLocation.position.toString()
                )
                try {
                    val snippet =
                        distanceFromUser.toString() + " " +
                                getString(R.string.km) +" " +"from your position "
                    val title = mosqueLocation.name


                    if (mosqueLocation.report >= 2) {
                        newClusterMarker =
                            ClusterMarker(

                                mosqueLocation.position.latitude,
                                mosqueLocation.position.longitude
                                ,
                                title,
                                snippet,
                                "verified",
                                false,
                                distanceFromUser
                            )
                    } else if (mosqueLocation.report == 0L || mosqueLocation.report == -1L || mosqueLocation.report == 1L) {
                        newClusterMarker =
                            ClusterMarker(

                                mosqueLocation.position.latitude,
                                mosqueLocation.position.longitude
                                ,
                                title,
                                snippet,
                                "not_verified",
                                false,
                                distanceFromUser
                            )
                    } else if (mosqueLocation.report <= -2) {
                        newClusterMarker =
                            ClusterMarker(

                                mosqueLocation.position.latitude,
                                mosqueLocation.position.longitude
                                ,
                                title,
                                snippet,
                                "false",
                                false,
                                distanceFromUser
                            )
                    }

                    mClusterMarkers.add(newClusterMarker!!)
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

    private fun addUserMarker(position: LatLng) {
        //userPosition = mapViewModel.getUserPosition()!!
        try {

            val snippet2 = getString(R.string.you)
            val newClusterMarker2 =
                ClusterMarker(
                    position.latitude,
                    position.longitude
                    ,
                    "You",
                    snippet2,
                    "Me",
                    false,
                    0.0
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
            title(text = getString(R.string.title_dialog))

            icon(R.drawable.logo2)
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
            title(text = getString(R.string.title_dialog))
            message(text = getString(R.string.dialog_confirm))
            icon(R.drawable.logo2)


            positiveButton(text = getString(R.string.confirm_mosque)) { dialog ->
                dialog.cancel()
                user?.let { confirmMosquePosition(marker, it) }
            }
            negativeButton(text = getString(R.string.report_mosque)) { dialog ->
                dialog.cancel()
                user?.let { reportMosquePosition(marker, it) }
            }
        }
    }

    private fun reportMosquePosition(marker: Marker, user: User) {
        mapViewModel.reportFalseMosqueLocation(marker, user)
    }

    private fun confirmMosquePosition(marker: Marker, user: User) {
        mapViewModel.confirmMosqueLocation(marker, user)
    }

    private fun showDirectionInGoogleMapDialog(marker: Marker) {
        MaterialDialog(this).show {
            title(text = getString(R.string.title_dialog))
            message(text = getString(R.string.dialog_msg_google_map))
            icon(R.drawable.logo2)


            positiveButton(text = getString(R.string.yes)) { dialog ->
                dialog.cancel()
                intentToGoogleMap(marker)
            }
            negativeButton(text = getString(R.string.no)) { dialog ->
                dialog.cancel()
                //Toast.makeText(applicationContext, "Window closed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun logOutDialog() {
        MaterialDialog(this).show {
            title(text = getString(R.string.title_dialog))
            message(text = getString(R.string.dialog_log_out))
            icon(R.drawable.logo2)


            positiveButton(text = getString(R.string.yes)) { dialog ->
                dialog.cancel()
                signOut()
            }
            negativeButton(text = getString(R.string.no)) { dialog ->
                dialog.cancel()

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

    private fun getGoogleMosqueFromFirebase() {
        mGoogleMosqueList = mapViewModel.getGoogleMosqueFromRepository()
    }

    private fun getNigerGoogleMosqueFromFirebase() {
        mNigerGoogleMosqueList = mapViewModel.getNigerGoogleMosqueFromRepository()
        Log.d(TAG,mNigerGoogleMosqueList.size.toString())
    }

    private fun getMosquesFromGoogleMap(userLocation: LatLng) {

        mapViewModel.getGoogleMapMosqueFromRepository()?.observe(this, Observer {
            val places: List<PlacesSearchResult>? = it?.results
            if (places != null) {
                for (i in places.indices) {
                    val googlePlace = it!!.results!![i]
                    val lat = googlePlace.geometry!!.location!!.lat
                    val lng = googlePlace.geometry!!.location!!.lng
                    val placeName = googlePlace.name
                    val distanceFromUser = calculateDistanceBetweenUserAndMosque(
                        LatLng(lat, lng), userLocation
                    )

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
                            distanceFromUser.toString() +
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
                                true,
                                distanceFromUser
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