package com.shid.mosquefinder.app.ui.main.factories

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import com.shid.mosquefinder.R
import com.shid.mosquefinder.domain.model.Ayah
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyAyahInfoFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationManager: NotificationManager,
    private val workManager: WorkManager,
    private val resources: Resources
) {

    fun create(workId: UUID, ayah: Ayah): ForegroundInfo {
        Timber.d("foreground")
        val channelId = createChannel()
        val title = "Daily Ayah"
        val alarmSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val largeIcon = BitmapFactory.decodeResource(resources, R.drawable.logo2)
        val contentText = ayah.translation

        val notification = NotificationCompat.Builder(context, channelId).apply {
            setSmallIcon(R.drawable.logo2)
            setLargeIcon(largeIcon)
            setContentTitle(mContext.getString(R.string.notification_title))
            setContentText(ayah.surah_number.toString() + ":" + ayah.verse_number + " " + contentText)
            setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(ayah.surah_number.toString() + ":" + ayah.verse_number + " " + contentText)
            )
            priority = NotificationCompat.PRIORITY_DEFAULT
            color = mContext.resources.getColor(R.color.whiteTransparent)
            setCategory(NotificationCompat.CATEGORY_REMINDER)
            setSound(alarmSound)
            setAutoCancel(true)
        }.build()
        return ForegroundInfo(1001, notification)
    }

    private fun createChannel(): String {
        val channelId = "daily-ayah"
        val channel = notificationManager.notificationChannels.firstOrNull { it.id == channelId }
        if (channel == null) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    "Daily Ayah",
                    NotificationManager.IMPORTANCE_LOW
                )
            )

        }
        return channelId
    }
}