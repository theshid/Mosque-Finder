package com.shid.mosquefinder.Ui.services

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Utils.SharePref


class MyFirebaseMessagingService: FirebaseMessagingService()  {

    private var broadcaster: LocalBroadcastManager? = null

    override fun onCreate() {
        broadcaster = LocalBroadcastManager.getInstance(this)
    }


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        handleMessage(remoteMessage)
    }

    private fun handleMessage(remoteMessage: RemoteMessage) {
        //1
        val handler = Handler(Looper.getMainLooper())

        //2
        handler.post(Runnable {
            remoteMessage.notification?.let {
                val intent = Intent("MyData")
                intent.putExtra("message", remoteMessage.data["text"]);
                broadcaster?.sendBroadcast(intent);
            }


            /*Toast.makeText(baseContext, getString(R.string.handle_notification_now),
                Toast.LENGTH_LONG).show()*/
        }
        )
    }


    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val sharePref = SharePref(applicationContext)
        sharePref.saveFirebaseToken(token)
    }
}