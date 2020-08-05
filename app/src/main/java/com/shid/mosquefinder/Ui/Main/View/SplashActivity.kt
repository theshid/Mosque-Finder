package com.shid.mosquefinder.Ui.Main.View

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.shid.mosquefinder.Data.Model.User
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Ui.Base.SplashViewModelFactory
import com.shid.mosquefinder.Ui.Main.ViewModel.SplashViewModel
import com.shid.mosquefinder.Utils.Common.USER
import com.shid.mosquefinder.Utils.PermissionUtils


class SplashActivity : AppCompatActivity() {
    private lateinit var splashViewModel: SplashViewModel
    private lateinit var splashViewModelFactory: SplashViewModelFactory
    companion object{
        var userPosition:LatLng?= null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        initSplashViewModel();
        val handler = Handler()
        handler.postDelayed({

            checkIfUserIsAuthenticated();
            if (PermissionUtils.isAccessFineLocationGranted(this)){
                setUpLocationListener()
            }else{
                Toast.makeText(this,"Noo Permission yet",Toast.LENGTH_LONG).show()
            }

        }, 3000)

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
                        Log.d("Splash", "position=" + location.latitude + "" + location.longitude)
                    }
                    // Few more things we can do here:
                    // For example: Update the location of user on server
                }
            },
            Looper.myLooper()
        )
    }

    private fun checkIfUserIsAuthenticated() {
        splashViewModel.checkIfUserIsAuthenticated()
        splashViewModel.isUserAuthenticatedLiveData!!.observe(this,
            Observer { user: User ->
                if (!user.isAuthenticated!!) {
                    goToAuthInActivity()
                    finish()
                } else {
                    getUserFromDatabase(user.uid)
                }
            }
        )
    }

    private fun getUserFromDatabase(uid: String) {
        splashViewModel.setUid(uid)
        splashViewModel.userLiveData!!.observe(
            this,
            Observer { user: User? ->
                goToMapActivity(user)
                finish()
            }
        )
    }

    private fun goToMapActivity(user: User?) {
        val intent = Intent(this@SplashActivity, MapsActivity2::class.java)
        intent.putExtra(USER, user)
        startActivity(intent)
    }

    private fun goToAuthInActivity() {
        val intent = Intent(this@SplashActivity, AuthActivity::class.java)
        startActivity(intent)
    }

    private fun initSplashViewModel() {
        splashViewModelFactory =
            SplashViewModelFactory(application)
        splashViewModel = ViewModelProvider(
            this,
            splashViewModelFactory
        ).get(SplashViewModel(application)::class.java)
    }
}