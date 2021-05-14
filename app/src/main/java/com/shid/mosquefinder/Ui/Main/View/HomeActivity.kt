package com.shid.mosquefinder.Ui.Main.View

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.shid.mosquefinder.Data.Model.User
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Utils.Common
import com.shid.mosquefinder.Utils.setTransparentStatusBar
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {
    private var user: User? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        user = getUserFromIntent()
        setClickListeners()

        setTransparentStatusBar()
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