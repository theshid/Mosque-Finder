package com.shid.mosquefinder.app.initializer

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.startup.Initializer
import androidx.work.Configuration
import androidx.work.WorkManager
import javax.inject.Inject

class WorkManagerInitializer : Initializer<Unit> {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun create(context: Context) {
        val configuration = Configuration.Builder().apply {
            setWorkerFactory(workerFactory)
            setMinimumLoggingLevel(Log.VERBOSE)
        }.build()
        WorkManager.initialize(context, configuration)

    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        // No dependencies on other libraries.
        return emptyList()
    }
}