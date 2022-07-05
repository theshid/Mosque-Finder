package com.shid.mosquefinder.app.ui.main.views

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.irozon.sneaker.Sneaker
import com.shid.mosquefinder.ConnectivityStateHolder
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.ui.base.BaseActivity
import com.shid.mosquefinder.app.ui.main.view_models.AuthViewModel
import com.shid.mosquefinder.app.ui.onboardingscreen.feature.onboarding.OnBoardingActivity
import com.shid.mosquefinder.app.utils.*
import com.shid.mosquefinder.app.utils.Common.LOCATION_PERMISSION_REQUEST_CODE
import com.shid.mosquefinder.app.utils.Common.USER
import com.shid.mosquefinder.app.utils.Common.logErrorMessage
import com.shid.mosquefinder.app.utils.Network.Event
import com.shid.mosquefinder.app.utils.Network.NetworkEvents
import com.shid.mosquefinder.data.model.User
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_auth.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class AuthActivity : BaseActivity() {

    var userPosition: LatLng? = null
   /* @Inject
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient*/
    /*private var locationCallback: LocationCallback ?= null
    private var locationRequest:LocationRequest ?= null*/


    private val authViewModel: AuthViewModel by viewModels()

    /*private lateinit var authViewModelFactory: AuthViewModelFactory*/
    private lateinit var googleSignInClient: GoogleSignInClient
    private var previousSate = true

    @Inject
    private lateinit var googleSignInOptions: GoogleSignInOptions

    @Inject
    private lateinit var sharePref: SharePref
    private var isFirstTime: Boolean? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    @Inject
    private lateinit var fusedLocationWrapper: FusedLocationWrapper

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        savedInstanceState?.let {
            previousSate = it.getBoolean("LOST_CONNECTION")
        }
        //fusedLocationWrapper = fusedLocationWrapper()
        //sharePref = SharePref(this)
        isFirstTime = sharePref.loadFirstTime()
        //setLocationUtils()

        NetworkEvents.observe(this, Observer {
            if (it is Event.ConnectivityEvent)
                handleConnectivityChange()
        })
        //initAuthViewModel()

        checkIfPermissionIsActive()
        initSignInButton()
        setObservers()

        initGoogleSignInClient()
    }

   /* private fun setLocationUtils() {
        //fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 50
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            maxWaitTime = 5000
        }

    }*/

    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    private fun permissionCheck(fusedLocationWrapper: FusedLocationWrapper) {
        if (PermissionUtils.isAccessFineLocationGranted(this)) {
            getUserLocation(fusedLocationWrapper)

        } else {
            Toast.makeText(this, getString(R.string.toast_permission), Toast.LENGTH_LONG).show()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    private fun getUserLocation(fusedLocationWrapper: FusedLocationWrapper) {
        this.lifecycleScope.launch {
            val location = fusedLocationWrapper.awaitLastLocation()
            userPosition = LatLng(location.latitude, location.longitude)
            userPosition?.let {
                sharePref.saveUserPosition(LatLng(it.latitude, it.longitude))
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun checkIfPermissionIsActive() {
        when {
            PermissionUtils.isAccessFineLocationGranted(this) -> {
                when {
                    PermissionUtils.isLocationEnabled(this) -> {
                        permissionCheck(fusedLocationWrapper)
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

    /*private fun retrieveLocation() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    userPosition = LatLng(location.latitude, location.longitude)

                    sharePref!!.saveUserPosition(userPosition!!)
                    // Update UI with location data
                    // ...
                }
            }


        }
    }*/

   /* @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationProviderClient!!.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )


    }*/

    override fun onResume() {
        super.onResume()
        handleConnectivityChange()
        //setUpLocationListener()
        /*setLocationUtils()
        retrieveLocation()
        startLocationUpdates()*/
    }

    override fun onPause() {
        super.onPause()
        /*   if (fusedLocationProviderClient != null){
               fusedLocationProviderClient!!.removeLocationUpdates(locationCallback)
           }
           fusedLocationProviderClient = null
           locationCallback = null
           locationRequest = null*/
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("LOST_CONNECTION", previousSate)
        super.onSaveInstanceState(outState)
    }

    private fun setObservers() {

        authViewModel.retrieveStatusMsg().observe(this, androidx.lifecycle.Observer {
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

  /*  @SuppressLint("MissingPermission")
    fun setUpLocationListener() {
        fusedLocationProviderClient!!.lastLocation
            .addOnSuccessListener { location: android.location.Location? ->
                userPosition =
                    location?.longitude?.let {
                        LatLng(
                            location.latitude,
                            it
                        )
                    } // Got last known location. In some rare situations this can be null.
                userPosition?.let {
                    Timber.d("value of position:$userPosition")
                    sharePref!!.saveUserPosition(LatLng(it.latitude, it.longitude))
                }
            }

    }*/


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

    private fun handleConnectivityChange() {
        if (ConnectivityStateHolder.isConnected && !previousSate) {
            Sneaker.with(this) // Activity, Fragment or ViewGroup
                .setTitle(getString(R.string.sneaker_connected))
                .setMessage(getString(R.string.sneaker_msg_network))
                .sneakSuccess()
            google_sign_in_button.isClickable = true
        }

        if (!ConnectivityStateHolder.isConnected && previousSate) {
            Sneaker.with(this) // Activity, Fragment or ViewGroup
                .setTitle(getString(R.string.sneaker_disconnected))
                .setMessage(getString(R.string.sneaker_msg_network_lost))
                .sneakError()
            google_sign_in_button.isClickable = false
        }

        previousSate = ConnectivityStateHolder.isConnected
    }

    private fun initSignInButton() {
        google_sign_in_button.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        resultLauncher.launch(signInIntent)
    }

    /*private fun initAuthViewModel() {
        authViewModelFactory =
            AuthViewModelFactory(application)
        authViewModel = ViewModelProvider(
            this,
            authViewModelFactory
        ).get(AuthViewModel(application)::class.java)
    }*/

    private fun initGoogleSignInClient() {
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
    }


    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                val task =
                    GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val googleSignInAccount =
                        task.getResult(ApiException::class.java)
                    googleSignInAccount?.let { getGoogleAuthCredential(it) }
                } catch (e: ApiException) {
                    logErrorMessage(e.message)
                }

            }
        }

    private fun getGoogleAuthCredential(googleSignInAccount: GoogleSignInAccount) {
        val googleTokenId = googleSignInAccount.idToken
        val googleAuthCredential =
            GoogleAuthProvider.getCredential(googleTokenId, null)
        signInWithGoogleAuthCredential(googleAuthCredential)
    }

    private fun signInWithGoogleAuthCredential(googleAuthCredential: AuthCredential) {
        authViewModel.signInWithGoogle(googleAuthCredential)
        authViewModel.authenticatedUserLiveData?.observe(this, Observer {
            if (it.isNew!!) {
                createNewUser(it)
            } else {
                goToHomeActivity(it)
            }
        })

    }

    private fun createNewUser(authenticatedUser: User) {

        authViewModel.createUser(authenticatedUser)
        authViewModel.createdUserLiveData?.observe(this, Observer {
            if (it.isCreated!!) {
                toastMessage(it.name!!)
            }
            goToOnBoardingActivity(it)
        })

    }

    private fun toastMessage(name: String) {
        Toast.makeText(
            this,
            String.format(resources.getString(R.string.toast_auth_salutation, name)),
            Toast.LENGTH_LONG
        ).show()

    }

    private fun goToOnBoardingActivity(user: User) {
        startActivity<OnBoardingActivity>{
            putExtra(USER, user)
        }
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }

    private fun goToHomeActivity(user: User) {
        val convertUserJson = GsonParser.gsonParser?.toJson(user)
        convertUserJson?.let {  sharePref.saveUser(convertUserJson) }
       /* if (convertUserJson != null) {
            sharePref.saveUser(convertUserJson)
        }*/
        if (isFirstTime == true) {
            startActivity<LoadingActivity>()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        } else {
            startActivity<HomeActivity>{
                putExtra(USER, user)
            }
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }

    }
}