package com.shid.mosquefinder.Ui.Main.View

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.afollestad.materialdialogs.MaterialDialog
import com.azan.Azan
import com.azan.AzanTimes
import com.azan.Method
import com.azan.astrologicalCalc.SimpleDate
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.model.LatLng
import com.judemanutd.autostarter.AutoStartPermissionHelper
import com.shid.mosquefinder.Data.Model.User
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Ui.Base.SurahViewModelFactory
import com.shid.mosquefinder.Ui.Main.ViewModel.SurahViewModel
import com.shid.mosquefinder.Ui.notification.NotificationWorker
import com.shid.mosquefinder.Utils.*
import com.shid.mosquefinder.Utils.enums.Prayers
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
    private val scope = lifecycleScope
    private val messageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.M)
        override fun onReceive(context: Context?, intent: Intent) {
            /*val pendingIntent = context?.let { NotificationHelper.getPendingIntent(it) }
            intent.extras?.getString("message")
                ?.let { NotificationHelper.showNotification(context!!,pendingIntent!!, it) }*/
            intent.extras?.getString("message")?.let { showToast(it) }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
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
        val bundle = intent.extras
        if (bundle != null) {
            Timber.d("bundle coming in:%s", bundle.keySet())
            if (bundle.getString("text") != null) {
                startActivity<BlogActivity>()
            }

        }

        displayAutoStartDialog()
        setWorkManagerNotification()
        checkIfPermissionIsActive()
        scope.launch { viewModel.nextPray.collect { tv_countdown_time.text = it } }
        scope.launch { viewModel.descNextPray.collect { tv_next_prayer_name.text = it } }
        setClickListeners()
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(messageReceiver, IntentFilter("MyData"))

    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver)
    }

    private fun displayAutoStartDialog() {
        if (isFirstTime == true && AutoStartPermissionHelper.getInstance()
                .isAutoStartPermissionAvailable(this)
        ) {
            dialogSettings()
            sharedPref.setFirstTime(false)
        } else {
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
            workManager.enqueueUniquePeriodicWork(
                "Daily Ayah",
                ExistingPeriodicWorkPolicy.KEEP,
                saveRequest
            )
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

        if (checkGooglePlayServices()) {
            viewModel.update()
        } else {
            //You won't be able to send notifications to this device
            Timber.w("Device doesn't have google play services")
        }


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
        scope.launch {
            val location = fusedLocationWrapper.awaitLastLocation()
            userPosition = LatLng(location.latitude, location.longitude)
            userPosition?.let {
                calculatePrayerTime(it)
                sharedPref.saveUserPosition(LatLng(it.latitude, it.longitude))
            }
        }
    }

    private fun dialogSettings() {
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


    private fun setNextPrayer(prayerTimes: AzanTimes) {
        val fajr = prayerTimes.fajr().toString().dropLast(3)
        val dhur = prayerTimes.thuhr().toString().dropLast(3)
        val asr = prayerTimes.assr().toString().dropLast(3)
        val maghrib = prayerTimes.maghrib().toString().dropLast(3)
        val isha = prayerTimes.ishaa().toString().dropLast(3)

        val fajrDate = initializePrayerDate(fajr)
        val dhurDate = initializePrayerDate(dhur)
        val asrDate = initializePrayerDate(asr)
        val maghribDate = initializePrayerDate(maghrib)
        val ishaDate = initializePrayerDate(isha)

        sharedPref.saveFajr(fajr)
        sharedPref.saveDhur(dhur)
        sharedPref.saveAsr(asr)
        sharedPref.saveMaghrib(maghrib)
        sharedPref.saveIsha(isha)

        val calendar = Calendar.getInstance()
        val simpleDateFormat = SimpleDateFormat("HH:mm")
        val nowTime = simpleDateFormat.parse(simpleDateFormat.format(calendar.time))
        if (nowTime.after(fajrDate) && nowTime.before(dhurDate)) {
            val salatTime = prayerTimes.thuhr().toString().dropLast(3)
            tv_prayer_time.text = Prayers.DHUR.prayer
            tv_home_salat_time.text = salatTime
            viewModel.getIntervalText(Prayers.DHUR, salatTime)
        } else if (nowTime.after(dhurDate) && nowTime.before(asrDate)) {
            val salatTime = prayerTimes.assr().toString().dropLast(3)
            tv_prayer_time.text = Prayers.ASR.prayer
            tv_home_salat_time.text = prayerTimes.assr().toString().dropLast(3)
            viewModel.getIntervalText(Prayers.ASR, salatTime)
        } else if (nowTime.after(asrDate) && nowTime.before(maghribDate)) {
            val salatTime = prayerTimes.maghrib().toString().dropLast(3)
            tv_prayer_time.text = Prayers.MAGHRIB.prayer
            tv_home_salat_time.text = prayerTimes.maghrib().toString().dropLast(3)
            viewModel.getIntervalText(Prayers.MAGHRIB, salatTime)
        } else if (nowTime.after(maghribDate) && nowTime.before(ishaDate)) {
            val salatTime = prayerTimes.ishaa().toString().dropLast(3)

            tv_prayer_time.text = Prayers.ISHA.prayer
            tv_home_salat_time.text = prayerTimes.ishaa().toString().dropLast(3)
            viewModel.getIntervalText(Prayers.ISHA, salatTime)
        } else {
            val salatTime = prayerTimes.fajr().toString().dropLast(3)

            tv_prayer_time.text = Prayers.FAJR.prayer
            tv_home_salat_time.text = prayerTimes.fajr().toString().dropLast(3)
            viewModel.getIntervalText(Prayers.FAJR, salatTime)
        }

    }

    private fun setClickListeners() {
        cardMap.setOnClickListener {
            goToMapActivity()
        }

        cardBlog.setOnClickListener {
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

        cardMosques.setOnClickListener {
            goToMosquesActivity()
        }

        cardQuotes.setOnClickListener {
            goToQuotesActivity()
        }

        cardQibla.setOnClickListener {
            goToQiblaActivity()
        }

        cardFeedback.setOnClickListener {
            goToFeedbackActivity()
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

    private fun goToQiblaActivity(){
        startActivity<CompassActivity>()
    }

    private fun goToFeedbackActivity(){
        startActivity<FeedbackActivity>()
    }

    private fun goToSettings() {
        startActivity<SettingsActivity>()
    }

    private fun goToMapActivity() {
        startActivity<MapsActivity2> {
            putExtra(Common.USER, user)
        }
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun goToQuranActivity() {
        startActivity<SurahActivity>()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun goToNamesActivity() {
        startActivity<NamesActivity>()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun goToAzkharActivity() {
        startActivity<CategoriesActivity>()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun goToMosquesActivity() {
        startActivity<BeautifulMosquesActivity>()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun goToQuotesActivity() {
        startActivity<QuotesActivity>()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun goToPrayerActivity() {
        startActivity<PrayerActivity> {
            putExtra("user", user)
        }
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun getUserFromIntent(): User? {
        return intent.getSerializableExtra(Common.USER) as com.shid.mosquefinder.Data.Model.User
    }

    private fun checkGooglePlayServices(): Boolean {
        // 1
        val status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        // 2
        return if (status != ConnectionResult.SUCCESS) {
            Timber.e("Error")
            // ask user to update google play services and manage the error.
            false
        } else {
            // 3
            Timber.i("Google play services updated")
            true
        }
    }

}