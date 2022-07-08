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
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.azan.Azan
import com.azan.Method
import com.azan.astrologicalCalc.SimpleDate
import com.elconfidencial.bubbleshowcase.BubbleShowCaseBuilder
import com.elconfidencial.bubbleshowcase.BubbleShowCaseSequence
import com.google.android.gms.maps.model.LatLng
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.ui.services.PrayerAlarmBroadcastReceiver
import com.shid.mosquefinder.app.utils.*
import com.shid.mosquefinder.app.utils.helper_class.singleton.Common
import com.shid.mosquefinder.app.utils.helper_class.singleton.PermissionUtils
import kotlinx.android.synthetic.main.activity_prayer.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class PrayerActivity : AppCompatActivity() {

    private var userPosition: LatLng? = null
    private lateinit var date: SimpleDate
    private var timeZone: Double? = null
    private var isFirstTime: Boolean? = null
    private lateinit var sharedPref: SharePref
    private var _broadcastReceiver: BroadcastReceiver? = null
    private val _sdfWatchTime = SimpleDateFormat("HH:mm")


    val prayerAlarm = PrayerAlarmBroadcastReceiver()

    @OptIn(ExperimentalCoroutinesApi::class)
    private lateinit var fusedLocationWrapper: FusedLocationWrapper


    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prayer)
        sharedPref = SharePref(this)
        isFirstTime = sharedPref.loadFirstTimePrayerNotification()
        fusedLocationWrapper = fusedLocationWrapper()
        permissionCheck(fusedLocationWrapper)
        setUI()
        clickListeners()
        if (isFirstTime == true) {
            activateShowcase()
            sharedPref.setFirstTimePrayerNotification(false)
        }
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
        if (_broadcastReceiver!= null){
            unregisterReceiver(_broadcastReceiver)
        }
    }

    private fun setUI() {
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

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun clickListeners() {

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
            if (sharedPref.loadIsAllPrayersNotificationActivated()) {
                sharedPref.saveFajrState(false)
                sharedPref.saveDhurState(false)
                sharedPref.saveAsrState(false)
                sharedPref.saveMaghribState(false)
                sharedPref.saveIshaState(false)
                sharedPref.setAllPrayerNotifications(false)

                ivSoundFajr.setImageResource(R.drawable.ic_sound_off)
                ivSoundDhur.setImageResource(R.drawable.ic_sound_off)
                ivSoundAsr.setImageResource(R.drawable.ic_sound_off)
                ivSoundMaghrib.setImageResource(R.drawable.ic_sound_off)
                ivSoundIsha.setImageResource(R.drawable.ic_sound_off)

                btn_activate_notification.text = getString(R.string.activate_all_notification)
            } else {
                sharedPref.saveFajrState(true)
                sharedPref.saveDhurState(true)
                sharedPref.saveAsrState(true)
                sharedPref.saveMaghribState(true)
                sharedPref.saveIshaState(true)
                sharedPref.setAllPrayerNotifications(true)

                ivSoundFajr.setImageResource(R.drawable.ic_sound_on)
                ivSoundDhur.setImageResource(R.drawable.ic_sound_on)
                ivSoundAsr.setImageResource(R.drawable.ic_sound_on)
                ivSoundMaghrib.setImageResource(R.drawable.ic_sound_on)
                ivSoundIsha.setImageResource(R.drawable.ic_sound_on)

                btn_activate_notification.text = getString(R.string.disactivate_all_notification)
            }

        }

        btnSoundFajr.setOnClickListener {
            val fajr: String?
            fajr = sharedPref.loadFajr()
            val isReminderSet = !sharedPref.loadFajrState()

            ivSoundFajr.setImageResource(if (isReminderSet) R.drawable.ic_sound_on else R.drawable.ic_sound_off)

            if (isReminderSet) {
                sharedPref.saveFajrState(isReminderSet)
                fajr.let { it1 ->
                    prayerAlarm.setPrayerAlarm(
                        this,
                        it1,
                        Common.FAJR,
                        isReminderSet,
                        Common.FAJR_INDEX
                    )
                }
            } else {
                prayerAlarm.cancelAlarm(this, Common.FAJR_INDEX)
                sharedPref.saveFajrState(isReminderSet)

            }

        }

        btnSoundDhur.setOnClickListener {
            val dhur: String?
            dhur = sharedPref.loadDhur()
            val isReminderSet = !sharedPref.loadDhurState()

            ivSoundDhur.setImageResource(if (isReminderSet) R.drawable.ic_sound_on else R.drawable.ic_sound_off)

            if (isReminderSet) {
                Timber.d("value of Dhur:$dhur")
                sharedPref.saveDhurState(isReminderSet)
                dhur.let { it1 ->
                    prayerAlarm.setPrayerAlarm(
                        this,
                        it1,
                        Common.DHUR,
                        isReminderSet,
                        Common.DHUR_INDEX
                    )
                }
            } else {
                prayerAlarm.cancelAlarm(this, Common.DHUR_INDEX)
                sharedPref.saveDhurState(isReminderSet)

            }

        }

        btnSoundAsr.setOnClickListener {
            val asr: String?
            asr = sharedPref.loadAsr()
            val isReminderSet = !sharedPref.loadAsrState()

            ivSoundAsr.setImageResource(if (isReminderSet) R.drawable.ic_sound_on else R.drawable.ic_sound_off)

            if (isReminderSet) {
                sharedPref.saveAsrState(isReminderSet)
                asr.let { it1 ->
                    prayerAlarm.setPrayerAlarm(
                        this,
                        it1,
                        Common.ASR,
                        isReminderSet,
                        Common.ASR_INDEX
                    )
                }
            } else {
                prayerAlarm.cancelAlarm(this, Common.ASR_INDEX)
                sharedPref.saveAsrState(isReminderSet)

            }

        }

        btnSoundMaghrib.setOnClickListener {
            val maghrib: String?
            maghrib = sharedPref.loadMaghrib()
            val isReminderSet = !sharedPref.loadMaghribState()

            ivSoundMaghrib.setImageResource(if (isReminderSet) R.drawable.ic_sound_on else R.drawable.ic_sound_off)

            if (isReminderSet) {
                sharedPref.saveMaghribState(isReminderSet)
                maghrib.let { it1 ->
                    prayerAlarm.setPrayerAlarm(
                        this,
                        it1,
                        Common.MAGHRIB,
                        isReminderSet,
                        Common.MAGHRIB_INDEX
                    )
                }
            } else {
                prayerAlarm.cancelAlarm(this, Common.MAGHRIB_INDEX)
                sharedPref.saveMaghribState(isReminderSet)

            }

        }

        btnSoundIsha.setOnClickListener {
            val isha: String?
            isha = sharedPref.loadIsha()
            val isReminderSet = !sharedPref.loadIshaState()

            ivSoundIsha.setImageResource(if (isReminderSet) R.drawable.ic_sound_on else R.drawable.ic_sound_off)

            if (isReminderSet) {
                sharedPref.saveIshaState(isReminderSet)
                isha.let { it1 ->
                    prayerAlarm.setPrayerAlarm(
                        this,
                        it1,
                        Common.ISHA,
                        isReminderSet,
                        Common.ISHA_INDEX
                    )
                }
            } else {
                prayerAlarm.cancelAlarm(this, Common.ISHA_INDEX)
                sharedPref.saveIshaState(isReminderSet)

            }

        }


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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
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
