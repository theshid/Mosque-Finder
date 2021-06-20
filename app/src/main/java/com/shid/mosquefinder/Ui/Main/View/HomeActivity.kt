package com.shid.mosquefinder.Ui.Main.View

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.azan.Azan
import com.azan.AzanTimes
import com.azan.Method
import com.azan.astrologicalCalc.Location
import com.azan.astrologicalCalc.SimpleDate
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.shid.mosquefinder.Data.Model.User
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Utils.Common
import com.shid.mosquefinder.Utils.PermissionUtils
import com.shid.mosquefinder.Utils.SharePref
import com.shid.mosquefinder.Utils.setTransparentStatusBar
import fr.quentinklein.slt.LocationTracker
import fr.quentinklein.slt.ProviderError
import kotlinx.android.synthetic.main.activity_home.*
import okhttp3.internal.notify
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class HomeActivity : AppCompatActivity() {
    private var user: User? = null
    var userPosition: LatLng? = null
    private var timeZone: Double? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private lateinit var sharedPref: SharePref
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        Log.d("Testing", this.getExternalFilesDir(null).toString())

        timeZone = getTimeZone()
        sharedPref = SharePref(this)
        userPosition = sharedPref.loadSavedPosition()
       
        setLocationUtils()
        checkIfPermissionIsActive()
        user = getUserFromIntent()
        setClickListeners()

        //setTransparentStatusBar()

    }

    private fun checkIfPermissionIsActive() {
        when {
            PermissionUtils.isAccessFineLocationGranted(this) -> {
                when {
                    PermissionUtils.isLocationEnabled(this) -> {
                        permissionCheck()

                    }
                    else -> {
                        PermissionUtils.showGPSNotEnabledDialog(this)
                    }
                }
            }
            else -> {
                PermissionUtils.requestAccessFineLocationPermission(
                    this,
                    AuthActivity.LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun permissionCheck() {
        if (PermissionUtils.isAccessFineLocationGranted(this)) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: android.location.Location? ->
                    userPosition =
                        location?.longitude?.let {
                            LatLng(
                                location.latitude,
                                it
                            )
                        } // Got last known location. In some rare situations this can be null.
                    userPosition?.let {
                        calculatePrayerTime(it)

                        sharedPref.saveUserPosition(LatLng(it.latitude, it.longitude))
                    }
                }

            if (userPosition == null) {
                retrieveLocation()
            }
        } else {
            Toast.makeText(this, getString(R.string.toast_permission), Toast.LENGTH_LONG).show()
        }
    }

    private fun setLocationUtils() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest()
        retrieveLocation()
    }

    private fun getTimeZone(): Double {
        val cal: Calendar = Calendar.getInstance(Locale.getDefault())
        val offset = -(cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)) / (60 * 1000)
        return offset.toDouble()
    }

    private fun retrieveLocation() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    userPosition = LatLng(location.latitude, location.longitude)
                    calculatePrayerTime(userPosition!!)

                    sharedPref.saveUserPosition(userPosition!!)
                    // Update UI with location data
                    // ...
                }
            }


        }
    }

    private fun calculatePrayerTime(position: LatLng) {
        val date = SimpleDate(GregorianCalendar())
        val location = com.azan.astrologicalCalc.Location(
            position.latitude,
            position.longitude,
            timeZone!!,
            0
        )
        val azan = Azan(location, Method.EGYPT_SURVEY)
        val prayerTimes = azan.getPrayerTimes(date)

        setNextPrayer(prayerTimes)

    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }


    private fun initializePrayerDate(time:String):Date{
        val simpleDateFormat = SimpleDateFormat("HH:mm")
        val futureTime: Date = simpleDateFormat.parse(time)
        return futureTime
    }

    private fun setNextPrayer(prayerTimes:AzanTimes){
        val fajr = initializePrayerDate(prayerTimes.fajr().toString().dropLast(3))
        val dhur = initializePrayerDate(prayerTimes.thuhr().toString().dropLast(3))
        val asr = initializePrayerDate(prayerTimes.assr().toString().dropLast(3))
        val maghrib = initializePrayerDate(prayerTimes.maghrib().toString().dropLast(3))
        val isha = initializePrayerDate(prayerTimes.ishaa().toString().dropLast(3))

        val calendar = Calendar.getInstance()
        val simpleDateFormat = SimpleDateFormat("HH:mm")
        val nowTime = simpleDateFormat.parse(simpleDateFormat.format(calendar.time))
        if (nowTime.after(fajr) && nowTime.before(dhur)){
            tv_prayer_time.text = "Dhur"
            tv_home_salat_time.text = prayerTimes.thuhr().toString().dropLast(3)
        } else if (nowTime.after(dhur) && nowTime.before(asr)){
            tv_prayer_time.text = "Asr"
            tv_home_salat_time.text = prayerTimes.assr().toString().dropLast(3)
        } else if (nowTime.after(asr) && nowTime.before(maghrib)){
            tv_prayer_time.text = "Maghrib"
            tv_home_salat_time.text = prayerTimes.maghrib().toString().dropLast(3)
        } else if (nowTime.after(maghrib) && nowTime.before(isha)){
            tv_prayer_time.text = "Isha"
            tv_home_salat_time.text = prayerTimes.ishaa().toString().dropLast(3)
        } else {
            tv_prayer_time.text = "Fajr"
            tv_home_salat_time.text = prayerTimes.fajr().toString().dropLast(3)
        }
    }


    override fun onPause() {
        super.onPause()
        stopLocationUpdates()


    }

    override fun onResume() {
        super.onResume()
        if (PermissionUtils.isAccessFineLocationGranted(this)) {

            startLocationUpdates()
        }
    }


    private fun setClickListeners() {
        cardMap.setOnClickListener(View.OnClickListener {
            goToMapActivity()
        })

        cardQuran.setOnClickListener(View.OnClickListener {
            goToQuranActivity()
        })

        cardNames.setOnClickListener(View.OnClickListener {
            goToNamesActivity()
        })

        cardAzkhar.setOnClickListener(View.OnClickListener {
            goToAzkharActivity()
        })

        cardMosques.setOnClickListener(View.OnClickListener {
            goToMosquesActivity()
        })

        cardQuotes.setOnClickListener(View.OnClickListener {
            goToQuotesActivity()
        })

        cardPrayerTime.setOnClickListener(View.OnClickListener {
            goToPrayerActivity()
        })
    }

    private fun goToMapActivity() {
        val intent = Intent(this, MapsActivity2::class.java)
        intent.putExtra(Common.USER, user)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun goToQuranActivity() {
        val intent = Intent(this, SurahActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun goToNamesActivity() {
        val intent = Intent(this, NamesActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun goToAzkharActivity() {
        val intent = Intent(this, CategoriesActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun goToMosquesActivity() {
        val intent = Intent(this, BeautifulMosquesActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun goToQuotesActivity() {
        val intent = Intent(this, QuotesActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun goToPrayerActivity() {
        val intent = Intent(this, PrayerActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun getUserFromIntent(): User? {
        return intent.getSerializableExtra(Common.USER) as com.shid.mosquefinder.Data.Model.User
    }
}