package com.shid.mosquefinder.Ui.Main.View

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
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
import com.shid.mosquefinder.Utils.Common
import com.shid.mosquefinder.Utils.Common.RC_SIGN_IN
import com.shid.mosquefinder.Utils.Common.USER
import com.shid.mosquefinder.Utils.Common.logErrorMessage
import com.shid.mosquefinder.Utils.Network.Event
import com.shid.mosquefinder.Utils.Network.NetworkEvents
import com.shid.mosquefinder.Utils.PermissionUtils
import com.shid.mosquefinder.Utils.setTransparentStatusBar
import fr.quentinklein.slt.LocationTracker
import fr.quentinklein.slt.ProviderError
import kotlinx.android.synthetic.main.activity_auth.*


class AuthActivity : AppCompatActivity() {


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 999
        private const val TAG = "AuthActivity"
        var userPosition: LatLng? = null
        var newUserPosition:LatLng?= null

    }

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback


    private lateinit var authViewModel: AuthViewModel
    private lateinit var authViewModelFactory: AuthViewModelFactory
    private lateinit var googleSignInClient: GoogleSignInClient
    private var previousSate = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        savedInstanceState?.let {
            previousSate = it.getBoolean("LOST_CONNECTION")
        }


        NetworkEvents.observe(this, Observer {
            if (it is Event.ConnectivityEvent)
                handleConnectivityChange()
        })
        initAuthViewModel()
        setTransparentStatusBar()
        checkIfPermissionIsActive()
        initSignInButton()

        initGoogleSignInClient()
    }

    private fun checkIfPermissionIsActive() {
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

    override fun onResume() {
        super.onResume()
        handleConnectivityChange()
    }

    override fun onPause() {
        super.onPause()
        if (fusedLocationProviderClient != null){
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("LOST_CONNECTION", previousSate)
        super.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()

    }

    @SuppressLint("MissingPermission")
    fun setUpLocationListener() {
         fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(application)
        // for getting the current location update after every 2 seconds with high accuracy
        val locationRequest = LocationRequest().setInterval(10000).setFastestInterval(2000)
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

    private fun handleConnectivityChange() {
        if (ConnectivityStateHolder.isConnected && !previousSate) {
            //showSnackBar(textView, "The network is back !")
            Sneaker.with(this) // Activity, Fragment or ViewGroup
                .setTitle("Connected!!")
                .setMessage("The network is back !")
                .sneakSuccess()
            google_sign_in_button.isClickable = true
        }

        if (!ConnectivityStateHolder.isConnected && previousSate) {
            //showSnackBar(textView, "No Network !")
            Sneaker.with(this) // Activity, Fragment or ViewGroup
                .setTitle("Connection lost")
                .setMessage("No Network!")
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
        startActivityForResult(signInIntent, RC_SIGN_IN)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
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
                goToMapActivity(it)
            }
        })

    }

    private fun createNewUser(authenticatedUser: User) {

        authViewModel.createUser(authenticatedUser)
        authViewModel.createdUserLiveData?.observe(this, Observer {
            if (it.isCreated!!) {
                toastMessage(it.name!!)
            }
            goToMapActivity(it)
        })

    }

    private fun toastMessage(name: String) {
        Toast.makeText(
            this,
            "Hi $name!\nYour account was successfully created.",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun goToMapActivity(user: User) {
        val intent = Intent(this@AuthActivity, MapsActivity2::class.java)
        intent.putExtra(USER, user)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }
}