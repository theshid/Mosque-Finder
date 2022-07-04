package com.shid.mosquefinder.app.ui.services

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.Timestamp
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.ui.main.views.HomeActivity
import com.shid.mosquefinder.app.utils.Common
import com.shid.mosquefinder.app.utils.SharePref
import com.shid.mosquefinder.app.utils.TimeUtil.hour
import com.shid.mosquefinder.app.utils.showToast
import timber.log.Timber
import java.util.*

class PrayerAlarmBroadcastReceiver : BroadcastReceiver() {
    val String.hour get() : Int = if (this != "-") this.split(":", " ").first().toInt() else 0
    val String.minutes get() : Int = if (this != "-") this.split(":", " ")[1].toInt() else 0

    companion object {
        const val EXTRA_ALARM = "extra_alarm"
        const val NOTIFICATION_TITLE = "Prayer Reminder"
        const val NOTIFICATION_REQUEST_CODE = 102
        const val CHANNEL_ID = "Reminder"
        const val CHANNEL_NAME = "Daily Reminder"
        private fun getScheduleName(index: Int) = when (index) {
            0 -> Common.FAJR
            1 -> Common.DHUR
            2 -> Common.ASR
            3 -> Common.MAGHRIB
            4 -> Common.ISHA
            else -> "-"
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val sharePref = SharePref(context)
        intent.extras?.getInt(EXTRA_ALARM)?.let {
            Timber.d("Inside intent extras")
           when(it){
               Common.FAJR_INDEX -> handleOnReceive(sharePref.loadFajr(),it,context)
               Common.DHUR_INDEX -> handleOnReceive(sharePref.loadDhur(),it,context)
               Common.ASR_INDEX -> handleOnReceive(sharePref.loadAsr(),it,context)
               Common.MAGHRIB_INDEX -> handleOnReceive(sharePref.loadMaghrib(),it,context)
               Common.ISHA_INDEX -> handleOnReceive(sharePref.loadIsha(),it,context)
           }
        }
    }

    private fun handleOnReceive(prayerTime: String, prayerIndex: Int, context: Context) {
        val reminderHour = prayerTime.split(":").first().toInt()
        if (reminderHour >= Timestamp.now().hour) {
            showAlarmNotification(context, prayerIndex, NOTIFICATION_TITLE, buildString {
                append("Now it's time for ")
                append(getScheduleName(prayerIndex))
                append(" pray at ")
                append(prayerTime)
            })

            val mediaPlayer = MediaPlayer.create(
                context, R.raw.adzan_makkah
            )
            mediaPlayer.apply {
                isLooping = false
                start()
            }

        }
    }

    fun setPrayerAlarm(
        context: Context,
        prayerType: String,
        prayer: String,
        showToast: Boolean? = true,
        index: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, PrayerAlarmBroadcastReceiver::class.java)
        intent.putExtra(EXTRA_ALARM, index)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, prayerType.hour)
            set(Calendar.MINUTE, prayerType.minutes)
            set(Calendar.SECOND, 0)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, index,
            intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
        if (showToast == true) context.showToast("Reminder for $prayer at $prayerType is set")
    }

    fun cancelAlarm(context: Context, id: Int, message: String? = "") {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, PrayerAlarmBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent.cancel()
        alarmManager.cancel(pendingIntent)
        message?.let {
            if (it.isNotBlank()) {
                val text = "Reminder for ${getScheduleName(id)} pray is unset"
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAlarmNotification(
        context: Context,
        id: Int,
        title: String,
        content: String
    ) {

        val notificationManagerCompat =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        val pendingIntent = PendingIntent.getActivity(
            context, NOTIFICATION_REQUEST_CODE, intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE
            else PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo2)
            .setContentTitle(title)
            .setContentText(content)
            .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableVibration(true)
                vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)
            }

            builder.setChannelId(CHANNEL_ID)
            notificationManagerCompat.createNotificationChannel(channel)
        }

        val notification = builder.build()
        notificationManagerCompat.notify(id, notification)
    }
}