package com.shid.mosquefinder.app.factory

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.ui.broadcast_receiver.AyahReceiver
import com.shid.mosquefinder.app.utils.helper_class.Constants.ACTION_RETRY
import com.shid.mosquefinder.app.utils.helper_class.Constants.notificationId
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackgroundInfoFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationManager: NotificationManager,
    private val resources: Resources
) {
    fun createNotificationForRetry() {
        val channelId = createChannel()
        val title = resources.getString(R.string.notification_work_retry_title)
        val restart = resources.getString(R.string.notification_work_retry_label)
        val retryIntent = Intent(context, AyahReceiver::class.java).apply {
            action = ACTION_RETRY
            putExtra(NotificationCompat.EXTRA_NOTIFICATION_ID, 0)
        }
        val retryPendingIntent: PendingIntent =
            PendingIntent.getBroadcast(
                context,
                0,
                retryIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        val notification = NotificationCompat.Builder(context, channelId).apply {
            setContentTitle(title)
            setTicker(title)
            setContentText(
                resources.getString(R.string.list_error)
            )
            setSmallIcon(R.drawable.logo2)
            addAction(R.drawable.ic_refresh, restart, retryPendingIntent)
        }.build()

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, notification)
        }

    }

    private fun createChannel(): String {
        val channelId = resources.getString(R.string.notification_channel_work_retry_id)
        val channel = notificationManager.notificationChannels.firstOrNull { it.id == channelId }
        if (channel == null) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    resources.getString(R.string.notification_channel_work_retry_label),
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
        return channelId
    }
}