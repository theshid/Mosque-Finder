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
import androidx.lifecycle.ViewModelProvider
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.afollestad.materialdialogs.MaterialDialog
import com.azan.Azan
import com.azan.AzanTimes
import com.azan.Method
import com.azan.astrologicalCalc.Location
import com.azan.astrologicalCalc.SimpleDate
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.judemanutd.autostarter.AutoStartPermissionHelper
import com.shid.mosquefinder.Data.Model.User
import com.shid.mosquefinder.Data.database.entities.Surah
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Ui.Base.SurahViewModelFactory
import com.shid.mosquefinder.Ui.Main.ViewModel.SurahViewModel
import com.shid.mosquefinder.Ui.notification.NotificationWorker
import com.shid.mosquefinder.Utils.Common
import com.shid.mosquefinder.Utils.PermissionUtils
import com.shid.mosquefinder.Utils.SharePref
import com.shid.mosquefinder.Utils.setTransparentStatusBar
import fr.quentinklein.slt.LocationTracker
import fr.quentinklein.slt.ProviderError
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_surah.*
import okhttp3.internal.notify
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class HomeActivity : AppCompatActivity() {
    private var user: User? = null
    var userPosition: LatLng? = null
    private var timeZone: Double? = null
    private lateinit var viewModel: SurahViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private lateinit var sharedPref: SharePref
    private var isFirstTime: Boolean? = null
    private lateinit var workManager: WorkManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setViewModel()
        timeZone = getTimeZone()
        sharedPref = SharePref(this)
        isFirstTime = sharedPref.loadFirstTime()
        userPosition = sharedPref.loadSavedPosition()
        if (isFirstTime == true &&  AutoStartPermissionHelper.getInstance().isAutoStartPermissionAvailable(this)){
            dialogSettings()
            sharedPref.setFirstTime(false)
        } else{
            sharedPref.setFirstTime(false)
        }
        setWorkManagerNotification()
        setLocationUtils()
        checkIfPermissionIsActive()
        user = getUserFromIntent()
        setClickListeners()

        //setTransparentStatusBar()

    }

    private fun setWorkManagerNotification() {
        workManager = WorkManager.getInstance(this)
        val saveRequest: PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.DAYS)
                .addTag("notification")
                .build()
        if (sharedPref.loadSwitchState()) {
            workManager.enqueue(saveRequest)
        } else {
            workManager.cancelAllWorkByTag(SettingsActivity.SettingsFragment.WORKER_TAG)
        }
    }

    private fun setViewModel() {
        viewModel = ViewModelProvider(
            this,
            SurahViewModelFactory(application)
        ).get(SurahViewModel::class.java)

        viewModel.getSurahs()
        viewModel.surahList.observe(this, androidx.lifecycle.Observer {
            Timber.d("value of :$it")
        })

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
        locationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 50
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            maxWaitTime= 5000
        }
        retrieveLocation()
    }

    private fun dialogSettings(){
        MaterialDialog(this).show {
            title(text = getString(R.string.title_dialog))
            message(text = getString(R.string.auto_start))
            positiveButton(text = getString(R.string.yes)) { dialog ->
                dialog.cancel()
                AutoStartPermissionHelper.getInstance().getAutoStartPermission(this@HomeActivity)
            }
            negativeButton(text = getString(R.string.cancel)) { dialog ->
                dialog.cancel()

            }
            icon(R.drawable.logo2)
        }
    }

    private fun getTimeZone(): Double {
        val tz = TimeZone.getDefault()
        val now = Date()
        val offsetFromUtc = tz.getOffset(now.time) / 3600000.0
        return offsetFromUtc
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

        btn_settings.setOnClickListener(View.OnClickListener {
            goToSettings()
        })
    }

    private fun goToSettings() {
        val intent = Intent(this,SettingsActivity::class.java)
        startActivity(intent)
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
        intent.putExtra("user",user)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun getUserFromIntent(): User? {
        return intent.getSerializableExtra(Common.USER) as com.shid.mosquefinder.Data.Model.User
    }
}