package com.shid.mosquefinder.Ui.Main.View

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.afollestad.materialdialogs.MaterialDialog
import com.azan.Azan
import com.azan.AzanTimes
import com.azan.Method
import com.azan.astrologicalCalc.SimpleDate
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.judemanutd.autostarter.AutoStartPermissionHelper
import com.shid.mosquefinder.Data.Model.User
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Ui.Base.SurahViewModelFactory
import com.shid.mosquefinder.Ui.Main.ViewModel.SurahViewModel
import com.shid.mosquefinder.Ui.notification.NotificationWorker
import com.shid.mosquefinder.Utils.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class HomeActivity : AppCompatActivity() {
    private var user: User? = null
    var userPosition: LatLng? = null
    private var timeZone: Double? = null
    private lateinit var viewModel: SurahViewModel
    private lateinit var sharedPref: SharePref
    @OptIn(ExperimentalCoroutinesApi::class)
    private lateinit var fusedLocationWrapper: FusedLocationWrapper
    private var isFirstTime: Boolean? = null
    private lateinit var workManager: WorkManager
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setViewModel()
        user = getUserFromIntent()
        timeZone = getTimeZone()
        sharedPref = SharePref(this)
        isFirstTime = sharedPref.loadFirstTime()
        fusedLocationWrapper = fusedLocationWrapper()
        displayAutoStartDialog()
        setWorkManagerNotification()
        checkIfPermissionIsActive()
        setClickListeners()
    }

    private fun displayAutoStartDialog(){
        if (isFirstTime == true &&  AutoStartPermissionHelper.getInstance().isAutoStartPermissionAvailable(this)){
            dialogSettings()
            sharedPref.setFirstTime(false)
        } else{
            sharedPref.setFirstTime(false)
        }
    }

    private fun setWorkManagerNotification() {
        workManager = WorkManager.getInstance(this)
        val saveRequest: PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.DAYS)
                .addTag("notification")
                .build()
        if (sharedPref.loadSwitchState()) {
            workManager.enqueueUniquePeriodicWork("Daily Ayah",ExistingPeriodicWorkPolicy.KEEP,saveRequest)
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
                    AuthActivity.LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    private fun permissionCheck(fusedLocationWrapper: FusedLocationWrapper) {
        if (PermissionUtils.isAccessFineLocationGranted(this)) {
            getUserLocation(fusedLocationWrapper)
            if (userPosition == null) {
                userPosition = sharedPref.loadSavedPosition()
            }
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
                calculatePrayerTime(it)
                sharedPref.saveUserPosition(LatLng(it.latitude, it.longitude))
            }
        }
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
        return tz.getOffset(now.time) / 3600000.0
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

    private fun initializePrayerDate(time: String): Date {
        val simpleDateFormat = SimpleDateFormat("HH:mm")
        return simpleDateFormat.parse(time)
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

    private fun setClickListeners() {
        cardMap.setOnClickListener{
            goToMapActivity()
        }

        cardBlog.setOnClickListener{
            goToBlogActivity()
        }

        cardQuran.setOnClickListener {
            goToQuranActivity()
        }

        cardNames.setOnClickListener {
            goToNamesActivity()
        }

        cardAzkhar.setOnClickListener {
            goToAzkharActivity()
        }

        cardMosques.setOnClickListener{
            goToMosquesActivity()
        }

        cardQuotes.setOnClickListener {
            goToQuotesActivity()
        }

        cardPrayerTime.setOnClickListener {
            goToPrayerActivity()
        }

        btn_settings.setOnClickListener {
            goToSettings()
        }
    }

    private fun goToBlogActivity() {
        startActivity<BlogActivity>()
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