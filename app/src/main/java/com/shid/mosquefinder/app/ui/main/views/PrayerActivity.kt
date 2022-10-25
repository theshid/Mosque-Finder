package com.shid.mosquefinder.app.ui.main.views

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.azan.Azan
import com.azan.Method
import com.azan.astrologicalCalc.SimpleDate
import com.elconfidencial.bubbleshowcase.BubbleShowCaseBuilder
import com.elconfidencial.bubbleshowcase.BubbleShowCaseSequence
import com.google.android.gms.maps.model.LatLng
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.ui.base.BaseActivity
import com.shid.mosquefinder.app.ui.services.PrayerAlarmBroadcastReceiver
import com.shid.mosquefinder.app.utils.extensions.startActivity
import com.shid.mosquefinder.app.utils.helper_class.FusedLocationWrapper
import com.shid.mosquefinder.app.utils.helper_class.SharePref
import com.shid.mosquefinder.app.utils.helper_class.singleton.Common
import com.shid.mosquefinder.app.utils.helper_class.singleton.PermissionUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_prayer.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class PrayerActivity : BaseActivity() {

    private var userPosition: LatLng? = null
    private lateinit var date: SimpleDate
    private var timeZone: Double? = null
    private var isFirstTime: Boolean? = null

    @Inject
    lateinit var sharedPref: SharePref
    private var _broadcastReceiver: BroadcastReceiver? = null
    private val _sdfWatchTime = SimpleDateFormat("HH:mm")


    private val prayerAlarm = PrayerAlarmBroadcastReceiver()

    @Inject
    @OptIn(ExperimentalCoroutinesApi::class)
    lateinit var fusedLocationWrapper: FusedLocationWrapper


    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prayer)
        isFirstTime = sharedPref.loadFirstTimePrayerNotification()
        permissionCheck(fusedLocationWrapper)
        setUI()
        clickListeners()

    }

    override fun onStart() {
        super.onStart()
        _broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action?.compareTo(Intent.ACTION_TIME_TICK) == 0) {
                    textTime.text = _sdfWatchTime.format(Date())
                }
            }
        }

        registerReceiver(_broadcastReceiver, IntentFilter(Intent.ACTION_TIME_TICK))
    }

    override fun onStop() {
        super.onStop()
        if (_broadcastReceiver != null) {
            unregisterReceiver(_broadcastReceiver)
        }
    }

    private fun setUI() {
        if (isFirstTime == true) {
            activateShowcase()
            sharedPref.setFirstTimePrayerNotification(false)
        }
        if (userPosition == null) {
            userPosition = sharedPref.loadSavedPosition()
        }

        val fajrIsReminderSet = sharedPref.loadFajrState()
        val dhurIsReminderSet = sharedPref.loadDhurState()
        val asrIsReminderSet = sharedPref.loadAsrState()
        val maghribIsReminderSet = sharedPref.loadMaghribState()
        val ishaIsReminderSet = sharedPref.loadIshaState()

        ivSoundFajr.setImageResource(if (fajrIsReminderSet) R.drawable.ic_sound_on else R.drawable.ic_sound_off)
        ivSoundDhur.setImageResource(if (dhurIsReminderSet) R.drawable.ic_sound_on else R.drawable.ic_sound_off)
        ivSoundAsr.setImageResource(if (asrIsReminderSet) R.drawable.ic_sound_on else R.drawable.ic_sound_off)
        ivSoundMaghrib.setImageResource(if (maghribIsReminderSet) R.drawable.ic_sound_on else R.drawable.ic_sound_off)
        ivSoundIsha.setImageResource(if (ishaIsReminderSet) R.drawable.ic_sound_on else R.drawable.ic_sound_off)

        Timber.d("userPosition:$userPosition")
        timeZone = getTimeZone()
        Timber.d("timeZone:${timeZone.toString()}")
        calculatePrayerTime(userPosition!!)
        findCity(userPosition!!.latitude, userPosition!!.longitude)
        setDate()
    }

    private fun clickListeners() {
        val fajr = sharedPref.loadFajr()
        val dhur = sharedPref.loadDhur()
        val asr = sharedPref.loadAsr()
        val maghrib = sharedPref.loadMaghrib()
        val isha = sharedPref.loadIsha()

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        menu_settings.setOnClickListener {
            goToSettings()
        }

        btn_qibla.setOnClickListener {
            goToQibla()
        }

        btn_activate_notification.setOnClickListener {
            val allNotificationState = sharedPref.loadIsAllPrayersNotificationActivated()

            setPrayerNotification(allNotificationState, fajr, Common.FAJR, Common.FAJR_INDEX)
            setPrayerNotification(allNotificationState, dhur, Common.DHUR, Common.DHUR_INDEX)
            setPrayerNotification(allNotificationState, asr, Common.ASR, Common.ASR_INDEX)
            setPrayerNotification(allNotificationState, maghrib, Common.MAGHRIB, Common.MAGHRIB_INDEX)
            setPrayerNotification(allNotificationState, isha, Common.ISHA, Common.ISHA_INDEX)

            ivSoundFajr.setImageResource(setReminderStateImage(allNotificationState))
            ivSoundDhur.setImageResource(setReminderStateImage(allNotificationState))
            ivSoundAsr.setImageResource(setReminderStateImage(allNotificationState))
            ivSoundMaghrib.setImageResource(setReminderStateImage(allNotificationState))
            ivSoundIsha.setImageResource(setReminderStateImage(allNotificationState))

            if (allNotificationState) {
                btn_activate_notification.text = getString(R.string.activate_all_notification)
            } else {
                btn_activate_notification.text = getString(R.string.disactivate_all_notification)
            }

        }

        btnSoundFajr.setOnClickListener {
            val isReminderSet = sharedPref.loadFajrState()
            ivSoundFajr.setImageResource(setReminderStateImage(isReminderSet))
            setPrayerNotification(isReminderSet, fajr, Common.FAJR, Common.FAJR_INDEX)
        }

        btnSoundDhur.setOnClickListener {
            val isReminderSet = sharedPref.loadDhurState()
            ivSoundDhur.setImageResource(setReminderStateImage(isReminderSet))
            setPrayerNotification(isReminderSet, dhur, Common.DHUR, Common.DHUR_INDEX)
        }

        btnSoundAsr.setOnClickListener {
            val isReminderSet = sharedPref.loadAsrState()
            ivSoundAsr.setImageResource(setReminderStateImage(isReminderSet))
            setPrayerNotification(isReminderSet, asr, Common.ASR, Common.ASR_INDEX)
        }

        btnSoundMaghrib.setOnClickListener {
            val isReminderSet = sharedPref.loadMaghribState()
            ivSoundMaghrib.setImageResource(setReminderStateImage(isReminderSet))
            setPrayerNotification(isReminderSet, maghrib, Common.MAGHRIB, Common.MAGHRIB_INDEX)
        }

        btnSoundIsha.setOnClickListener {
            val isReminderSet = sharedPref.loadIshaState()
            ivSoundIsha.setImageResource(setReminderStateImage(isReminderSet))
            setPrayerNotification(isReminderSet, isha, Common.ISHA, Common.ISHA_INDEX)
        }
    }

    private fun setPrayerNotification(
        reminderState: Boolean,
        prayerTime: String,
        prayerName: String,
        prayerIndex: Int
    ) {
        setPrayerState(reminderState, prayerName)
        if (!reminderState) {
            prayerTime.let { time ->
                prayerAlarm.setPrayerAlarm(this, time, prayerName, true, prayerIndex)
            }

        } else {
            prayerAlarm.cancelAlarm(this, prayerIndex)
        }
    }

    private fun setPrayerState(reminderState: Boolean, prayerName: String) {
        if (!reminderState) {
            when (prayerName) {
                Common.FAJR -> sharedPref.saveFajrState(true)
                Common.DHUR -> sharedPref.saveDhurState(true)
                Common.ASR -> sharedPref.saveAsrState(true)
                Common.MAGHRIB -> sharedPref.saveMaghribState(true)
                Common.ISHA -> sharedPref.saveIshaState(true)
            }
        } else {
            when (prayerName) {
                Common.FAJR -> sharedPref.saveFajrState(false)
                Common.DHUR -> sharedPref.saveDhurState(false)
                Common.ASR -> sharedPref.saveAsrState(false)
                Common.MAGHRIB -> sharedPref.saveMaghribState(false)
                Common.ISHA -> sharedPref.saveIshaState(false)
            }
        }
    }

    private fun setReminderStateImage(state: Boolean): Int {
        return if (state) R.drawable.ic_sound_off else R.drawable.ic_sound_on
    }

    private fun goToQibla() {
        startActivity<CompassActivity>()
    }

    private fun findCity(MyLat: Double, MyLong: Double) {
        val geocoder = Geocoder(this, Locale.getDefault())
        var addresses: List<Address>? = null
        try {
            addresses = geocoder.getFromLocation(MyLat, MyLong, 1)
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        val cityName = addresses!![0].locality
        val country = addresses[0].countryName
        val city = cityName.replace(' ', '-')
        txt_city.text = "$city,$country"
    }

    private fun getTimeZone(): Double {
        val tz = TimeZone.getDefault()
        val now = Date()
        val offsetFromUtc = tz.getOffset(now.time) / 3600000.0
        val m2tTimeZoneIs: String = offsetFromUtc.toString()
        Timber.d("new timezone test:$m2tTimeZoneIs")
        return offsetFromUtc
    }

    private fun setDate() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEE, MMM d, yyyy")
        val timeFormat = SimpleDateFormat("HH:mm")
        val time = timeFormat.format(calendar.time)
        val date = dateFormat.format(calendar.time)
        textTime.text = time
        textDate.text = date
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    private fun getUserLocation(fusedLocationWrapper: FusedLocationWrapper) {
        this.lifecycleScope.launch {
            val location = fusedLocationWrapper.awaitLastLocation()
            userPosition = LatLng(location.latitude, location.longitude)
            userPosition?.let {
                calculatePrayerTime(it)
                findCity(it.latitude, it.longitude)
                sharedPref.saveUserPosition(LatLng(it.latitude, it.longitude))
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    private fun permissionCheck(fusedLocationWrapper: FusedLocationWrapper) {
        if (PermissionUtils.isAccessFineLocationGranted(this)) {
            getUserLocation(fusedLocationWrapper)

        } else {
            Toast.makeText(this, getString(R.string.toast_permission), Toast.LENGTH_LONG).show()
        }
    }

    private fun calculatePrayerTime(position: LatLng) {
        date = SimpleDate(GregorianCalendar())
        Timber.d("date:${date.toString()}")
        val location = com.azan.astrologicalCalc.Location(
            position.latitude,
            position.longitude,
            timeZone!!,
            0
        )
        Timber.d("Location Library:${location.gmtDiff}")
        val azan = Azan(location, Method.EGYPT_SURVEY)
        val prayerTimes = azan.getPrayerTimes(date)
        tv_pray_time_fajr.text = prayerTimes.fajr().toString().dropLast(3)
        tv_pray_time_dhuhr.text = prayerTimes.thuhr().toString().dropLast(3)
        tv_pray_time_asr.text = prayerTimes.assr().toString().dropLast(3)
        tv_pray_time_maghrib.text = prayerTimes.maghrib().toString().dropLast(3)
        tv_pray_time_isha.text = prayerTimes.ishaa().toString().dropLast(3)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_prayer, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                goToSettings()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun goToSettings() {
        startActivity<SettingsActivity>()
    }

    private fun activateShowcase() {
        BubbleShowCaseSequence()
            .addShowCase(
                BubbleShowCaseBuilder(this) //Activity instance
                    .title(getString(R.string.bubble_prayer_notification_title)) //Any title for the bubble view
                    .targetView(btn_activate_notification) //View to point out
                    .description(getString(R.string.bubble_prayer_notification_all_description))
                    .backgroundColorResourceId(R.color.colorPrimary)
                    .imageResourceId(R.drawable.logo2)
                    .textColorResourceId(R.color.colorWhite)
            ) //First BubbleShowCase to show
            .addShowCase(
                BubbleShowCaseBuilder(this) //Activity instance
                    .title(getString(R.string.bubble_prayer_notification_title)) //Any title for the bubble view
                    .targetView(btnSoundFajr) //View to point out
                    .description(getString(R.string.bubble_prayer_notification_description))
                    .backgroundColorResourceId(R.color.colorPrimary)
                    .imageResourceId(R.drawable.logo2)
                    .textColorResourceId(R.color.colorWhite)
            )
            .show() //Display the ShowCaseSequence
    }


}
