package com.shid.mosquefinder.app.ui.broadcast_receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.shid.mosquefinder.app.ui.main.views.LoadingActivity
import timber.log.Timber

class DbReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        if (intent.action.equals(LoadingActivity.FILTER)){
            Timber.d("BR Test")
        }


    }
}