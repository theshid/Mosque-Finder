package com.shid.mosquefinder.Ui.Main.View

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.shid.mosquefinder.Data.Model.User
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Ui.Base.SplashViewModelFactory
import com.shid.mosquefinder.Ui.Main.ViewModel.SplashViewModel
import com.shid.mosquefinder.Utils.Common.USER
import com.shid.mosquefinder.Utils.PermissionUtils
import com.shid.mosquefinder.Utils.setTransparentStatusBar
import fr.quentinklein.slt.LocationTracker
import fr.quentinklein.slt.ProviderError


class SplashActivity : AppCompatActivity() {
    private lateinit var splashViewModel: SplashViewModel
    private lateinit var splashViewModelFactory: SplashViewModelFactory
    private  var fusedLocationProviderClient:FusedLocationProviderClient ?= null
    private lateinit var locationCallback: LocationCallback

    companion object{
        var userPosition:LatLng?= null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        initSplashViewModel()
        setTransparentStatusBar()


        if (PermissionUtils.isAccessFineLocationGranted(this)){
            setUpLocationListener()

        }else{
            Toast.makeText(this,getString(R.string.toast_permission),Toast.LENGTH_LONG).show()
        }

        val handler = Handler()
        handler.postDelayed({

            checkIfUserIsAuthenticated();


        }, 3000)

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
                    userPosition = LatLng(location.latitude, location.longitude)
                    Log.d("Splash", "position=" + location.latitude + "" + location.longitude)
                    Log.d("Splash","accuracy:"+location.accuracy)
                }
                // Few more things we can do here:
                // For example: Update the location of user on server
            }
        }

        fusedLocationProviderClient!!.requestLocationUpdates(
            locationRequest,locationCallback,
            Looper.myLooper())
    }

    override fun onStop() {
        super.onStop()


    }

    override fun onPause() {
        super.onPause()
        if (fusedLocationProviderClient != null){
            fusedLocationProviderClient!!.removeLocationUpdates(locationCallback)
        }

    }

    override fun onResume() {
        super.onResume()
        if (fusedLocationProviderClient != null){
            setUpLocationListener()
        }
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
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun goToAuthInActivity() {
        val intent = Intent(this@SplashActivity, AuthActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
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