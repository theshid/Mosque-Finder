package com.shid.mosquefinder.Ui.Main.View

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.azan.Azan
import com.azan.Method
import com.azan.astrologicalCalc.SimpleDate
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Utils.PermissionUtils
import kotlinx.android.synthetic.main.activity_prayer.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class PrayerActivity : AppCompatActivity() {

    private var userPosition: LatLng? = null
    private lateinit var date: SimpleDate
    private var timeZone: Double? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest:LocationRequest


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prayer)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest()

        permissionCheck()
        setDate()
        //setCity()
        timeZone = getTimeZone()

        button.setOnClickListener(View.OnClickListener {
            goToMapActivity()
        })
    }

    override fun onResume() {
        super.onResume()
        if (PermissionUtils.isAccessFineLocationGranted(this)){
            startLocationUpdates()
        }


    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }


    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }


    private fun setCity() {
        val calendar: Calendar = Calendar.getInstance(TimeZone.getDefault())
        val zone: TimeZone = calendar.timeZone
        val zoneId = zone.id
        txt_city.text = zoneId
    }

    private fun findCity(MyLat: Double, MyLong: Double) {
        //double MyLat = 33.97159194946289;
        //double MyLong = -6.849812984466553;
        val geocoder = Geocoder(this, Locale.getDefault())
        var addresses: List<Address>? = null
        try {
            addresses = geocoder.getFromLocation(MyLat, MyLong, 1)
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        val cityName = addresses!![0].locality
        val country = addresses[0].countryName

        val cityy = cityName.replace(' ', '-')
        txt_city.text = "$cityy,$country"
        /*val preff = getSharedPreferences("lastprayertimes", MODE_PRIVATE)
        val editor = preff.edit()
        editor.putString("country", country)
        editor.putString("city", cityy)
        editor.apply()*/

    }

    private fun goToMapActivity(){
        val intent = Intent(this,MapsActivity2::class.java)
        startActivity(intent)
    }

    private fun getTimeZone(): Double {
        val cal: Calendar = Calendar.getInstance(Locale.getDefault())
        val offset = -(cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)) / (60 * 1000)
        return offset.toDouble()
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

    @SuppressLint("MissingPermission")
    private fun permissionCheck() {
        if (PermissionUtils.isAccessFineLocationGranted(this)) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    userPosition =
                        location?.longitude?.let {
                            LatLng(
                                location.latitude,
                                it
                            )
                        } // Got last known location. In some rare situations this can be null.
                    userPosition?.let { calculatePrayerTime(it)
                    findCity(it.latitude,it.longitude)}
                }

           if (userPosition == null){
               retrieveLocation()
           }
        }else {
            Toast.makeText(this, getString(R.string.toast_permission), Toast.LENGTH_LONG).show()
        }
    }

    private fun retrieveLocation(){
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    userPosition = LatLng(location.latitude,location.longitude)
                    calculatePrayerTime(userPosition!!)
                    findCity(userPosition!!.latitude,userPosition!!.longitude)
                    // Update UI with location data
                    // ...
                }
            }


        }
    }

        private fun calculatePrayerTime(position: LatLng) {
            date = SimpleDate(GregorianCalendar())
            val location = com.azan.astrologicalCalc.Location(
                position.latitude,
                position.longitude,
                timeZone!!,
                0
            )
            val azan = Azan(location, Method.EGYPT_SURVEY)
            val prayerTimes = azan.getPrayerTimes(date)
            tv_pray_time_fajr.text = prayerTimes.fajr().toString()
            tv_pray_time_dhuhr.text = prayerTimes.thuhr().toString()
            tv_pray_time_asr.text = prayerTimes.assr().toString()
            tv_pray_time_maghrib.text = prayerTimes.maghrib().toString()
            tv_pray_time_isha.text = prayerTimes.ishaa().toString()

        }




    }
