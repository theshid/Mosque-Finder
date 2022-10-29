package com.shid.mosquefinder.app.ui.broadcast_receiver


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import com.shid.mosquefinder.R
import dagger.hilt.android.AndroidEntryPoint
import de.coldtea.smplr.smplralarm.apis.SmplrAlarmAPI
import timber.log.Timber

@AndroidEntryPoint
class PrayerAlarmBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        intent.extras?.getInt(SmplrAlarmAPI.SMPLR_ALARM_REQUEST_ID)?.let {
            Timber.d("Inside intent extras 2")
            handleOnReceive(context)
        }
    }

    private fun handleOnReceive(context: Context) {
        val mediaPlayer = MediaPlayer.create(
            context, R.raw.islamic_hd_ringtone
        )
        mediaPlayer.apply {
            isLooping = false
            start()
        }
    }

}