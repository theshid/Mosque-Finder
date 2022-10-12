package com.shid.mosquefinder.app.ui.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.shid.mosquefinder.R
import com.shid.mosquefinder.data.api.QuranApiInterface
import com.shid.mosquefinder.data.local.database.QuranDao
import com.shid.mosquefinder.data.local.database.QuranDatabase
import com.shid.mosquefinder.data.local.database.entities.AyahDb
import com.shid.mosquefinder.data.repository.AyahRepositoryImpl
import com.shid.mosquefinder.domain.model.Ayah
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.GlobalScope
import timber.log.Timber
import java.util.*

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    val api:QuranApiInterface,
    val dao: QuranDao
) :
    Worker(context, params) {

    private val CHANNEL_ID = "notify-ayah"
    private var repository: AyahRepositoryImpl? = null
    private var randomAyah: Ayah? = null
    private val randomSurahNumber = (1..114).random()
    private val mContext = context

    companion object {
        var notificationId = 90
    }

    init {
        repository = AyahRepositoryImpl(dao,api)
        randomAyah = repository!!.getRandomAyah(randomSurahNumber)
        createNotificationChannel()
    }

    override fun doWork(): Result {
        randomAyah?.let { makeNotification(it) }
        return Result.success()
    }

    private fun makeNotification(ayah: Ayah) {
        if (Locale.getDefault().language.contentEquals("fr")) {
            var contentText: String? = null
            if (ayah.frenchTranslation == null || ayah.frenchTranslation.equals("empty")) {
                contentText = ayah.translation
            } else {
                contentText = ayah.frenchTranslation
            }
            val alarmSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val largeIcon = BitmapFactory.decodeResource(mContext.resources, R.drawable.logo2)
            val builder = NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo2)
                .setLargeIcon(largeIcon)
                .setContentTitle(mContext.getString(R.string.notification_title))
                .setContentText(ayah.surah_number.toString() + ":" + ayah.verse_number + " " + contentText)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(ayah.surah_number.toString() + ":" + ayah.verse_number + " " + contentText)
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setColor(mContext.resources.getColor(R.color.whiteTransparent))
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setSound(alarmSound)
                .setAutoCancel(true)

            Timber.d("value of surah:" + ayah.surah_number)
            Timber.d("value of verse:" + ayah.verse_number)

            with(NotificationManagerCompat.from(mContext)) {
                // notificationId is a unique int for each notification that you must define
                notify(notificationId, builder.build())
            }
        } else {
            val contentText = ayah.translation
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val largeIcon = BitmapFactory.decodeResource(mContext.resources, R.drawable.logo2)
            var builder = NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo2)
                .setLargeIcon(largeIcon)
                .setContentTitle(mContext.getString(R.string.notification_title))
                .setContentText(ayah.surah_number.toString() + ":" + ayah.verse_number + " " + contentText)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(ayah.surah_number.toString() + ":" + ayah.verse_number + " " + contentText)
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setColor(mContext.resources.getColor(R.color.whiteTransparent))
                .setSound(alarmSound)
                .setAutoCancel(true)
            Timber.d("value of surah:" + ayah.surah_number)
            Timber.d("value of verse:" + ayah.verse_number)
            with(NotificationManagerCompat.from(mContext)) {
                // notificationId is a unique int for each notification that you must define
                notify(notificationId, builder.build())
            }
        }


    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "channel"
            val descriptionText = "channel description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}