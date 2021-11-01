package com.shid.mosquefinder.Utils

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.shid.mosquefinder.Ui.Main.View.SplashActivity
import timber.log.Timber
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin


@SuppressLint("MissingPermission")
class Compass(context: Context) : SensorEventListener {
    private val TAG = "Compass"

    interface CompassListener {
        fun onNewAzimuth(azimuth: Float)
    }

    private var listener: CompassListener? = null

    private var sensorManager: SensorManager? = null
    private var gsensor: Sensor? = null
    private var msensor: Sensor? = null

    private val mGravity = FloatArray(3)
    private val mGeomagnetic = FloatArray(3)
    private val R = FloatArray(9)
    private val I = FloatArray(9)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userPosition:LatLng = LatLng(0.0,0.0)


    private var azimuth = 0f
    private var azimuthFix = 0f
    private val kaabaLocation = LatLng(21.422487, 39.826206)
    private var sharePref: SharePref? = null

    init {
        sensorManager = context
            .getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gsensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        msensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                if (location != null) {
                    userPosition = LatLng(location.latitude, location.longitude)
                }// Got last known location. In some rare situations this can be null.
            }
        if (sharePref?.loadSavedPosition() != null){
            userPosition = sharePref!!.loadSavedPosition()
        }
        //userPosition = sharePref?.loadSavedPosition()

    }


    @SuppressLint("MissingPermission")
    fun start() {
        sensorManager!!.registerListener(
            this, gsensor,
            SensorManager.SENSOR_DELAY_GAME
        )
        sensorManager!!.registerListener(
            this, msensor,
            SensorManager.SENSOR_DELAY_GAME
        )


    }

    fun stop() {
        sensorManager!!.unregisterListener(this)
    }

    fun setAzimuthFix(fix: Float) {
        azimuthFix = fix
    }

    fun resetAzimuthFix() {
        setAzimuthFix(0f)
    }

    fun setListener(l: CompassListener?) {
        listener = l
    }

    override fun onSensorChanged(event: SensorEvent) {
        val alpha = 0.97f
        synchronized(this) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                mGravity[0] = alpha * mGravity[0] + (1 - alpha) * event.values[0]
                mGravity[1] = alpha * mGravity[1] + (1 - alpha) * event.values[1]
                mGravity[2] = alpha * mGravity[2] + (1 - alpha) * event.values[2]

                // mGravity = event.values;

                // Log.e(TAG, Float.toString(mGravity[0]));
            }
            if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                // mGeomagnetic = event.values;
                mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha) * event.values[0]
                mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha) * event.values[1]
                mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha) * event.values[2]
                // Log.e(TAG, Float.toString(event.values[0]));
            }
            val success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic)
            if (success) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(R, orientation)
                // Log.d(TAG, "azimuth (rad): " + azimuth);
                azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat() // orientation
                azimuth = (azimuth + azimuthFix + 360) % 360
                azimuth -= if (userPosition!= LatLng(0.0,0.0)){
                    Timber.d("user location:%s %s", userPosition.latitude.toString() ,
                        userPosition.longitude.toString())

                    bearing(userPosition.latitude,
                        userPosition.longitude,kaabaLocation.latitude,kaabaLocation.longitude)
                        .toFloat()
                } else{
                    Timber.d("user location:%s %s",
                        SplashActivity.userPosition!!.latitude.toString() ,
                        SplashActivity.userPosition!!.longitude.toString())

                    bearing(SplashActivity.userPosition!!.latitude,
                        SplashActivity.userPosition!!.longitude,kaabaLocation.latitude,kaabaLocation.longitude)
                        .toFloat()
                }

                // Log.d(TAG, "azimuth (deg): " + azimuth);
                if (listener != null) {
                    listener!!.onNewAzimuth(azimuth)
                }
            }
        }
    }

     private fun bearing(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double
    ): Double {
        val latitude1 = Math.toRadians(startLat)
        val latitude2 = Math.toRadians(endLat)
        val longDiff = Math.toRadians(endLng - startLng)
        val y = sin(longDiff) * cos(latitude2)
        val x =
            cos(latitude1) * sin(latitude2) - sin(latitude1) * cos(latitude2) * cos(
                longDiff
            )
        return (Math.toDegrees(atan2(y, x)) + 360) % 360
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}