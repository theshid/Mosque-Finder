package com.shid.mosquefinder.app.ui.main.views

import android.annotation.SuppressLint
import android.content.*
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.azan.Azan
import com.azan.Method
import com.azan.astrologicalCalc.SimpleDate
import com.elconfidencial.bubbleshowcase.BubbleShowCaseBuilder
import com.elconfidencial.bubbleshowcase.BubbleShowCaseSequence
import com.google.android.gms.maps.model.LatLng
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.ui.base.BaseActivity
import com.shid.mosquefinder.app.ui.main.view_models.PrayerViewModel
import com.shid.mosquefinder.app.utils.extensions.startActivity
import com.shid.mosquefinder.app.utils.helper_class.Constants.DATE_PATTERN
import com.shid.mosquefinder.app.utils.helper_class.Constants.HOUR_PATTERN
import com.shid.mosquefinder.app.utils.helper_class.FusedLocationWrapper
import com.shid.mosquefinder.app.utils.helper_class.SharePref
import com.shid.mosquefinder.app.utils.helper_class.SharedPreferenceBooleanLiveData
import com.shid.mosquefinder.app.utils.helper_class.singleton.Common
import com.shid.mosquefinder.app.utils.helper_class.singleton.PermissionUtils
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
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
    private lateinit var fajrLiveState: SharedPreferenceBooleanLiveData
    private lateinit var dhurLiveState: SharedPreferenceBooleanLiveData
    private lateinit var asrLiveState: SharedPreferenceBooleanLiveData
    private lateinit var maghribLiveState: SharedPreferenceBooleanLiveData
    private lateinit var ishaLiveState: SharedPreferenceBooleanLiveData

    private var fajrC: Boolean? = null
    private var dhurC: Boolean? = null
    private var asrC: Boolean? = null
    private var maghribC: Boolean? = null
    private var ishaC: Boolean? = null

    @Inject
    lateinit var sharedPref: SharePref

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    @ApplicationContext
    lateinit var aContext: Context

    private val viewModel: PrayerViewModel by viewModels()

    private var _broadcastReceiver: BroadcastReceiver? = null
    private val _sdfWatchTime = SimpleDateFormat(HOUR_PATTERN)


    @Inject
    @OptIn(ExperimentalCoroutinesApi::class)
    lateinit var fusedLocationWrapper: FusedLocationWrapper


    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prayer)
        isFirstTime = sharedPref.loadFirstTimePrayerNotification()
        userPosition = sharedPref.loadSavedPosition()
        permissionCheck(fusedLocationWrapper)
        initLiveDataPreferences()
        setUI()
        clickListeners()
        initObservers()

    }

    private fun initObservers() {

        fajrLiveState.observe(this) {
            fajrC = it
            setEnableAllTextButton()
            Timber.d("faje:$it")
        }

        dhurLiveState.observe(this) {
            dhurC = it
            setEnableAllTextButton()
        }

        asrLiveState.observe(this) {
            asrC = it
            setEnableAllTextButton()
        }

        maghribLiveState.observe(this) {
            maghribC = it
            setEnableAllTextButton()
        }

        ishaLiveState.observe(this) {
            ishaC = it
            setEnableAllTextButton()
        }
    }

    private fun setEnableAllTextButton() {
        if (fajrC == true && dhurC == true && asrC == true &&
            maghribC == true && ishaC == true
        ) {
            btn_activate_notification.text = getString(R.string.disactivate_all_notification)
            sharedPref.setAllPrayerNotifications(true)
        } else {
            btn_activate_notification.text = getString(R.string.activate_all_notification)
            sharedPref.setAllPrayerNotifications(false)
        }
    }

    private fun initLiveDataPreferences() {
        val fajrIsReminderSet = sharedPref.loadFajrState()
        val dhurIsReminderSet = sharedPref.loadDhurState()
        val asrIsReminderSet = sharedPref.loadAsrState()
        val maghribIsReminderSet = sharedPref.loadMaghribState()
        val ishaIsReminderSet = sharedPref.loadIshaState()

        fajrLiveState =
            SharedPreferenceBooleanLiveData(sharedPreferences, Common.FAJR_STATE, fajrIsReminderSet)
        dhurLiveState =
            SharedPreferenceBooleanLiveData(sharedPreferences, Common.DHUR_STATE, dhurIsReminderSet)
        asrLiveState =
            SharedPreferenceBooleanLiveData(sharedPreferences, Common.ASR_STATE, asrIsReminderSet)
        maghribLiveState = SharedPreferenceBooleanLiveData(
            sharedPreferences,
            Common.MAGHRIB_STATE,
            maghribIsReminderSet
        )
        ishaLiveState =
            SharedPreferenceBooleanLiveData(sharedPreferences, Common.ISHA_STATE, ishaIsReminderSet)
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

            setPrayerNotification(allNotificationState, fajr, Common.FAJR)
            setPrayerNotification(allNotificationState, dhur, Common.DHUR)
            setPrayerNotification(allNotificationState, asr, Common.ASR)
            setPrayerNotification(
                allNotificationState,
                maghrib,
                Common.MAGHRIB
            )
            setPrayerNotification(allNotificationState, isha, Common.ISHA)

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
            setPrayerNotification(isReminderSet, fajr, Common.FAJR)
        }

        btnSoundDhur.setOnClickListener {
            val isReminderSet = sharedPref.loadDhurState()
            ivSoundDhur.setImageResource(setReminderStateImage(isReminderSet))
            setPrayerNotification(isReminderSet, dhur, Common.DHUR)
        }

        btnSoundAsr.setOnClickListener {
            val isReminderSet = sharedPref.loadAsrState()
            ivSoundAsr.setImageResource(setReminderStateImage(isReminderSet))
            setPrayerNotification(isReminderSet, asr, Common.ASR)
        }

        btnSoundMaghrib.setOnClickListener {
            val isReminderSet = sharedPref.loadMaghribState()
            ivSoundMaghrib.setImageResource(setReminderStateImage(isReminderSet))
            setPrayerNotification(isReminderSet, maghrib, Common.MAGHRIB)
        }

        btnSoundIsha.setOnClickListener {
            val isReminderSet = sharedPref.loadIshaState()
            ivSoundIsha.setImageResource(setReminderStateImage(isReminderSet))
            setPrayerNotification(isReminderSet, isha, Common.ISHA)
        }
    }

    private fun setPrayerNotification(
        reminderState: Boolean,
        prayerTime: String,
        prayerName: String
    ) {
        setPrayerState(reminderState, prayerName)
        if (!reminderState) {

            when (prayerName) {
                Common.FAJR -> sharedPref.saveFajrId(
                    viewModel.setPrayerAlarm(
                        aContext,
                        prayerTime,
                        prayerName,
                        true
                    )
                )
                Common.DHUR -> sharedPref.saveDhurId(
                    viewModel.setPrayerAlarm(
                        aContext,
                        prayerTime,
                        prayerName,
                        true
                    )
                )
                Common.ASR -> sharedPref.saveAsrId(
                    viewModel.setPrayerAlarm(
                        aContext,
                        prayerTime,
                        prayerName,
                        true
                    )
                )
                Common.MAGHRIB -> sharedPref.saveMaghribId(
                    viewModel.setPrayerAlarm(
                        aContext,
                        prayerTime,
                        prayerName,
                        true
                    )
                )
                Common.ISHA -> sharedPref.saveIshaId(
                    viewModel.setPrayerAlarm(
                        aContext,
                        prayerTime,
                        prayerName,
                        true
                    )
                )
            }

        } else {
            cancelPrayerNotification(prayerName)
        }
    }

    private fun cancelPrayerNotification(prayerName: String) {
        val requestId: Int
        when (prayerName) {
            Common.FAJR -> {
                requestId = sharedPref.loadFajrId()
                viewModel.cancelPrayerNotification(requestId, aContext, prayerName)
            }
            Common.DHUR -> {
                requestId = sharedPref.loadDhurId()
                viewModel.cancelPrayerNotification(requestId, aContext, prayerName)
            }
            Common.ASR -> {
                requestId = sharedPref.loadAsrId()
                viewModel.cancelPrayerNotification(requestId, aContext, prayerName)
            }
            Common.MAGHRIB -> {
                requestId = sharedPref.loadMaghribId()
                viewModel.cancelPrayerNotification(requestId, aContext, prayerName)
            }
            Common.ISHA -> {
                requestId = sharedPref.loadIshaId()
                viewModel.cancelPrayerNotification(requestId, aContext, prayerName)
            }
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
            Timber.d("latitude:$MyLat")
            Timber.d("latitude:$MyLong")
            addresses = geocoder.getFromLocation(MyLat, MyLong, 1)
        } catch (ex: IOException) {
            ex.printStackTrace()
            Timber.e("exception:${ex.message}")
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
        val dateFormat = SimpleDateFormat(DATE_PATTERN)
        val timeFormat = SimpleDateFormat(HOUR_PATTERN)
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
