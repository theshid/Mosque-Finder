package com.shid.mosquefinder.app.workers

import android.content.res.Resources
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.shid.mosquefinder.R
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PeriodicWorkTrigger @Inject constructor(
    private val resources: Resources,
    private val workManager: WorkManager
) {

    fun triggerWork() {
        workManager.enqueueUniquePeriodicWork(
            resources.getString(R.string.surah_crawler_work_name),
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<AyahCrawler>(
                Duration.ofHours(12L)
            ).build()
        )
    }
}