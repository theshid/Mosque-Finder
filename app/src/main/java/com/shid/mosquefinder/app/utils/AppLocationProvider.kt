package com.shid.mosquefinder.app.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import java.util.*

@SuppressLint("MissingPermission")
class AppLocationProvider {

    private lateinit var timer: Timer
    private var locationManager: LocationManager? = null
    private lateinit var locationCallBack: LocationCallBack
    private var gpsEnabled = false
    private var networkEnabled = false

    private var locationListener: LocationListener = object : LocationListener {

        override fun onLocationChanged(location: Location) {
            timer.cancel()
            locationCallBack.locationResult(location)
        }

        override fun onProviderDisabled(provider: String) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
    }

    fun getLocation(context : Context, callBack: LocationCallBack): Boolean {
        locationCallBack = callBack
        if (locationManager == null)
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?

        //exceptions will be thrown if provider is not permitted.
        try {
            gpsEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        try {
            networkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        //don't start listeners if no provider is enabled
        if (!gpsEnabled && !networkEnabled)
            return false

        val criteria = Criteria()
        if (gpsEnabled) {
            criteria.accuracy = Criteria.ACCURACY_FINE
        } else {
            criteria.accuracy = Criteria.ACCURACY_COARSE
        }
        locationManager!!.requestSingleUpdate(criteria, locationListener, null)

        timer = Timer()
        timer.schedule(GetLastKnownLocation(), 5000)
        return true
    }

    inner class GetLastKnownLocation : TimerTask() {

        override fun run() {
            locationManager!!.removeUpdates(locationListener)

            var netLoc: Location? = null
            var gpsLoc: Location? = null

            if (gpsEnabled)
                gpsLoc = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (networkEnabled)
                netLoc = locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            //check which value use the latest one
            if (gpsLoc != null && netLoc != null) {
                if (gpsLoc.time > netLoc.time)
                    locationCallBack.locationResult(gpsLoc)
                else
                    locationCallBack.locationResult(netLoc)
                return
            }

            if (gpsLoc != null) {
                locationCallBack.locationResult(gpsLoc)
                return
            }
            if (netLoc != null) {
                locationCallBack.locationResult(netLoc)
                return
            }
            locationCallBack.locationResult(null)
        }
    }

    interface LocationCallBack {
        fun locationResult(location: Location?)
    }
}