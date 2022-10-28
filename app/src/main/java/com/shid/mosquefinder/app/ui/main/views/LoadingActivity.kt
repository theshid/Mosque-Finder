package com.shid.mosquefinder.app.ui.main.views

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.ui.main.view_models.SurahViewModel
import com.shid.mosquefinder.app.utils.extensions.startActivity
import com.shid.mosquefinder.app.utils.helper_class.SharePref
import com.shid.mosquefinder.app.utils.helper_class.singleton.Common
import com.shid.mosquefinder.app.utils.helper_class.singleton.GsonParser
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class LoadingActivity : AppCompatActivity() {
    companion object {
        const val FILTER = "com.shid.mosquefinder.DBLOAD"
    }

    private val viewModel: SurahViewModel by viewModels()

    @Inject
    lateinit var sharedPref: SharePref

    private lateinit var intentFilter: IntentFilter

    private var user: com.shid.mosquefinder.data.model.User? = null
    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            goToHomeActivity()
            Timber.d("Test Receiver")
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = GsonParser.gsonParser?.fromJson(
            sharedPref.loadUser(),
            com.shid.mosquefinder.data.model.User::class.java
        )
        intentFilter = IntentFilter(FILTER)
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter)
        setContentView(R.layout.activity_loading)
        setViewModel()
    }

    private fun setViewModel() {
        viewModel.getSurahs()
    }

    override fun onStart() {
        super.onStart()

        registerReceiver(receiver, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }


    fun goToHomeActivity() {
        startActivity<HomeActivity> {
            putExtra(Common.USER, user)
        }
        finish()
    }
}