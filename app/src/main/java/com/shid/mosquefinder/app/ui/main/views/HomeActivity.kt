package com.shid.mosquefinder.app.ui.main.views

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
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
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.ui.base.BaseActivity
import com.shid.mosquefinder.app.ui.main.view_models.SurahViewModel
import com.shid.mosquefinder.app.ui.notification.NotificationWorker
import com.shid.mosquefinder.app.utils.enums.Prayers
import com.shid.mosquefinder.app.utils.extensions.serializable
import com.shid.mosquefinder.app.utils.extensions.showToast
import com.shid.mosquefinder.app.utils.extensions.startActivity
import com.shid.mosquefinder.app.utils.helper_class.Constants
import com.shid.mosquefinder.app.utils.helper_class.FusedLocationWrapper
import com.shid.mosquefinder.app.utils.helper_class.SharePref
import com.shid.mosquefinder.app.utils.helper_class.singleton.Common
import com.shid.mosquefinder.app.utils.helper_class.singleton.Common.LOCATION_PERMISSION_REQUEST_CODE
import com.shid.mosquefinder.app.utils.helper_class.singleton.PermissionUtils
import com.shid.mosquefinder.data.model.User
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : BaseActivity() {
    private var user: User? = null
    var userPosition: LatLng? = null
    private var timeZone: Double? = null
    private val viewModel: SurahViewModel by viewModels()

    @Inject
    lateinit var sharedPref: SharePref

    @Inject
    @OptIn(ExperimentalCoroutinesApi::class)
    lateinit var fusedLocationWrapper: FusedLocationWrapper
    private var isFirstTime: Boolean? = null

    @Inject
    lateinit var workManager: WorkManager
    private val scope = lifecycleScope
    private val messageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.M)
        override fun onReceive(context: Context?, intent: Intent) {
            intent.extras?.getString(Constants.EXTRA_KEY_MESSAGE)?.let { showToast(it) }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        
        user = getUserFromIntent()
        timeZone = getTimeZone()
        isFirstTime = sharedPref.loadFirstTime()

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
        setPrayerTime()
        setClickListeners()
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(
                messageReceiver,
                IntentFilter(Constants.INTENT_FILTER_MESSAGE_RECEIVER)
            )

    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver)
    }

    private fun setPrayerTime() {
        scope.launch { viewModel.nextPray.collect { tv_countdown_time.text = it } }
        scope.launch { viewModel.descNextPray.collect { tv_next_prayer_name.text = it } }
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
        // workManager = WorkManager.getInstance(this)
        val saveRequest: PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.DAYS)
                .addTag(Constants.WORKER_TAG)
                .build()
        if (sharedPref.loadSwitchState()) {
            workManager.enqueueUniquePeriodicWork(
                Constants.WORKER_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                saveRequest
            )
        } else {
            workManager.cancelAllWorkByTag(SettingsActivity.SettingsFragment.WORKER_TAG)
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
                    LOCATION_PERMISSION_REQUEST_CODE
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
            showToast(getString(R.string.toast_permission))
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
            setPrayerUI(Prayers.DHUR, prayerTimes.thuhr().toString().dropLast(3))
        } else if (nowTime.after(dhurDate) && nowTime.before(asrDate)) {
            setPrayerUI(Prayers.ASR, prayerTimes.assr().toString().dropLast(3))
        } else if (nowTime.after(asrDate) && nowTime.before(maghribDate)) {
            setPrayerUI(Prayers.MAGHRIB, prayerTimes.maghrib().toString().dropLast(3))
        } else if (nowTime.after(maghribDate) && nowTime.before(ishaDate)) {
            setPrayerUI(Prayers.ISHA, prayerTimes.ishaa().toString().dropLast(3))
        } else {
            setPrayerUI(Prayers.FAJR, prayerTimes.fajr().toString().dropLast(3))
        }

    }

    private fun setPrayerUI(prayerName: Prayers, prayerTime: String) {
        tv_prayer_time.text = prayerName.prayer
        tv_home_salat_time.text = prayerTime
        viewModel.getIntervalText(prayerName, prayerTime)
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

    private fun goToQiblaActivity() {
        startActivity<CompassActivity>()
    }

    private fun goToFeedbackActivity() {
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
        return intent.serializable(Common.USER)
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