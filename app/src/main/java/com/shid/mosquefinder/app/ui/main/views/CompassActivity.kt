package com.shid.mosquefinder.app.ui.main.views

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.model.LatLng
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.ui.base.BaseActivity
import com.shid.mosquefinder.app.utils.extensions.showToast
import com.shid.mosquefinder.app.utils.helper_class.Compass
import com.shid.mosquefinder.app.utils.helper_class.FusedLocationWrapper
import com.shid.mosquefinder.app.utils.helper_class.SOTWFormatter
import com.shid.mosquefinder.app.utils.helper_class.SharePref
import com.shid.mosquefinder.app.utils.helper_class.singleton.PermissionUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_compass.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class CompassActivity : BaseActivity() {
    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 999
        var userPosition: LatLng = LatLng(0.0, 0.0)
    }

    private var compass: Compass? = null
    private var arrowView: ImageView? = null
    private var dialView: ImageView? = null

    @Inject
    lateinit var sharedPref: SharePref

    @OptIn(ExperimentalCoroutinesApi::class)
    @Inject
    lateinit var fusedLocationWrapper: FusedLocationWrapper

    private var sotwLabel: TextView? = null // SOTW is for "side of the world"
    private var currentAzimuth = 0f
    private var sotwFormatter: SOTWFormatter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compass)

        sotwFormatter = SOTWFormatter(this)
        arrowView = findViewById(R.id.main_image_hands)
        dialView = findViewById(R.id.main_image_dial)
        sotwLabel = findViewById(R.id.sotw_label)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        checkIfPermissionIsActive()
        setupCompass()
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    private fun checkIfPermissionIsActive() {
        when {
            PermissionUtils.isAccessFineLocationGranted(this) -> {
                when {
                    PermissionUtils.isLocationEnabled(this) -> {
                        getUserLocation(fusedLocationWrapper)
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    private fun getUserLocation(fusedLocationWrapper: FusedLocationWrapper) {
        this.lifecycleScope.launch {
            val location = fusedLocationWrapper.awaitLastLocation()
            userPosition = LatLng(location.latitude, location.longitude)
            userPosition.let {
                sharedPref.saveUserPosition(LatLng(it.latitude, it.longitude))
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
                            getUserLocation(fusedLocationWrapper)
                        }
                        else -> {
                            PermissionUtils.showGPSNotEnabledDialog(this)
                        }
                    }
                } else {
                    showToast(getString(R.string.permisson_not_granted))
                }
            }
        }
    }


    override fun onStart() {
        super.onStart()
        Timber.d("start compass")
        compass!!.start()
    }

    override fun onPause() {
        super.onPause()
        compass!!.stop()
    }

    override fun onResume() {
        super.onResume()
        compass!!.start()
    }

    override fun onStop() {
        super.onStop()
        Timber.d("stop compass")
        compass!!.stop()
    }

    private fun setupCompass() {
        compass = Compass(this)
        val cl: Compass.CompassListener = getCompassListener()
        compass!!.setListener(cl)
    }

    private fun adjustArrow(azimuth: Float) {
        Timber.d("will set rotation from $currentAzimuth to $azimuth")
        val an: Animation = RotateAnimation(
            -currentAzimuth, -azimuth,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
            0.5f
        )
        currentAzimuth = azimuth

        an.apply {
            duration = 500
            repeatCount = 0
            fillAfter = true
        }

        arrowView!!.startAnimation(an)
    }

    private fun adjustDial(azimuth: Float) {
        Timber.d("will set rotation from $currentAzimuth to $azimuth")
        val an: Animation = RotateAnimation(
            -currentAzimuth, -azimuth,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
            0.5f
        )
        currentAzimuth = azimuth
        an.duration = 500
        an.repeatCount = 0
        an.fillAfter = true

        dialView!!.startAnimation(an)
    }

    private fun adjustSotwLabel(azimuth: Float) {
        sotwLabel!!.text = sotwFormatter!!.format(azimuth)
    }

    private fun getCompassListener(): Compass.CompassListener {
        return object : Compass.CompassListener {
            override fun onNewAzimuth(azimuth: Float) {
                // UI updates only in UI thread
                // https://stackoverflow.com/q/11140285/444966
                runOnUiThread {
                    adjustDial(azimuth)
                    adjustArrow(azimuth)
                    adjustSotwLabel(azimuth)
                }
            }

        }
    }
}