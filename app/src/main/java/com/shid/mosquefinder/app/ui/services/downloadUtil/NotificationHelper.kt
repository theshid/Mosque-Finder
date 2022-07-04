package com.shid.mosquefinder.app.ui.services.downloadUtil

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.ui.main.views.AyahActivity


@RequiresApi(Build.VERSION_CODES.M)
class NotificationHelper(val context: Context) {


    companion object{
         var CHANNEL_DESCRIPTION = "download"
         var CHANNEL_ID = "mosque_dl"
         var CHANNEL_NAME = "download_surah"
         var NOTIFICATION_ID = 1234
    }

    private val progressMax = 100
    var surahNumber = 1


    private val notificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

     val notificationBuilder: NotificationCompat.Builder by lazy {
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText("Download in progress")
            .setProgress(progressMax,0,false)
            .setSound(null)
            .setOngoing(true)
            .setContentIntent(contentIntent)
            .setSmallIcon(R.drawable.logo2)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
    }

    private val contentIntent by lazy {
        val intent = Intent(context,AyahActivity::class.java)
        intent.putExtra("surah_number",surahNumber)
        PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() =
        NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = CHANNEL_DESCRIPTION
            setSound(null, null)
        }

    fun getNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(createChannel())
        }
        return notificationBuilder.build()
    }

    fun updateNotification(notificationText: String? = null) {
        notificationText?.let { notificationBuilder.setContentText(it) }
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }
}