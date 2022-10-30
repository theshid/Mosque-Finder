package com.shid.mosquefinder.app.factory

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.Resources
import androidx.core.app.NotificationCompat
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import com.shid.mosquefinder.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ForegroundInfoFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationManager: NotificationManager,
    private val workManager: WorkManager,
    private val resources: Resources
) {

    fun create(workId: UUID, num: Long): ForegroundInfo {
        val channelId = createChannel()
        val title = resources.getString(R.string.notification_work_title)
        val cancel = resources.getString(R.string.notification_work_cancel_label)
        val cancelIntent = workManager.createCancelPendingIntent(workId)

        val notification = NotificationCompat.Builder(context, channelId).apply {
            setContentTitle(title)
            setTicker(title)
            setContentText(
                resources.getQuantityString(R.plurals.notification_work_status, num.toInt(), num)
            )
            setOngoing(true)
            setProgress(0, 0, true)
            setSmallIcon(R.drawable.logo2)
            addAction(R.drawable.ic_cancel, cancel, cancelIntent)
        }.build()

        return ForegroundInfo(resources.getInteger(R.integer.notification_work_id), notification)

    }


    private fun createChannel(): String {
        val channelId = resources.getString(R.string.notification_channel_work_id)
        val channel = notificationManager.notificationChannels.firstOrNull { it.id == channelId }
        if (channel == null) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    resources.getString(R.string.notification_channel_work_label),
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
        return channelId
    }


}