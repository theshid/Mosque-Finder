package com.shid.mosquefinder.Ui.services.downloadUtil

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
import com.shid.mosquefinder.Ui.Main.View.AyahActivity
import com.shid.mosquefinder.Ui.Main.View.SurahActivity


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
            // 2
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText("Download in progress")
            .setProgress(progressMax,0,false)
            .setSound(null)
            .setOngoing(true)
            .setContentIntent(contentIntent)
            .setSmallIcon(R.drawable.logo2)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            // 3
            .setAutoCancel(true)
    }

    private val contentIntent by lazy {
        val intent = Intent(context,AyahActivity::class.java)
        intent.putExtra("surah_number",surahNumber)
        PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() =
        // 1
        NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {

            // 2
            description = CHANNEL_DESCRIPTION
            setSound(null, null)
        }

    fun getNotification(): Notification {
        // 1
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(createChannel())
        }

        // 2
        return notificationBuilder.build()
    }

    fun updateNotification(notificationText: String? = null) {
        // 1
        notificationText?.let { notificationBuilder.setContentText(it) }
        // 2
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }
}