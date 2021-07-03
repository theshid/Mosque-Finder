package com.shid.mosquefinder.Ui.Main.View

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.firestore.auth.User
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Ui.Base.SurahViewModelFactory
import com.shid.mosquefinder.Ui.Main.ViewModel.SurahViewModel
import com.shid.mosquefinder.Ui.broadcast_receiver.DbReceiver
import com.shid.mosquefinder.Utils.Common
import com.shid.mosquefinder.Utils.GsonParser
import com.shid.mosquefinder.Utils.SharePref
import timber.log.Timber

class LoadingActivity : AppCompatActivity() {
    companion object {
        val FILTER = "com.shid.mosquefinder.DBLOAD"
    }

    private lateinit var viewModel: SurahViewModel
    private var sharedPref:SharePref ?= null
    private var user:com.shid.mosquefinder.Data.Model.User ?= null
    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            goToHomeActivity()
            Timber.d("Test Receiver")
        }

    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = SharePref(this)
        user = GsonParser.gsonParser?.fromJson(sharedPref!!.loadUser(),com.shid.mosquefinder.Data.Model.User::class.java)
        val filter = IntentFilter(FILTER)
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,filter)
        setContentView(R.layout.activity_loading)
        setViewModel()
    }

    private fun setViewModel() {
        viewModel = ViewModelProvider(
            this,
            SurahViewModelFactory(application)
        ).get(SurahViewModel::class.java)

        viewModel.getSurahs()
        viewModel.surahList.observe(this, androidx.lifecycle.Observer {

        })

    }

    override fun onStart() {
        super.onStart()

        //registerReceiver(receiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }



    fun goToHomeActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra(Common.USER,user)
        startActivity(intent)
    }
}