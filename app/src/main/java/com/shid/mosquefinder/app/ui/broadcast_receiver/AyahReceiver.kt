package com.shid.mosquefinder.app.ui.broadcast_receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.utils.helper_class.Constants.ACTION_RETRY
import com.shid.mosquefinder.app.utils.helper_class.Constants.notificationId
import com.shid.mosquefinder.app.workers.AyahCrawler
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.time.Duration
import javax.inject.Inject

@AndroidEntryPoint
class AyahReceiver : BroadcastReceiver() {

    @Inject
    lateinit var workManager: WorkManager

    override fun onReceive(context: Context, intent: Intent) {
        if (ACTION_RETRY == intent.action) {
            Timber.d("inside onReceive")
            workManager.enqueueUniquePeriodicWork(
                context.getString(R.string.comic_crawler_work_name),
                ExistingPeriodicWorkPolicy.REPLACE,
                PeriodicWorkRequestBuilder<AyahCrawler>(
                    Duration.ofHours(12L)
                ).build()
            )
            NotificationManagerCompat.from(context).cancel(null, notificationId)
        }
    }
}