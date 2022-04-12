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
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.elconfidencial.bubbleshowcase.BubbleShowCase
import com.elconfidencial.bubbleshowcase.BubbleShowCaseBuilder
import com.elconfidencial.bubbleshowcase.BubbleShowCaseListener
import com.elconfidencial.bubbleshowcase.BubbleShowCaseSequence
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import timber.log.Timber
import uk.co.markormesher.android_fab.FloatingActionButton
import uk.co.markormesher.android_fab.SpeedDialMenuAdapter
import uk.co.markormesher.android_fab.SpeedDialMenuItem
import uk.co.markormesher.android_fab.SpeedDialMenuOpenListener
import java.math.BigDecimal
import java.math.RoundingMode

class MapsActivity2 : AppCompatActivity(), OnMapReadyCallback, FirebaseAuth.AuthStateListener,
    OnMapsSdkInitializedCallback {
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 999
        private const val RQ_SEARCH = 101

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    private lateinit var mMap: GoogleMap
    private var userPosition: LatLng? = null
    private var testPlaces: List<PlacesSearchResult>? = null
    private lateinit var mapViewModel: MapViewModel

    //Markers
    private var mClusterManager: ClusterManager<ClusterMarker>? = null
    private var mClusterMarkers: MutableList<ClusterMarker> = ArrayList()
    private var mClusterManagerRenderer: MyClusterManagerRenderer? = null
    private var markerCollection: MarkerManager.Collection? = null
    private var markerManager: MarkerManager? = null
    private var markerCollectionForClusters: MarkerManager.Collection? = null
    private var markerCollectionForClusters2: MarkerManager.Collection? = null
    private var clusterMarkerFromIntent: ClusterMarker? = null

    private var mMosqueList: MutableList<Mosque> = ArrayList()
    private var sortedMosqueList: List<ClusterMarker> = ArrayList()

    private lateinit var mMapBoundary: LatLngBounds

    private var previousSate = true

    private val firebaseAuth = FirebaseAuth.getInstance()
    private var googleSignInClient: GoogleSignInClient? = null
    private var user: User? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    private lateinit var fusedLocationWrapper: FusedLocationWrapper
    private var sharePref: SharePref? = null
    private var isFirstTime: Boolean? = null
    private var useCount: Int = 0
    private var didUserRate: Boolean? = null

    lateinit var manager: ReviewManager
    var reviewInfo: ReviewInfo? = null

    private var buttonIcon = 0
    private var speedDialSize = 3
    private val speedDialSizeOptions = arrayOf(
        Pair("None", 0),
        Pair("1 item", 1),
        Pair("2 items", 2),
        Pair("3 items", 3)

    )


    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LATEST, this);
        setContentView(R.layout.activity_maps2)
        savedInstanceState?.let {
            previousSate = it.getBoolean("LOST_CONNECTION")
        }
        initialSetUp()
        permissionCheck(fusedLocationWrapper)
        setDrawerLayout()
        setTransparentStatusBar()
        setNetworkMonitor()
        setupViewModel()
        fetchGoogleMapMosques()
        initGoogleSignInClient()
        setMessageForToast(user!!)
        btnClickListeners()
        setObserver()
    }

    private fun fetchGoogleMapMosques() {
        when {
            userPosition != null -> {
                mosqueeDePlace(userPosition!!)
                Timber.d("using User Position")
            }
            else -> {
                checkPref()
                if (userPosition != null) {
                    mosqueeDePlace(userPosition!!)
                    Timber.d("using User Position share Pref")
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun initialSetUp() {
        fusedLocationWrapper = fusedLocationWrapper()
        sharePref = SharePref(this)

        searchText.isClickable = false
        searchText.isEnabled = false
        useCount = loadUseCount()
        didUserRate = loadIfUserRated()
        useCount++
        setUserCount(useCount)
        if (useCount != 0 && didUserRate == false) {
            if (useCount.rem(5) == 0) {
                dialogRateApp(didUserRate!!)
            }
        }

        if (checkPrefFirstTime()) {
            activateShowcase()
            isFirstTime = false
            setFirstTimePref(isFirstTime!!)
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        user = getUserFromIntent()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    private fun getUserLocation(fusedLocationWrapper: FusedLocationWrapper) {
        this.lifecycleScope.launch {
            val location = fusedLocationWrapper.awaitLastLocation()
            userPosition = LatLng(location.latitude, location.longitude)
            userPosition?.let {
                savePositionToSharePref(LatLng(it.latitude, it.longitude))
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    private fun permissionCheck(fusedLocationWrapper: FusedLocationWrapper) {
        if (PermissionUtils.isAccessFineLocationGranted(this)) {
            getUserLocation(fusedLocationWrapper)

        } else {
            Toast.makeText(this, getString(R.string.toast_permission), Toast.LENGTH_LONG).show()
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadUseCount(): Int {
        return sharePref!!.loadUseCount()
    }

    private fun loadIfUserRated(): Boolean {
        return sharePref!!.loadIfUserRated()
    }

    private fun checkPrefFirstTime(): Boolean {
        isFirstTime = sharePref!!.loadIsFirstTimePref()
        return isFirstTime!!
    }

    private fun askForReview(valueRate: Boolean) {
        if (reviewInfo != null) {
            manager.launchReviewFlow(this, reviewInfo!!).addOnFailureListener {
            }.addOnCompleteListener { _ ->
                didUserRate = true
                setIfUserRated(true)

            }
        }
    }

    // Call this method asap, for example in onCreate()
    private fun initReviews() {
        manager = ReviewManagerFactory.create(this)
        manager.requestReviewFlow().addOnCompleteListener { request ->
            if (request.isSuccessful) {
                reviewInfo = request.result
            } else {
                // Log error
            }
        }
    }

    private fun dialogRateApp(valueRate: Boolean) {
        MaterialDialog(this).show {
            title(text = getString(R.string.title_dialog))
            message(text = getString(R.string.dialog_rate_app))
            positiveButton(text = getString(R.string.rate)) { dialog ->
                dialog.cancel()
                goToStore()
                setIfUserRated(true)
            }
            negativeButton(text = getString(R.string.cancel)) { dialog ->
                dialog.cancel()
                didUserRate = false
                setIfUserRated(valueRate)
            }
            icon(R.drawable.logo2)
        }
    }

    private fun goToStore() {
        val intent = Intent(Intent.ACTION_VIEW)
            .setData(Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
        try {
            startActivity(
                Intent(intent)
                    .setPackage("com.android.vending")
            )
        } catch (exception: ActivityNotFoundException) {
            startActivity(intent)
        }
    }

    private fun setIfUserRated(didUserRate: Boolean) {
        sharePref!!.saveIfUserRated(didUserRate)
    }

    private fun setUserCount(use_count: Int) {
        sharePref!!.saveUseCount(use_count)
    }

    private fun setFirstTimePref(value: Boolean) {
        sharePref!!.setIsFirstTime(value)
    }

    private fun checkPref() {
        userPosition = sharePref!!.loadSavedPosition()
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

                R.id.nav_home -> {
                    goToHome()
                }

                R.id.nav_quran -> {
                    goToQuran()
                }

                R.id.nav_names -> {
                    goToNames()
                }

                R.id.nav_azkhar -> {
                    goToCategories()
                }

                R.id.nav_quotes -> {
                    goToQuotes()
                }

                R.id.nav_mosques -> {
                    goToBeautifulMosques()
                }

                R.id.nav_qibla -> {
                    goToQibla()
                }

                R.id.nav_settings -> {
                    goToSettings()
                }

                R.id.nav_share -> {
                    goToShareApp()
                }
                R.id.nav_feedback -> {
                    goToFeedback()
                }
                /* R.id.nav_credits -> {
                 //AnalyticsUtil.logEvent(this, AnalyticsUtil.Value.MENU_CREDITS)
                 goToCredits()
             }*/
                R.id.nav_contact -> {
                    sendEmail()
                }
                R.id.nav_help -> {
                    activateShowcase()
                }
                R.id.nav_exit -> {
                    logOutDialog()
                }
            }
            drawerLayout.closeDrawer(navigationView)
            true
        }
    }

    private fun goToSettings() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }

    private fun goToQibla() {
        val intent = Intent(this, CompassActivity::class.java)
        startActivity(intent)
    }

    private fun goToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("user", user)
        startActivity(intent)
    }

    private fun goToNames() {
        val intent = Intent(this, NamesActivity::class.java)
        startActivity(intent)
    }

    private fun goToCategories() {
        val intent = Intent(this, CategoriesActivity::class.java)
        startActivity(intent)
    }

    private fun activateShowcase() {
        BubbleShowCaseSequence()
            .addShowCase(
                BubbleShowCaseBuilder(this) //Activity instance
                    .title(getString(R.string.bubble)) //Any title for the bubble view
                    .backgroundColorResourceId(R.color.colorPrimary)
                    .imageResourceId(R.drawable.logo2)
                    .textColorResourceId(R.color.colorWhite)
            ) //First BubbleShowCase to show
            .addShowCase(
                BubbleShowCaseBuilder(this) //Activity instance
                    .title(getString(R.string.bubble1)) //Any title for the bubble view
                    .targetView(card_map) //View to point out
                    .description(getString(R.string.description1))
                    .backgroundColorResourceId(R.color.colorPrimary)
                    .imageResourceId(R.drawable.logo2)
                    .textColorResourceId(R.color.colorWhite)
            ) //First BubbleShowCase to show
            .addShowCase(
                BubbleShowCaseBuilder(this) //Activity instance
                    .title(getString(R.string.bubble2)) //Any title for the bubble view
                    .targetView(fab1) //View to point out
                    .description(getString(R.string.description2))
                    .backgroundColorResourceId(R.color.colorPrimary)
                    .imageResourceId(R.drawable.logo2)
                    .textColorResourceId(R.color.colorWhite)
            ) // This one will be showed when firstShowCase is dismissed
            .addShowCase(
                BubbleShowCaseBuilder(this) //Activity instance
                    .title(getString(R.string.bubble3)) //Any title for the bubble view
                    .targetView(searchText) //View to point out
                    .description(getString(R.string.description3))
                    .backgroundColorResourceId(R.color.colorPrimary)
                    .imageResourceId(R.drawable.logo2)
                    .textColorResourceId(R.color.colorWhite)
            )
            .addShowCase(
                BubbleShowCaseBuilder(this) //Activity instance
                    .title(getString(R.string.bubble4)) //Any title for the bubble view
                    .targetView(menuButton) //View to point out
                    .description(getString(R.string.description4))
                    .backgroundColorResourceId(R.color.colorPrimary)
                    .imageResourceId(R.drawable.logo2)
                    .textColorResourceId(R.color.colorWhite)
                    .listener(object : BubbleShowCaseListener {
                        override fun onBackgroundDimClick(bubbleShowCase: BubbleShowCase) {

                        }

                        override fun onBubbleClick(bubbleShowCase: BubbleShowCase) {

                        }

                        override fun onCloseActionImageClick(bubbleShowCase: BubbleShowCase) {
                            when {
                                userPosition != null -> {
                                    addMapMarkers(userPosition!!)
                                }
                                else -> {
                                    Toast.makeText(
                                        this@MapsActivity2,
                                        getString(R.string.offline),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }

                        }

                        override fun onTargetClick(bubbleShowCase: BubbleShowCase) {

                        }

                    })
            )
            .show() //Display the ShowCaseSequence
    }

    private fun goToQuran() {
        val intent = Intent(this, SurahActivity::class.java)
        startActivity(intent)
    }

    private fun goToBeautifulMosques() {
        val intent = Intent(this, BeautifulMosquesActivity::class.java)
        startActivity(intent)
    }

    private fun goToQuotes() {
        val intent = Intent(this, QuotesActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
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
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
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
                    Timber.d(it.message.toString())
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
        firebaseAuth.currentUser?.delete()
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
        Toast.makeText(
            this,
            String.format(resources.getString(R.string.toast_log_in_msg, user.name)),
            Toast.LENGTH_LONG
        ).show()
    }

    private fun goToAuthInActivity() {
        val intent = Intent(this@MapsActivity2, AuthActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
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
            Sneaker.with(this) // Activity, Fragment or ViewGroup
                .setTitle(getString(R.string.sneaker_connected))
                .setMessage(getString(R.string.sneaker_msg_network))
                .sneakSuccess()
        }

        if (!ConnectivityStateHolder.isConnected && previousSate) {
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
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("LOST_CONNECTION", previousSate)
        super.onSaveInstanceState(outState)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.apply {
            try {
                val success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        this@MapsActivity2, R.raw.map_style
                    )
                )
                if (!success) {
                    Timber.e("Style parsing failed.")
                }
            } catch (e: Resources.NotFoundException) {
                Timber.e("Can't find style. Error: $e")
            }
        }

        when {
            userPosition != null -> {
                Handler().postDelayed(kotlinx.coroutines.Runnable {

                    addMapMarkers(userPosition!!)
                    searchText.isClickable = true
                    searchText.isEnabled = true
                }, 3000)
            }
            else -> {
                Handler().postDelayed(kotlinx.coroutines.Runnable {
                    checkPref()
                    userPosition?.let {
                        addMapMarkers(it)
                        searchText.isClickable = true
                        searchText.isEnabled = true
                    }

                }, 3000)

            }
        }

    }

    private fun savePositionToSharePref(position: LatLng) {
        sharePref!!.saveUserPosition(position)
    }

    private fun btnClickListeners() {
        card_first.setOnClickListener {
            if (sortedMosqueList.isNotEmpty() && sortedMosqueList.size > 5) {
                setCameraView(sortedMosqueList[1].position)
            } else {
                Toast.makeText(this, getString(R.string.refresh_map), Toast.LENGTH_LONG).show()
            }

        }

        card_second.setOnClickListener {
            if (sortedMosqueList.isNotEmpty() && sortedMosqueList.size > 5) {
                setCameraView(sortedMosqueList[2].position)
            } else {
                Toast.makeText(this, getString(R.string.refresh_map), Toast.LENGTH_LONG).show()
            }

        }

        card_third.setOnClickListener {
            if (sortedMosqueList.isNotEmpty() && sortedMosqueList.size > 5) {
                setCameraView(sortedMosqueList[3].position)
            } else {
                Toast.makeText(this, getString(R.string.refresh_map), Toast.LENGTH_LONG).show()
            }

        }

        menuButton.setOnClickListener {
            drawerLayout.openDrawer(navigationView)
        }

        searchText.setOnClickListener {
            goToSearch()
        }

        fab.speedDialMenuAdapter = setUpSpeedMenuAdapter()
        fab.contentCoverEnabled = true
        fab.setContentCoverColour(0xcc000000.toInt())

        fab.setOnSpeedDialMenuOpenListener(object : SpeedDialMenuOpenListener {

            override fun onOpen(floatingActionButton: FloatingActionButton) {

            }
        })


    }

    private fun setUpSpeedMenuAdapter(): SpeedDialMenuAdapter {
        val speedDialMenuAdapter = object : SpeedDialMenuAdapter() {
            override fun getCount(): Int = speedDialSizeOptions[speedDialSize].second

            override fun getMenuItem(context: Context, position: Int): SpeedDialMenuItem =
                when (position) {
                    0 -> SpeedDialMenuItem(
                        context,
                        R.drawable.ic_refresh,
                        getString(R.string.options_1)
                    )
                    1 -> SpeedDialMenuItem(
                        context,
                        R.drawable.ic_add,
                        getString(R.string.options_2)
                    )
                    2 -> SpeedDialMenuItem(
                        context,
                        R.drawable.ic_my_location,
                        getString(R.string.options_3)
                    )
                    else -> throw IllegalArgumentException("No menu item: $position")
                }

            override fun getBackgroundColour(position: Int): Int {
                return Color.argb(255, 255, 255, 255)
            }

            override fun onMenuItemClick(position: Int): Boolean {
                if (position == 0) {
                    mosqueeDePlace(userPosition!!)
                    Handler().postDelayed(Runnable {
                        addMapMarkers(userPosition!!)
                    }, 2000)
                } else if (position == 1) {
                    setCameraView()
                    Handler().postDelayed(kotlinx.coroutines.Runnable {
                        mosquePromptDialog()
                    }, 2000)
                } else {
                    when {
                        userPosition != null -> {
                            setCameraView(userPosition!!)
                        }
                        else -> {
                            setCameraView()
                        }
                    }
                }
                return true
            }

            override fun onPrepareItemLabel(context: Context, position: Int, label: TextView) {
                // make the first item bold if there are multiple items
                // (this isn't a design pattern, it's just to demo the functionality)
                if (speedDialSize > 0) {
                    label.setTypeface(label.typeface, Typeface.BOLD)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        label.setTextColor(
                            resources.getColor(
                                R.color.colorWhite,
                                context.theme
                            )
                        )
                    } else {
                        label.setTextColor(resources.getColor(R.color.colorWhite))
                    }
                }
            }

            // rotate the "+" icon only
            override fun fabRotationDegrees(): Float = if (buttonIcon == 0) 135F else 0F
        }
        return speedDialMenuAdapter
    }

    private fun goToSearch() {
        val intent = Intent(this, SearchActivity::class.java)
        val bundle = Bundle()
        val list = arrayListOf<ClusterMarker>()
        list.addAll(mClusterMarkers)
        bundle.putParcelableArrayList("test", list)
        intent.putExtras(bundle)
        startActivityForResult(intent, RQ_SEARCH)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun mosquePromptDialog() {
        MaterialDialog(this).show {
            title(text = getString(R.string.title_dialog))
            message(text = getString(R.string.dialog_prompt_location))
            positiveButton(text = getString(R.string.yes)) { dialog ->
                dialog.cancel()
                prepareMapForInput()
            }
            negativeButton(text = getString(R.string.cancel)) { dialog ->
                dialog.cancel()
            }
            icon(R.drawable.logo2)
        }
    }

    private fun prepareMapForInput() {
        resetMap()
        Toast.makeText(this, getString(R.string.toast_marker), Toast.LENGTH_LONG).show()
        addSingleMarker()
    }

    private fun addSingleMarker() {
        markerManager = MarkerManager(mMap)
        markerCollection = markerManager!!.newCollection()
        markerCollection!!.addMarker(
            MarkerOptions()
                .position(userPosition!!)
                .draggable(true)
                .zIndex(1.0f)
        )

        val listener: GoogleMap.OnMarkerDragListener = object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragEnd(marker: Marker) {
                Timber.d("onMarkerDragEnd:%s", marker.position)
            }

            override fun onMarkerDragStart(p0: Marker) {
                Timber.d("onMarkerDragStart")
            }

            override fun onMarkerDrag(p0: Marker) {

                Timber.d("onMarkerDrag")
            }
        }

        markerCollection!!.setOnMarkerDragListener(listener)
        markerCollection!!.setOnMarkerClickListener { marker -> //userPosition?.let { mosqueInputDialog(it) }
            Timber.d("onMarkerClickListener:%s", marker.position)

            marker.let {
                mosqueInputDialog(
                    LatLng(
                        it.position.latitude,
                        it.position.longitude
                    )
                )
            }
            true
        }
    }

    private fun mosqueInputDialog(userPositionToAdd: LatLng) {

        MaterialDialog(this).show {
            input(hint = getString(R.string.dialog_input_title))
            // input(inputType = type)
            title(text = getString(R.string.title_dialog))
            message(text = getString(R.string.dialog_prompt_location))
            positiveButton(text = getString(R.string.dialog_save)) { dialog ->
                val inputField = dialog.getInputField().text.toString()

                dialog.cancel()
                saveMosqueInputInDatabase(userPositionToAdd, inputField)
                mosqueeDePlace(userPositionToAdd)
                userPosition?.let { addMapMarkers(it) }
            }
            negativeButton(text = getString(R.string.cancel)) { dialog ->
                dialog.cancel()
                mosqueeDePlace(userPositionToAdd)
                userPosition?.let { addMapMarkers(it) }
            }
            icon(R.drawable.logo2)
        }
    }

    private fun saveMosqueInputInDatabase(userPosition: LatLng, inputField: String) {
        val mosqueLocation = GeoPoint(userPosition.latitude, userPosition.longitude)
        val userMosqueInput: HashMap<String, Comparable<*>> = hashMapOf(
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                clusterMarkerFromIntent = data.getParcelableExtra("search_result")!!

                val latLngNow = clusterMarkerFromIntent?.position
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
                    mMap.setOnMapLoadedCallback {
                        mMap.moveCamera(
                            CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0)
                        )
                    }
                }

            }
        }


    }


    private fun addMapMarkers(userLocation: LatLng) {
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

        putPlacesInCluster(userLocation)
        getMosqueFromFirebase()
        addFirebaseMarkersToClusterManager(userLocation)
        addUserMarker(userLocation)

        sortClusterMarkerList()
        setTextViews()
        markersClickListeners()
        for (i in mClusterMarkers) {
            Timber.d(i.title.toString())
        }
        mClusterManager!!.cluster()
        setCameraView()
    }

    private fun setTextViews() {
        if (sortedMosqueList.isNotEmpty() && sortedMosqueList.size > 5) {
            Timber.d(sortedMosqueList[2].title.toString())
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

    }

    private fun sortClusterMarkerList() {
        sortedMosqueList = mClusterMarkers.sortedWith(compareBy { it.distanceFromUser })
    }

    private fun setCameraView(position: LatLng) {
        val bottomBoundary: Double = position.latitude - .009
        val leftBoundary: Double = position.longitude - .009
        val topBoundary: Double = position.latitude + .009
        val rightBoundary: Double = position.longitude + .009

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

    private fun setCameraView() {
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


    private fun addFirebaseMarkersToClusterManager(userLocation: LatLng) {
        var newClusterMarker: ClusterMarker? = null
        if (mMosqueList != null) {
            for (mosqueLocation in mMosqueList) {
                val distanceFromUser = calculateDistanceBetweenUserAndMosque(
                    LatLng(
                        mosqueLocation.position.latitude, mosqueLocation.position.longitude
                    ), userLocation
                )
                Timber.d("addMapMarkers: location: $mosqueLocation.position.toString()")
                try {
                    val snippet =
                        distanceFromUser.toString() + " " +
                                getString(R.string.km) + " " + getString(R.string.from_position)
                    val title = mosqueLocation.name

                    if (mosqueLocation.report >= 2) {
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
                    } else if (mosqueLocation.report == 0L || mosqueLocation.report == -1L || mosqueLocation.report == 1L) {
                        newClusterMarker =
                            ClusterMarker(

                                mosqueLocation.position.latitude,
                                mosqueLocation.position.longitude,
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
                                mosqueLocation.position.longitude,
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
                    Timber.e("addMapMarkers: NullPointerException: %s", e.message)
                }
            }
        }
    }

    private fun addUserMarker(position: LatLng) {
        try {
            val snippet2 = getString(R.string.you)
            val newClusterMarker2 =
                ClusterMarker(
                    position.latitude,
                    position.longitude,
                    "You",
                    snippet2,
                    "Me",
                    false,
                    0.0
                )
            mClusterManager!!.addItem(newClusterMarker2)
            mClusterMarkers.add(newClusterMarker2)


        } catch (e: NullPointerException) {
            Timber.e("addMapMarkers: NullPointerException: " + e.message)
        }
    }

    private fun markersClickListeners() {
        markerCollectionForClusters = mClusterManager!!.markerCollection
        markerCollectionForClusters?.setOnMarkerClickListener { marker ->
            Timber.d("you clicked")
            marker.showInfoWindow()
            true
        }

        markerCollectionForClusters?.setOnInfoWindowClickListener { marker ->
            Timber.d("you clicked info window")
            for (i in mClusterMarkers) {
                Timber.d(marker.title)
                Timber.d(i.title!!)
                //We check if the marker comes from Google Place or FireStore
                if (i.isMarkerFromGooglePlace && i.title == marker.title) {
                    showDirectionInGoogleMapDialog(marker)

                } else if (i.title == marker.title && !i.isMarkerFromGooglePlace) {
                    Timber.d("you clicked info window ,access granted")
                    showOptionsDialog(marker)
                }
            }
        }
    }

    private fun showOptionsDialog(marker: Marker) {
        when {
            marker.title!!.contains("You") -> {
                marker.showInfoWindow()
            }
            marker.title!!.contains("Trip") -> {
                showDirectionInGoogleMapDialog(marker)
            }
            else -> {
                showOptionsForUserInputMosquesDialog(marker)
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
        Timber.d("check OnClick")
        val latitude: String = marker.position.latitude.toString()
        val longitude: String = marker.position.longitude.toString()
        val gmmIntentUri = Uri.parse("google.navigation:q=$latitude,$longitude")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        try {
            if (mapIntent.resolveActivity(applicationContext.packageManager) != null) {
                startActivity(mapIntent)
                Timber.d("check OnClick start activity")
            }
        } catch (e: java.lang.NullPointerException) {
            Timber.d("onClick: NullPointerException: Couldn't open map.%s", e.message)
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


    private fun mosqueeDePlace(userLocation: LatLng) {
        /* if (getCountryCode(applicationContext) != "gh" && getCountryCode(applicationContext) != "tg" && getCountryCode(applicationContext) != "ne" ) {
         mapViewModel.getGoogleMapMosqueFromRepository(userLocation)
             ?.observe(this, Observer {
                 val places: List<PlacesSearchResult>? = it?.results
                 testPlaces = places
             })
     }*/

        mapViewModel.getGoogleMapMosqueFromRepository(userLocation)
            ?.observe(this, Observer {
                val places: List<PlacesSearchResult>? = it?.results
                Timber.d("Is googleMap mosque list empty:${places?.isEmpty()}")
                testPlaces = places
            })

    }

    private fun putPlacesInCluster(userLocation: LatLng) {
        Timber.d("value of places $testPlaces")
        if (testPlaces != null) {
            for (i in testPlaces!!.indices) {
                val googlePlace = testPlaces!![i]
                val lat = googlePlace.geometry!!.location!!.lat
                val lng = googlePlace.geometry!!.location!!.lng
                val placeName = googlePlace.name
                val distanceFromUser = calculateDistanceBetweenUserAndMosque(
                    LatLng(lat, lng), userLocation
                )

                Timber.d(
                    "addMapMarkers: location: %s",
                    googlePlace.geometry.location.toString()
                )
                Timber.d("addMapMarkers: name: %s", googlePlace.name)
                try {
                    val snippet =
                        distanceFromUser.toString() +
                                getString(R.string.km) + " " + getString(R.string.position)

                    val newClusterMarker =
                        ClusterMarker(
                            lat,
                            lng,
                            placeName,
                            snippet,
                            "default",
                            true,
                            distanceFromUser
                        )
                    mClusterManager!!.addItem(newClusterMarker)
                    markerCollectionForClusters = mClusterManager!!.markerCollection
                    mClusterMarkers.add(newClusterMarker)
                    for (i in mClusterMarkers) {
                        Timber.d(i.title.toString())
                    }

                } catch (e: NullPointerException) {
                    Timber.e("addMapMarkers: NullPointerException: %s", e.message)
                }

            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
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
                            permissionCheck(fusedLocationWrapper)
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

    override fun onMapsSdkInitialized(renderer: MapsInitializer.Renderer) {
        when (renderer) {
            MapsInitializer.Renderer.LATEST -> Timber.d("The latest version of the renderer is used.")
            MapsInitializer.Renderer.LEGACY -> Timber.d("The legacy version of the renderer is used.")
        }

    }


}
