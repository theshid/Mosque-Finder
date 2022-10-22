package com.shid.mosquefinder.app.initializer

import android.content.Context
import androidx.startup.Initializer
import androidx.work.WorkManager
import com.shid.mosquefinder.app.di.InitializerEntryPoint
import com.shid.mosquefinder.app.workers.DailyAyahWorker
import javax.inject.Inject

@Suppress("unused")
class ScheduledWorkInitializer : Initializer<Unit> {

    @Inject
    internal lateinit var manager: WorkManager

    @Inject
    internal lateinit var trigger: DailyAyahWorker

    override fun create(context: Context) {
        InitializerEntryPoint(context).inject(this)
        trigger.triggerWork()
    }

    override fun dependencies(): List<Class<out Initializer<*>>> =
        listOf(
            TimberInitializer::class.java,
            WorkManagerInitializer::class.java,
            DependencyGraphInitializer::class.java
        )
}