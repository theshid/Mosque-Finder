package com.shid.mosquefinder

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.impl.Scheduler.MAX_SCHEDULER_LIMIT

import com.shid.mosquefinder.app.utils.network.ConnectivityStateHolder.registerConnectivityBroadcaster
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {
    /*@EntryPoint
    @InstallIn(SingletonComponent::class)
    interface HiltWorkerFactoryEntryPoint {
        fun workerFactory(): HiltWorkerFactory
    }*/
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    companion object {
        var context: Context? = null
        lateinit var application: App
    }

    override fun onCreate() {
        super.onCreate()

        context = this;
        application = this
        registerConnectivityBroadcaster()
        /*SoLoader.init(this, false)

        if (BuildConfig.DEBUG && FlipperUtils.shouldEnableFlipper(this)) {
            val client = AndroidFlipperClient.getInstance(this)
            client.addPlugin(InspectorFlipperPlugin(this, DescriptorMapping.withDefaults()))
            client.addPlugin(DatabasesFlipperPlugin(this))
            client.start()
        }*/
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder().setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()
    }

    /*override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setExecutor(Dispatchers.Default.asExecutor())
            .setWorkerFactory(EntryPoints.get(this, HiltWorkerFactoryEntryPoint::class.java).workerFactory())
            .setTaskExecutor(Dispatchers.Default.asExecutor())
            .setMaxSchedulerLimit(MAX_SCHEDULER_LIMIT)
            .build()
    }*/
}