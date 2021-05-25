package com.shid.mosquefinder.Ui.Main.View

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.azan.Azan
import com.azan.Method
import com.azan.astrologicalCalc.Location
import com.azan.astrologicalCalc.SimpleDate
import com.google.android.gms.maps.model.LatLng
import com.shid.mosquefinder.Data.Model.User
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Utils.Common
import com.shid.mosquefinder.Utils.setTransparentStatusBar
import fr.quentinklein.slt.LocationTracker
import fr.quentinklein.slt.ProviderError
import kotlinx.android.synthetic.main.activity_home.*
import java.util.*

class HomeActivity : AppCompatActivity() {
    private var user: User? = null
    private var locationTracker: LocationTracker? = null
    var userPosition: LatLng? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setUpNewLocationListener()
        user = getUserFromIntent()
        setClickListeners()

        setTransparentStatusBar()
        setUI()
    }

    private fun setUI(){

        val today = SimpleDate(GregorianCalendar())
        val locationPrayer = Location(5.6361298,-0.2344183, 0.0,0)
        val azan = Azan(locationPrayer, Method.MUSLIM_LEAGUE)
        val prayerTimes = azan.getPrayerTimes(today)
        tv_home_salat_time.text = prayerTimes.fajr().toString()

    }

    override fun onPause() {
        super.onPause()

        locationTracker?.stopListening()

    }

    override fun onResume() {
        super.onResume()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        locationTracker?.startListening(this)
    }

    private fun setUpNewLocationListener() {
        locationTracker?.addListener(object : LocationTracker.Listener {

            override fun onLocationFound(location: android.location.Location) {
                userPosition = LatLng(location.latitude, location.longitude)


                Log.d("Home", "new position:$userPosition")
                Log.d("Home", "accuracy" + location.accuracy)
            }

            override fun onProviderError(providerError: ProviderError) {
            }

        });
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