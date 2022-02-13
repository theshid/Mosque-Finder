package com.shid.mosquefinder.Ui.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.res.ResourcesCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.shid.mosquefinder.Data.Repository.AyahRepository
import com.shid.mosquefinder.Data.database.QuranDatabase
import com.shid.mosquefinder.Data.database.entities.Ayah
import com.shid.mosquefinder.R
import kotlinx.coroutines.GlobalScope
import timber.log.Timber
import java.util.*


class NotificationWorker(var context: Context, params: WorkerParameters) :
    Worker(context, params) {

    private val CHANNEL_ID = "notify-ayah"
    private var repository: AyahRepository? = null
    private var randomAyah: Ayah? = null
    private val randomSurahNumber = (1..114).random()

    companion object {
        private var notificationId = 90
    }

    init {
        val dao = QuranDatabase.getDatabase(context, GlobalScope, context.resources).surahDao()
        repository = AyahRepository(dao)
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
            val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.logo2)
            var builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo2)
                .setLargeIcon(largeIcon)
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(ayah.surah_number.toString() + ":"+ayah.verse_number+" "+contentText)
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText(ayah.surah_number.toString() + ":"+ayah.verse_number+" "+contentText))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setColor(context.resources.getColor(R.color.whiteTransparent))
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setSound(alarmSound)
                .setAutoCancel(true)

            Timber.d("value of surah:"+ayah.surah_number)
            Timber.d("value of verse:"+ayah.verse_number)

            with(NotificationManagerCompat.from(context)) {
                // notificationId is a unique int for each notification that you must define
                notify(notificationId, builder.build())
            }
        } else {
            val contentText = ayah.translation
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.logo2)
            var builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo2)
                .setLargeIcon(largeIcon)
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(ayah.surah_number.toString() + ":"+ayah.verse_number+" "+contentText)
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText(ayah.surah_number.toString() + ":"+ayah.verse_number+" "+contentText))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setColor(context.resources.getColor(R.color.whiteTransparent))
                .setSound(alarmSound)
                .setAutoCancel(true)
            Timber.d("value of surah:"+ayah.surah_number)
            Timber.d("value of verse:"+ayah.verse_number)
            with(NotificationManagerCompat.from(context)) {
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
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}