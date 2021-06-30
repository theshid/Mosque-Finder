package com.shid.mosquefinder.Ui.Main.View

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Ui.Base.SurahViewModelFactory
import com.shid.mosquefinder.Ui.Main.ViewModel.SurahViewModel
import com.shid.mosquefinder.Ui.broadcast_receiver.DbReceiver
import timber.log.Timber

class LoadingActivity : AppCompatActivity() {
    companion object {
        val FILTER = "com.shid.mosquefinder.DBLOAD"
    }

    private lateinit var viewModel: SurahViewModel
    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if(FILTER.equals(intent?.action)){
                Timber.d("Test Receiver")
            }

        }

    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        val filter = IntentFilter(FILTER)
        registerReceiver(receiver, filter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(receiver)
    }

    fun goToHomeActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }
}