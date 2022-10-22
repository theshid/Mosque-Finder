package com.shid.mosquefinder.app.workers

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.shid.mosquefinder.app.ui.main.views.SettingsActivity
import com.shid.mosquefinder.app.ui.notification.NotificationWorker
import com.shid.mosquefinder.app.utils.helper_class.Constants
import com.shid.mosquefinder.app.utils.helper_class.SharePref
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyAyahWorker @Inject constructor(
    private val workManager: WorkManager,
    private val sharePref: SharePref
) {

    fun triggerWork() {
        val saveRequest: PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.DAYS)
                .addTag(Constants.WORKER_TAG)
                .build()

        if (sharePref.loadSwitchState()) {
            workManager.enqueueUniquePeriodicWork(
                Constants.WORKER_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                saveRequest
            )
        } else {
            workManager.cancelAllWorkByTag(SettingsActivity.SettingsFragment.WORKER_TAG)
        }
    }
}