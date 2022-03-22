package com.shid.mosquefinder.Ui.Main.View

import android.annotation.SuppressLint
import android.content.Intent
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
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Utils.*
import kotlinx.android.synthetic.main.activity_prayer.*
import kotlinx.android.synthetic.main.activity_prayer.toolbar
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
    private lateinit var sharedPref: SharePref
    @OptIn(ExperimentalCoroutinesApi::class)
    private lateinit var fusedLocationWrapper: FusedLocationWrapper


    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prayer)
        fusedLocationWrapper = fusedLocationWrapper()
        permissionCheck(fusedLocationWrapper)
        setUI()
        clickListeners()
    }


    private fun setUI() {
        sharedPref = SharePref(this)
        if (userPosition == null){
            userPosition = sharedPref.loadSavedPosition()
        }
        Timber.d("userPosition:$userPosition")
        timeZone = getTimeZone()
        Timber.d("timeZone:${timeZone.toString()}")
        calculatePrayerTime(userPosition!!)
        findCity(userPosition!!.latitude, userPosition!!.longitude)
        setDate()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun clickListeners() {
        btn_location.setOnClickListener {
            permissionCheck(fusedLocationWrapper)
        }

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        menu_settings.setOnClickListener {
            goToSettings()
        }

        btn_qibla.setOnClickListener {
            goToQibla()
        }
    }

    private fun goToQibla() {
        val intent = Intent(this, CompassActivity::class.java)
        startActivity(intent)
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
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }


}
