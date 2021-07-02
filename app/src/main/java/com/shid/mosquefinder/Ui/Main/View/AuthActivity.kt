package com.shid.mosquefinder.Ui.Main.View

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
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
import com.shid.mosquefinder.Data.Model.User
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Ui.Main.ViewModel.AuthViewModel
import com.shid.mosquefinder.Ui.Base.AuthViewModelFactory
import com.shid.mosquefinder.Ui.onboardingscreen.feature.onboarding.OnBoardingActivity
import com.shid.mosquefinder.Utils.Common.RC_SIGN_IN
import com.shid.mosquefinder.Utils.Common.USER
import com.shid.mosquefinder.Utils.Common.logErrorMessage
import com.shid.mosquefinder.Utils.GsonParser
import com.shid.mosquefinder.Utils.Network.Event
import com.shid.mosquefinder.Utils.Network.NetworkEvents
import com.shid.mosquefinder.Utils.PermissionUtils
import com.shid.mosquefinder.Utils.SharePref
import com.shid.mosquefinder.Utils.Status
import kotlinx.android.synthetic.main.activity_auth.*
import timber.log.Timber


class AuthActivity : AppCompatActivity() {


    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 999
        var userPosition: LatLng? = null
    }

    private  var fusedLocationProviderClient: FusedLocationProviderClient ?= null
    private var locationCallback: LocationCallback ?= null
    private var locationRequest:LocationRequest ?= null


    private lateinit var authViewModel: AuthViewModel
    private lateinit var authViewModelFactory: AuthViewModelFactory
    private lateinit var googleSignInClient: GoogleSignInClient
    private var previousSate = true

    private var sharePref: SharePref? = null
    private var isFirstTime: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        savedInstanceState?.let {
            previousSate = it.getBoolean("LOST_CONNECTION")
        }

        sharePref = SharePref(this)
        isFirstTime = sharePref!!.loadFirstTime()
        setLocationUtils()

        NetworkEvents.observe(this, Observer {
            if (it is Event.ConnectivityEvent)
                handleConnectivityChange()
        })
        initAuthViewModel()

        checkIfPermissionIsActive()
        initSignInButton()
        setObservers()

        initGoogleSignInClient()
    }

    private fun setLocationUtils() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 50
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            maxWaitTime= 5000
        }

    }

    private fun checkIfPermissionIsActive() {
        when {
            PermissionUtils.isAccessFineLocationGranted(this) -> {
                when {
                    PermissionUtils.isLocationEnabled(this) -> {
                        retrieveLocation()
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

    private fun retrieveLocation() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    userPosition = LatLng(location.latitude, location.longitude)

                    sharePref!!.saveUserPosition(userPosition!!)
                    // Update UI with location data
                    // ...
                }
            }


        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationProviderClient!!.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )


    }

    override fun onResume() {
        super.onResume()
        handleConnectivityChange()
        //setUpLocationListener()
        setLocationUtils()
        retrieveLocation()
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        if (fusedLocationProviderClient != null){
            fusedLocationProviderClient!!.removeLocationUpdates(locationCallback)
        }
        fusedLocationProviderClient = null
        locationCallback = null
        locationRequest = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("LOST_CONNECTION", previousSate)
        super.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()

    }

    private fun setObservers() {

        authViewModel.retrieveStatusMsg().observe(this, androidx.lifecycle.Observer{
            when (it.status) {
                Status.SUCCESS -> {
                    Toast.makeText(this, it.data, Toast.LENGTH_LONG).show()
                }
                Status.LOADING -> {

                }
                Status.ERROR -> {
                    //Handle Error

                    Toast.makeText(this, it.data, Toast.LENGTH_LONG).show()
                    Log.d("Search", it.message.toString())
                }
            }
        })


    }

    @SuppressLint("MissingPermission")
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
                            setLocationUtils()
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

    private fun handleConnectivityChange() {
        if (ConnectivityStateHolder.isConnected && !previousSate) {
            //showSnackBar(textView, "The network is back !")
            Sneaker.with(this) // Activity, Fragment or ViewGroup
                .setTitle(getString(R.string.sneaker_connected))
                .setMessage(getString(R.string.sneaker_msg_network))
                .sneakSuccess()
            google_sign_in_button.isClickable = true
        }

        if (!ConnectivityStateHolder.isConnected && previousSate) {
            //showSnackBar(textView, "No Network !")
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
        //startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun initAuthViewModel() {
        authViewModelFactory =
            AuthViewModelFactory(application)
        authViewModel = ViewModelProvider(
            this,
            authViewModelFactory
        ).get(AuthViewModel(application)::class.java)
    }

    private fun initGoogleSignInClient() {
        val googleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
    }

    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
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
    }*/

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
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
            String.format(resources.getString(R.string.toast_auth_salutation,name)),
            Toast.LENGTH_LONG
        ).show()
    }

    private fun goToOnBoardingActivity(user: User){
        val intent = Intent(this@AuthActivity, OnBoardingActivity::class.java)
        intent.putExtra(USER, user)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }

    private fun goToHomeActivity(user: User) {
        val convertUserJson = GsonParser.gsonParser?.toJson(user)
        if (convertUserJson != null) {
            sharePref?.saveUser(convertUserJson)
        }
        if (isFirstTime == true){
            val intent = Intent(this,LoadingActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }else{
            val intent = Intent(this@AuthActivity, HomeActivity::class.java)
            intent.putExtra(USER, user)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }

    }
}