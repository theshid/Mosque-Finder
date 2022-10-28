package com.shid.mosquefinder

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import androidx.work.impl.Scheduler.MAX_SCHEDULER_LIMIT
import com.shid.mosquefinder.app.utils.helper_class.SharePref

import com.shid.mosquefinder.app.utils.network.ConnectivityStateHolder.registerConnectivityBroadcaster
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class App : Application(),Configuration.Provider {

    @Inject
    lateinit var sharedPref: SharePref
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    private val lifecycle = ProcessLifecycleOwner.get().lifecycle


    override fun onCreate() {
        super.onCreate()
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                sharedPref.isAppInBackground(false)
                Timber.d("foreground")
            }

            override fun onPause(owner: LifecycleOwner) {
                super.onPause(owner)
                sharedPref.isAppInBackground(true)
                Timber.d("background")
            }
        })
        registerConnectivityBroadcaster()
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder().setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()
    }

    /* override fun getWorkManagerConfiguration(): Configuration {
         return Configuration.Builder().setWorkerFactory(workerFactory)
             .setMinimumLoggingLevel(android.util.Log.DEBUG)
             .build()
     }*/

}