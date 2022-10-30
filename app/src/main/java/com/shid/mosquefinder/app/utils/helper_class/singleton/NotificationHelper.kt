package com.shid.mosquefinder.app.utils.helper_class.singleton

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.ui.main.views.BlogActivity
import com.shid.mosquefinder.app.ui.services.downloadUtil.NotificationHelper.Companion.CHANNEL_ID

object NotificationHelper {
    @RequiresApi(Build.VERSION_CODES.M)
    fun showNotification(context: Context, pendingIntent: PendingIntent, message: String) {
        val alarmSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.logo2)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo2)
            .setLargeIcon(largeIcon)
            .setContentTitle(context.getString(R.string.notification_helper_title))
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setColor(context.resources.getColor(R.color.whiteTransparent))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setSound(alarmSound)
            .setAutoCancel(true)
        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            notify(
                com.shid.mosquefinder.app.ui.notification.NotificationWorker.Companion.notificationId,
                builder.build()
            )
        }


    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun getPendingIntent(context: Context): PendingIntent {
        val intentBlog = Intent(context, BlogActivity::class.java)
        intentBlog.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        return PendingIntent.getActivity(context, 0, intentBlog, PendingIntent.FLAG_IMMUTABLE)
    }
}